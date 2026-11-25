package com.library.demo.service;

import com.library.demo.dto.AuthResponse;
import com.library.demo.dto.LoginRequest;
import com.library.demo.dto.RegisterRequest;
import com.library.demo.dto.UserResponse;
import com.library.demo.entity.AppUser;
import com.library.demo.entity.Role;
import com.library.demo.exception.ApiException;
import com.library.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;

    @Value("${security.jwt.issuer}")
    private String jwtIssuer;

    @Value("${security.jwt.access-token-ttl-seconds}")
    private long accessTokenTtlSeconds;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String username = normalizeUsername(request.username());
        if (userRepository.existsByUsername(username)) {
            throw ApiException.conflict("Bu username band: " + username);
        }
        AppUser user = new AppUser();
        user.setFullName(request.fullName().trim());
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.MEMBER);
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String username = normalizeUsername(request.username());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.password()));
        } catch (AuthenticationException ex) {
            throw ApiException.unauthorized("Username yoki parol noto'g'ri");
        }

        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> ApiException.unauthorized("Username yoki parol noto'g'ri"));
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtIssuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(accessTokenTtlSeconds))
                .subject(user.getUsername())
                .claim("roles", List.of(user.getRole().name()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new AuthResponse(token, "Bearer", accessTokenTtlSeconds, UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public UserResponse me(String username) {
        AppUser user = userRepository.findByUsername(normalizeUsername(username))
                .orElseThrow(() -> ApiException.notFound("Foydalanuvchi topilmadi"));
        return UserResponse.from(user);
    }

    private String normalizeUsername(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }
}

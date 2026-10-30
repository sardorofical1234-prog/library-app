package com.library.demo.service;

import com.library.demo.dto.RegisterRequest;
import com.library.demo.dto.UserResponse;
import com.library.demo.entity.AppUser;
import com.library.demo.entity.Role;
import com.library.demo.exception.ApiException;
import com.library.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw ApiException.conflict("Bu username band: " + request.username());
        }
        AppUser user = new AppUser();
        user.setFullName(request.fullName());
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.MEMBER);
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse me(String username) {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> ApiException.notFound("Foydalanuvchi topilmadi"));
        return UserResponse.from(user);
    }
}

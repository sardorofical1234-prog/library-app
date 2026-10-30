package com.library.demo.config;

import com.library.demo.entity.AppUser;
import com.library.demo.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // REST API + Basic Auth: CSRF kerak emas
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Kitoblarni ko'rish - har qanday login qilgan foydalanuvchi
                        .requestMatchers(HttpMethod.GET, "/api/books/**").authenticated()
                        // Kitob qo'shish/o'zgartirish/o'chirish - faqat ADMIN
                        .requestMatchers("/api/books/**").hasRole("ADMIN")
                        // Hamma ijaralar va kechikkanlar - faqat ADMIN
                        .requestMatchers("/api/loans/all", "/api/loans/overdue").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Foydalanuvchini bazadan username bo'yicha topadi. */
    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> {
            AppUser user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Foydalanuvchi topilmadi: " + username));
            return User.withUsername(user.getUsername())
                    .password(user.getPassword())
                    .roles(user.getRole().name())
                    .build();
        };
    }
}

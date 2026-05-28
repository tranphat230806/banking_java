package com.example.banking.config;

import com.example.banking.Security.CustomAuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@EnableWebSecurity
public class PasswordConfig {

    private final CustomAuthenticationSuccessHandler successHandler;

    public PasswordConfig(CustomAuthenticationSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        // Public routes
                        .requestMatchers("/login", "/forgot", "/reset", "/create",
                                "/forgot/**", "/css/**", "/js/**", "/uploads/**").permitAll()

                        // Admin-only routes
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")

                        // User-only routes (banking, giao dịch, QR)
                        .requestMatchers("/dashboard", "/banking", "/bill/**",
                                "/history", "/qr", "/myQR",
                                "/transfer/**", "/face/**", "/face-verify/**",
                                "/scanQR", "/getUserByCode").hasAuthority("ROLE_USER")

                        // Profile & upload: cả admin lẫn user
                        .requestMatchers("/profile", "/profile/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler(successHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
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
                        .requestMatchers("/login", "/forgot", "/reset", "/create", "/forgot/**", "/css/**", "/js/**").permitAll()
                        // 1. Phân quyền riêng cho Admin (Bao gồm dashboard của admin)
                        .requestMatchers("/admin/**", "/admin/dashboard").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/dashboard").hasAuthority("ROLE_USER")
                        .requestMatchers("/banking", "/bill/**", "/history", "/profile", "/qr", "/myQR")
                        .hasAnyAuthority("ROLE_USER", "ROLE_ADMIN", "USER", "ADMIN")
                        .anyRequest().authenticated()
                ).formLogin(form -> form
                        .loginPage("/login")// nếu chưa có login page thì tạm bỏ dòng này
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
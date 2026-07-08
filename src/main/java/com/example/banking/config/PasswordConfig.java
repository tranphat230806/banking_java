package com.example.banking.config;

import com.example.banking.Security.CustomAuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;

@Configuration
@EnableWebSecurity
public class PasswordConfig {

    private final CustomAuthenticationSuccessHandler successHandler;

    public PasswordConfig(CustomAuthenticationSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf
                        // Chỉ bỏ qua CSRF cho các đường dẫn API để Postman gọi không bị 403
                        .ignoringRequestMatchers("/api/**")
                )
                .authorizeHttpRequests(auth -> auth
                        // Public routes
                        .requestMatchers("/login", "/forgot", "/reset", "/create",
                                "/forgot/**", "/css/**", "/js/**", "/uploads/**").permitAll()

                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")

                        // User-only routes (banking, giao dịch, QR)
                        .requestMatchers("/dashboard", "/banking", "/bill/**",
                                "/history", "/qr", "/myQR",
                                "/transfer/**", "/face/**", "/face-verify/**",
                                "/scanQR", "/getUserByCode","/api/**").hasAuthority("ROLE_USER")

                        // Profile & upload: cả admin lẫn user
                        .requestMatchers("/profile", "/profile/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .sessionRegistry(sessionRegistry())  // Dùng registry tường minh theo dõi cross-device
                        .maxSessionsPreventsLogin(false)      // Cho phép đăng nhập mới, đá session cũ
                        .expiredUrl("/login?expired=true")   // Chuyển hướng session cũ khi bị đá
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
                        .invalidateHttpSession(true)       // Xóa toàn bộ session (bao gồm admin_face_verified)
                        .clearAuthentication(true)         // Xóa SecurityContext
                        .deleteCookies("JSESSIONID")       // Xóa cookie session trên browser
                        .addLogoutHandler(new LogoutHandler() {
                            @Override
                            public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
                                if (authentication != null && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN"))) {
                                    com.example.banking.Controller.ChatController.clearChatHistory();
                                }
                            }
                        })
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public org.springframework.security.web.session.HttpSessionEventPublisher httpSessionEventPublisher() {
        return new org.springframework.security.web.session.HttpSessionEventPublisher();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
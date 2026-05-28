package com.example.banking.Security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // Trích xuất danh sách quyền hiện tại của tài khoản vừa đăng nhập thành công
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

        // Kiểm tra Admin — luôn yêu cầu xác thực khuôn mặt sau đăng nhập
        if (roles.contains("ROLE_ADMIN") || roles.contains("ADMIN")) {
            response.sendRedirect("/admin/face-verify");
        }
        // Kiểm tra User (chấp nhận cả ROLE_USER hoặc USER)
        else if (roles.contains("ROLE_USER") || roles.contains("USER")) {
            response.sendRedirect("/dashboard");
        } else {
            response.sendRedirect("/login?error=unauthorized");
        }
    }
}
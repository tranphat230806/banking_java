package com.example.banking.config;

import com.example.banking.Security.AdminFaceIdInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AdminFaceIdInterceptor adminFaceIdInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminFaceIdInterceptor)
                .addPathPatterns("/admin/**")
                // Loại trừ trang face-verify và các API của nó để tránh redirect loop
                .excludePathPatterns(
                        "/admin/face-verify",
                        "/admin/face-verify/**",
                        "/admin/face/**"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}

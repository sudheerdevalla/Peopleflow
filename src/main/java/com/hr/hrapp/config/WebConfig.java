package com.hr.hrapp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private MfaInterceptor mfaInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(mfaInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login",
                        "/default",
                        "/mfa",
                        "/mfa/verify",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/uploads/**"
                );
    }
}

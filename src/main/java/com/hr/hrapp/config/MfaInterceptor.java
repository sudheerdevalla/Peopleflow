package com.hr.hrapp.config;

import com.hr.hrapp.entity.User;
import com.hr.hrapp.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class MfaInterceptor implements HandlerInterceptor {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        String uri = request.getRequestURI();

        // MFA/login related pages are always allowed
        if (uri.equals("/login")
                || uri.equals("/default")
                || uri.equals("/mfa")
                || uri.equals("/mfa/verify")
                || uri.startsWith("/api/mfa/")
            || uri.equals("/error")
                || uri.startsWith("/css/")
                || uri.startsWith("/js/")
                || uri.startsWith("/images/")) {
            return true;
        }

        // User is not logged in
        if (request.getUserPrincipal() == null) {
            return true;
        }

        String username = request.getUserPrincipal().getName();

        User user = userRepository.findByUsername(username).orElse(null);

        if (user != null) {

            Boolean verified =
                    (Boolean) request.getSession().getAttribute("MFA_VERIFIED");

            if (!Boolean.TRUE.equals(verified)) {
                response.sendRedirect("/mfa");
                return false;
            }
        }

        return true;
    }
}

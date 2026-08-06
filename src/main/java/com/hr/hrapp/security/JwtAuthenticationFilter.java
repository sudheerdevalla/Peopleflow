package com.hr.hrapp.security;

import com.hr.hrapp.service.CustomerUserDetailsService;

import io.jsonwebtoken.JwtException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomerUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        // Swagger skip
        if (path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")) {

            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader =
                request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authHeader != null
                && authHeader.startsWith("Bearer ")) {

            jwt = authHeader.substring(7);

            try {

                username =
                        jwtUtil.extractUsername(jwt);

            } catch (JwtException e) {

                response.setStatus(
                        HttpServletResponse.SC_UNAUTHORIZED);

                response.getWriter().write(
                        "Invalid JWT Token");

                return;

            } catch (Exception e) {

                response.setStatus(
                        HttpServletResponse.SC_UNAUTHORIZED);

                response.getWriter().write(
                        "Authentication Failed");

                return;
            }
        }

        if (username != null
                && SecurityContextHolder
                .getContext()
                .getAuthentication() == null) {

            UserDetails userDetails =
                    userDetailsService
                            .loadUserByUsername(username);

            if (jwtUtil.validateToken(
                    jwt,
                    userDetails.getUsername())) {

                String authoritiesStr =
                        jwtUtil.extractAuthorities(jwt);

                List<SimpleGrantedAuthority> authorities =
                        authoritiesStr != null
                                && !authoritiesStr.isEmpty()

                                ? Arrays.stream(
                                        authoritiesStr.split(","))
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList())

                                : userDetails.getAuthorities()
                                .stream()
                                .map(a -> new SimpleGrantedAuthority(
                                        a.getAuthority()))
                                .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                authorities);

                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request));

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authToken);
            }
        }

        filterChain.doFilter(
                request,
                response);
    }
}
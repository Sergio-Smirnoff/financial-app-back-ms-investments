package com.financialapp.investments.infrastructure.config;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class InternalAuthFilter extends OncePerRequestFilter {

    private final String internalToken;

    public InternalAuthFilter(@Value("${internal.auth.token}") String internalToken) {
        this.internalToken = internalToken;
    }

    @PostConstruct
    void validate() {
        Assert.hasText(internalToken, "internal.auth.token must not be blank");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/actuator") || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestToken = request.getHeader("X-Internal-Token");

        if (!internalToken.equals(requestToken)) {
            log.warn("Unauthorized internal request to path: {}. Missing or invalid X-Internal-Token", path);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: Missing or invalid internal token");
            return;
        }

        filterChain.doFilter(request, response);
    }
}

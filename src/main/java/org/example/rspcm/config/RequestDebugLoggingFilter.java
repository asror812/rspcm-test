package org.example.rspcm.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestDebugLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        long startedAt = System.currentTimeMillis();

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String pathWithQuery = query == null ? uri : uri + "?" + query;

        String origin = request.getHeader("Origin");
        String userAgent = request.getHeader("User-Agent");
        // String requestId = request.getHeader("X-Request-Id");
        // String authorization = request.getHeader("Authorization");
        // boolean authHeaderPresent = authorization != null && authorization.startsWith("Bearer ");
        String contentType = request.getContentType();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - startedAt;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean authenticated = authentication != null && authentication.isAuthenticated();
            String principal = authentication == null ? null : authentication.getName();
            log.info(
                    "REQ {} {} status={} origin={} contentType={} remote={} ua=\"{}\" took={}ms",
                    method,
                    pathWithQuery,
                    response.getStatus(),
                    origin,
                    contentType,
                    request.getRemoteAddr(),
                    userAgent,
                    durationMs
            );
        }
    }
}

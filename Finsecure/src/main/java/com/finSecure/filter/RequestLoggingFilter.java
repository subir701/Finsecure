package com.finSecure.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Logs every incoming HTTP request and its response status + duration.
 * Also injects a unique requestId into MDC so all log lines for the same
 * request share a traceable identifier.
 */
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String requestId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("requestId", requestId);

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String fullUri = query != null ? uri + "?" + query : uri;

        long start = System.currentTimeMillis();
        log.info("--> {} {}", method, fullUri);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;
            int status = response.getStatus();

            if (status >= 500) {
                log.error("<-- {} {} {} ({}ms)", status, method, fullUri, duration);
            } else if (status >= 400) {
                log.warn("<-- {} {} {} ({}ms)", status, method, fullUri, duration);
            } else {
                log.info("<-- {} {} {} ({}ms)", status, method, fullUri, duration);
            }

            MDC.clear();
        }
    }
}
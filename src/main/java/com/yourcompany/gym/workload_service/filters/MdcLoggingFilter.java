package com.yourcompany.gym.workload_service.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class MdcLoggingFilter extends OncePerRequestFilter {


    private static final String HEADER_NAME = "X-Transaction-Id";
    private static final String MDC_KEY = "transactionId";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String transactionId = null;
        try {
            transactionId = request.getHeader(HEADER_NAME);
            if (transactionId == null) {
                transactionId = UUID.randomUUID().toString();
            }
            MDC.put(MDC_KEY, transactionId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}

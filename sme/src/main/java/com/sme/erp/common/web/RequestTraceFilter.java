package com.sme.erp.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class RequestTraceFilter extends OncePerRequestFilter {
    private static final String TRACE_HEADER = "X-Request-ID";
    private static final String MDC_TRACE_ID = "traceId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String traceId = resolveTraceId(request);
        MDC.put(MDC_TRACE_ID, traceId);
        response.setHeader(TRACE_HEADER, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_TRACE_ID);
        }
    }

    private String resolveTraceId(HttpServletRequest request) {
        String value = request.getHeader(TRACE_HEADER);
        return value == null || value.isBlank() ? UUID.randomUUID().toString() : value.trim();
    }
}

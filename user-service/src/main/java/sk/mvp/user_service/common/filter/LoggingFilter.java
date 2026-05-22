package sk.mvp.user_service.common.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;
import sk.mvp.user_service.common.http.CachedHttpServletRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.UUID;

@Component
public class LoggingFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // loguj len endpointy začínajúce /api
        if (!request.getRequestURI().startsWith("/api")) {
            filterChain.doFilter(request, response);
            return;
        }

//        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
         CachedHttpServletRequest cachedHttpServletRequest = new CachedHttpServletRequest(request);
         ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);


        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put(CORRELATION_ID_HEADER, correlationId);

        try {
            logRequest(cachedHttpServletRequest, correlationId);

            filterChain.doFilter(cachedHttpServletRequest, wrappedResponse);

            logResponse(wrappedResponse, correlationId);
        } finally {
            wrappedResponse.copyBodyToResponse(); // nezabudni odovzdať response späť klientovi
            MDC.remove(CORRELATION_ID_HEADER);
        }
    }


    private void logRequest(CachedHttpServletRequest request, String correlationId) throws IOException {
        StringBuilder headers = new StringBuilder();
        Enumeration<String> headerNames = request.getHeaderNames();
        for (String headerName : Collections.list(headerNames)) {
            headers.append(headerName).append("=").append(request.getHeader(headerName)).append("; ");
        }
        HttpSession session = request.getSession(false);
        String sessionId = (session != null) ? session.getId() : "no-session";

        String body = (request.getCachedPayload() != null && request.getCachedPayload().length > 0)
                ? new String(request.getCachedPayload(), StandardCharsets.UTF_8)
                : "";
        log.info("[{}] Incoming Request [{} {}], sessionId={}, headers=[{}], body={}",
                correlationId,
                request.getMethod(),
                request.getRequestURI(),
                sessionId,
                headers,
                body);
    }

    private void logResponse(ContentCachingResponseWrapper response, String correlationId) throws IOException {
        String body = new String(response.getContentAsByteArray(), StandardCharsets.UTF_8);
        log.info("[{}] Outgoing Response, status={}, body={}", correlationId, response.getStatus(), body);
    }


}

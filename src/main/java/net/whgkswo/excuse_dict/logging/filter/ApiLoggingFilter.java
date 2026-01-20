package net.whgkswo.excuse_dict.logging.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiLoggingFilter extends OncePerRequestFilter {

    private static final String SEPARATOR_START = "=".repeat(15) + " Request Start " + "=".repeat(15);
    private static final String SEPARATOR_END = "=".repeat(15) + " Request End " + "=".repeat(15);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if(shouldNotFilter(request)){
            filterChain.doFilter(request, response);
            return;
        }

        // 요청, 응답정보 여러 번 읽을 수 있도록 랩핑
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        String requestId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("requestId", requestId);

        String endpoint = extractEndpoint(request.getRequestURI(), request.getMethod());
        MDC.put("endpoint", endpoint);

        long startTime = System.currentTimeMillis();

        try{
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logApiCall(requestWrapper, responseWrapper, duration);
            responseWrapper.copyBodyToResponse(); // 이거 없으면 클라이언트가 빈 응답 받음
            MDC.clear();
        }
    }

    private String extractEndpoint(String uri, String method) {
        // 쿼리 스트링 제거
        String path = uri.split("\\?")[0];

        // 맨 앞 슬래시 제거
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        // 숫자로만 이루어진 경로 세그먼트를 {id}로 치환
        String[] parts = path.split("/");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].matches("\\d+")) {
                parts[i] = "{id}";
            }
        }

        return method + "/" + String.join("/", parts);
    }

    // 로그 기록
    private void logApiCall(ContentCachingRequestWrapper request,
                            ContentCachingResponseWrapper response,
                            long duration){

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        int status = response.getStatus();

        // 요청 로깅 시작
        log.info(SEPARATOR_START);

        log.info("[{}] [API] {} {} {} - {}ms",
                method,
                uri + (queryString != null ? "?" + queryString : ""),
                status,
                duration);

        if (shouldLogRequestBody(method)) {
            String requestBody = getRequestBody(request);
            if (!requestBody.isBlank()) {
                log.info("[Request Body] {}", requestBody);
            }
        }

        String responseBody = getResponseBody(response);

        if (!responseBody.isBlank()) {
            if (status >= 400) {
                log.warn("[Response Body] Status: {}, Body: {}", status, responseBody);
            } else {
                log.info("[Response Body] Status: {}, Body: {}", status, responseBody);
            }
        }

        // 요청 로깅 종료
        log.info(SEPARATOR_END);
    }

    private boolean shouldLogRequestBody(String method) {
        return method.equals("POST") || method.equals("PUT") || method.equals("PATCH");
    }

    private String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length == 0) {
            return "";
        }
        return new String(content, StandardCharsets.UTF_8);
    }

    private String getResponseBody(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        if (content.length == 0) {
            return "";
        }
        return new String(content, StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getRequestURI();
        String method = request.getMethod();

        if("OPTIONS".equals(method)) return true;

        // 제미나이는 필터 적용하면 프론트가 응답을 못 받는 버그 있음;;
        if(path.contains("generate")) return true;

        return !path.startsWith("/api");
    }
}

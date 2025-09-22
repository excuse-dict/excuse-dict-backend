package net.whgkswo.excuse_bundle.gemini.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public <T> Mono<T> generateText(String prompt, Class<T> responseType, T fallback) {

        // 요청 바디 생성
        Map<String, Object> requestBody = createRequestBody(prompt);

        // API URL
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;

        // POST 요청 전송
        return webClient
                .post()
                .uri(url)
                .header("Content-Type", "application/json; charset=utf-8")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(this::extractResponse)
                .flatMap(text -> parseResponse(text, responseType))
                .onErrorResume(e -> Mono.just(fallback));
    }

    // 요청 바디 생성
    private Map<String, Object> createRequestBody(String prompt) {
        return Map.of("contents", List.of(
                Map.of("parts", List.of(
                        Map.of("text", prompt)
                ))
        ));
    }

    // 응답 데이터 문자열로 추출
    @SuppressWarnings("unchecked")
    private String extractResponse(Map<String, Object> response) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            throw new RuntimeException("응답 파싱 실패: " + e.getMessage());
        }
    }

    // 응답 데이터 파싱
    private <T> Mono<T> parseResponse(String text, Class<T> responseType) {
        try {
            if (responseType == String.class) {
                return Mono.just((T) text);
            }
            // JSON 파싱 시도
            T parsed = objectMapper.readValue(text, responseType);
            return Mono.just(parsed);
        } catch (Exception e) {
            return Mono.error(new RuntimeException("JSON 파싱 실패: " + e.getMessage()));
        }
    }
}

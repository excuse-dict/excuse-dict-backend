package net.whgkswo.excuse_dict.lib.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_dict.exceptions.BusinessLogicException;
import net.whgkswo.excuse_dict.exceptions.ExceptionType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JsonHelper {

    private final ObjectMapper objectMapper;

    // 객체 -> json 직렬화
    public <T> String serialize(T data){

        try{
            if(data instanceof String){
                return (String) data;   // objectMapper를 거치면 불필요한 따옴표가 붙으므로 직접 반환
            }else {
                return objectMapper.writeValueAsString(data);
            }
        } catch (JsonProcessingException e) {
            throw new BusinessLogicException(ExceptionType.SERIALIZATION_FAILED);
        }
    }

    // json -> 객체 역직렬화
    public <T> T deserialize(String jsonData, Class<T> clazz){

        if (clazz == String.class) {
            return clazz.cast(jsonData); // objectMapper를 거치면 불필요한 따옴표가 붙으므로 직접 반환
        }

        try{
            return objectMapper.readValue(jsonData, clazz);
        } catch (JsonProcessingException e) {
            throw new BusinessLogicException(ExceptionType.DESERIALIZATION_FAILED);
        }
    }

    // 역직렬화 (Mono)
    public <T> Mono<T> deserializeMono(String jsonData, Class<T> responseType) {
        return Mono.fromCallable(() -> deserialize(jsonData, responseType));
    }

    // 순수 json 객체만 남기기
    public String clearJson(String rawJson){
        if (rawJson == null || rawJson.trim().isEmpty()) {
            return rawJson;
        }

        String cleaned = rawJson.trim();

        // 코드블록 패턴 제거
        cleaned = cleaned.replaceAll("```json\\s*", "");
        cleaned = cleaned.replaceAll("```\\s*", "");
        cleaned = cleaned.replaceAll("^```.*?\\n", "");
        cleaned = cleaned.replaceAll("\\n```$", "");

        // JSON의 시작과 끝 찾기 (객체 또는 배열)
        int jsonStart = -1;
        int jsonEnd = -1;

        // 중괄호 객체 찾기
        int firstBrace = cleaned.indexOf('{');
        int lastBrace = cleaned.lastIndexOf('}');

        // 대괄호 배열 찾기
        int firstBracket = cleaned.indexOf('[');
        int lastBracket = cleaned.lastIndexOf(']');

        // 더 먼저 시작하는 것 선택
        if (firstBrace >= 0 && (firstBracket < 0 || firstBrace < firstBracket)) {
            // 객체가 먼저 시작
            jsonStart = firstBrace;
            jsonEnd = lastBrace;
        } else if (firstBracket >= 0) {
            // 배열이 먼저 시작
            jsonStart = firstBracket;
            jsonEnd = lastBracket;
        }

        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            cleaned = cleaned.substring(jsonStart, jsonEnd + 1);
        }

        return cleaned.trim();
    }
}

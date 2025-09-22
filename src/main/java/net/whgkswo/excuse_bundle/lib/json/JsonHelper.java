package net.whgkswo.excuse_bundle.lib.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.exceptions.BusinessLogicException;
import net.whgkswo.excuse_bundle.exceptions.ExceptionType;
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
        return Mono.fromCallable(() -> deserialize(jsonData, responseType))
                .onErrorMap(
                        BusinessLogicException.class,
                        e -> new RuntimeException("JSON 파싱 실패: " + e.getMessage())
                );
    }
}

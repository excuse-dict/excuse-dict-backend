package net.whgkswo.excuse_bundle.elasticsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.exceptions.BusinessLogicException;
import net.whgkswo.excuse_bundle.exceptions.ExceptionType;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ESQueryLoader {
    private final ObjectMapper objectMapper;
    private final Map<String, JsonNode> queries = new ConcurrentHashMap<>();

    // 서버 시작시 한 번에 호출해서 캐시
    @PostConstruct // <- 이걸로 자동 호출
    public void loadQueries() {
        try {
            // elasticsearch/queries 폴더의 모든 JSON 파일 스캔
            Resource[] resources = new PathMatchingResourcePatternResolver()
                    .getResources("classpath:elasticsearch/queries/*.json");

            for (Resource resource : resources) {
                JsonNode queryFile = objectMapper.readTree(resource.getInputStream());

                // 파일 안의 각 쿼리를 개별적으로 저장
                queryFile.fields().forEachRemaining(entry -> {
                    queries.put(entry.getKey(), entry.getValue());
                });
            }

        } catch (IOException e) {
            throw new BusinessLogicException(ExceptionType.ES_QUERY_LOAD_FAILED);
        }
    }

    // jsonPlaceholder -> json 안에 변수로 선언된 값에 대입
    public String getQuery(String queryName, Map<String, Object> jsonPlaceholders) {
        JsonNode queryTemplate = queries.get(queryName);
        if (queryTemplate == null) {
            throw new BusinessLogicException(ExceptionType.ES_QUERY_NOT_FOUND);
        }

        try {
            String queryString = new ObjectMapper().writeValueAsString(queryTemplate);

            // 플레이스홀더 치환
            for (Map.Entry<String, Object> param : jsonPlaceholders.entrySet()) {
                String placeholder = "{{" + param.getKey() + "}}";
                queryString = queryString.replace(placeholder, String.valueOf(param.getValue()));
            }

            return queryString;
        } catch (JsonProcessingException e) {
            // 플레이스홀더 누락되면 예외
            throw new BusinessLogicException(ExceptionType.ES_QUERY_LOAD_FAILED);
        }
    }
}

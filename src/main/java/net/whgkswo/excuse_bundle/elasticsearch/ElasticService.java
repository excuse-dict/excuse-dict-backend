package net.whgkswo.excuse_bundle.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.indices.AnalyzeRequest;
import co.elastic.clients.elasticsearch.indices.AnalyzeResponse;
import co.elastic.clients.elasticsearch.indices.analyze.AnalyzeToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.whgkswo.excuse_bundle.exceptions.BusinessLogicException;
import net.whgkswo.excuse_bundle.exceptions.ExceptionType;
import org.springframework.stereotype.Service;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElasticService {

    private final ElasticsearchClient elasticsearchClient;

    private static final double SIMILARITY_EXACTLY_SAME = 1.0;

    private static final double MIN_SIMILARITY_THRESHOLD = 0.7; // 최소 유사도

    // 그냥 검색
    public <T> List<T> executeSearch(String queryJson, String indexName, Class<T> clazz) {
        try {
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(indexName)
                    .withJson(new StringReader(queryJson))
            );

            SearchResponse<T> response = elasticsearchClient
                    .search(searchRequest, clazz);

            return response.hits().hits().stream()
                    .map(hit -> hit.source())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new BusinessLogicException(ExceptionType.ES_SEARCH_FAILED);
        }
    }

    // 페이지네이션용
    public <T> SearchResponse<T> executeSearchWithResponse(String queryJson, String indexName, Class<T> clazz) {
        try {
            log.info("ES 검색 시작 - Index: {}, Query: {}", indexName, queryJson);
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(indexName)
                    .withJson(new StringReader(queryJson))
            );

            SearchResponse<T> response = elasticsearchClient.search(searchRequest, clazz);

            log.info("ES 검색 완료 - 결과: {} 개", response.hits().total().value());
            return response;

        } catch (Exception e) {
            log.error("ES 검색 실패 - Index: {}, Query: {}, Error: {}", indexName, queryJson, e.getMessage(), e);
            throw new BusinessLogicException(ExceptionType.ES_SEARCH_FAILED);
        }
    }

    // 형태소 입력으로 키워드와의 유사도 계산
    public double calculateKeywordMatchScore(List<String> morphemes, Set<String> keywords){
        if(morphemes.isEmpty() || keywords.isEmpty()){
            return 0.0;
        }

        double maxScore = 0.0;

        // 형태소와 키워드 n:n 유사도 계산 후 최댓값 산출
        for (String morpheme : morphemes) {
            for (String keyword : keywords) {
                double similarity = calculateWordSimilarity(morpheme, keyword);
                if (similarity > MIN_SIMILARITY_THRESHOLD) {  // 70% 이상 유사하면 매칭
                    maxScore = Math.max(maxScore, similarity);
                }
            }
        }

        return maxScore;
    }

    // 두 단어 간 유사도 계산
    public double calculateWordSimilarity(String wordA, String wordB) {
        if (wordA.equals(wordB)) return 1.0;

        // 자모 분해 (정확한 거리 계산을 위해)
        List<Character> decomposedA = decomposeKorean(wordA);
        List<Character> decomposedB = decomposeKorean(wordB);

        // 레벤슈타인 거리 계산
        int distance = getLevenshteinDistance(decomposedA, decomposedB);
        int maxLength = Math.max(decomposedA.size(), decomposedB.size());
        return maxLength == 0 ? 1.0 : 1.0 - (double) distance / maxLength;
    }

    // 레벤슈타인 거리 계산
    private int getLevenshteinDistance(List<Character> wordA, List<Character> wordB) {
        // 두 단어의 각 글자들을 분해해 2차원 배열로 펼침
        int[][] dp = new int[wordA.size() + 1][wordB.size() + 1];

        for (int i = 0; i <= wordA.size(); i++) dp[i][0] = i;
        for (int j = 0; j <= wordB.size(); j++) dp[0][j] = j;

        // 배열의 각 칸의 값은 두 글자 간 최소 변환 거리
        for (int i = 1; i <= wordA.size(); i++) {
            for (int j = 1; j <= wordB.size(); j++) {
                if (wordA.get(i-1).equals(wordB.get(j-1))) {
                    dp[i][j] = dp[i-1][j-1];
                } else {
                    dp[i][j] = Math.min(Math.min(dp[i-1][j], dp[i][j-1]), dp[i-1][j-1]) + 1;
                }
            }
        }

        return dp[wordA.size()][wordB.size()];
    }

    // 한글 자모 분해
    private List<Character> decomposeKorean(String text) {
        List<Character> result = new ArrayList<>();

        for (char ch : text.toCharArray()) {
            if (ch >= 0xAC00 && ch <= 0xD7A3) { // 한글 완성형
                int unicode = ch - 0xAC00;
                int cho = unicode / (21 * 28);     // 초성
                int jung = (unicode % (21 * 28)) / 28; // 중성
                int jong = unicode % 28;            // 종성

                result.add((char)(0x1100 + cho));  // 초성
                result.add((char)(0x1161 + jung)); // 중성
                if (jong > 0) {
                    result.add((char)(0x11A7 + jong)); // 종성
                }
            } else {
                result.add(ch); // 한글이 아닌 경우 그대로
            }
        }

        return result;
    }

    // 불용어 목록 (의미없는 단어들)
    private static final Set<String> STOP_WORDS = Set.of(
            "것", "수", "때", "곳", "데", "등", "점", "경우", "때문",
            "정도", "생각", "마음", "기분", "느낌", "상태", "상황",
            "하다", "되다", "있다", "없다", "같다", "이다"
    );

    // 사용자 입력을 분석해 의미있는 형태소 추출
    public List<String> getMeaningfulMorphemes(String userInput) {
        try {
            AnalyzeRequest request = AnalyzeRequest.of(a -> a
                    .analyzer("nori")
                    .text(userInput));

            AnalyzeResponse response = elasticsearchClient.indices().analyze(request);

            return response.tokens().stream()
                    .map(AnalyzeToken::token) // 토큰 -> 문자열
                    .filter(term -> !term.isEmpty()) // 빈 문자열 제거
                    .filter(term -> !isStopWord(term)) // 불용어 제거
                    .distinct() // 중복 제거
                    .collect(Collectors.toList());

        } catch (Exception e) {
            // 분석 실패 시 띄어쓰기 방식으로 대체
            return Arrays.stream(userInput.toLowerCase().split("\\s+"))
                    .filter(word -> word.length() >= 2)
                    .collect(Collectors.toList());
        }
    }

    // 불용어 체크
    private boolean isStopWord(String word) {
        return STOP_WORDS.contains(word);
    }

    // 형태소 -> 문자열 유사도 계산
    public double calculateMatchScore(List<String> morphemes, String targetString) {
        double maxScore = 0.0;

        // 형태소 중에 태그명과 정확히 일치하는 것이 있는지 확인
        for (String morpheme : morphemes) {
            if (morpheme.equals(targetString)) {
                return SIMILARITY_EXACTLY_SAME; // 완전 일치
            }

            // 오타 허용한 유사도 계산
            double similarity = calculateWordSimilarity(morpheme, targetString);
            if (similarity >= MIN_SIMILARITY_THRESHOLD) {
                maxScore = Math.max(maxScore, similarity);
            }
        }

        return maxScore;
    }
}

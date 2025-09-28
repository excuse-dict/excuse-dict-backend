package net.whgkswo.excuse_bundle.words;

import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import kr.co.shineware.nlp.komoran.model.Token;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.whgkswo.excuse_bundle.komoran.KomoranService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WordService {

    private final KomoranService komoranService;

    private static final double SIMILARITY_EXACTLY_SAME = 1.0;

    private static final double MIN_SIMILARITY_THRESHOLD_DEFAULT = 0.8; // 두 글자 기준 최소 유사도 (오타 허용 기준치)
    private static final double MIN_SIMILARITY_THRESHOLD_MIN = 0.4; // 글자가 길어져도 최소 이것보단 유사해야 함

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
                maxScore = Math.max(maxScore, similarity);
            }
        }

        return maxScore;
    }

    // 두 단어 간 유사도 계산
    private double calculateWordSimilarity(String wordA, String wordB) {
        if (wordA.equals(wordB)) return 1.0;

        // 자모 분해 (정확한 거리 계산을 위해)
        List<Character> decomposedA = decomposeKorean(wordA);
        List<Character> decomposedB = decomposeKorean(wordB);

        // 레벤슈타인 거리 계산
        int distance = getLevenshteinDistance(decomposedA, decomposedB);
        int maxLength = Math.max(decomposedA.size(), decomposedB.size());

        double similarity = maxLength == 0 ? 1.0 : 1.0 - (double) distance / maxLength;

        // 임계값 넘으면 반환
        return similarity >= getMinSimilarityThreshold(wordA, wordB) ? similarity : 0.0;
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
            maxScore = Math.max(maxScore, similarity);
        }

        return maxScore;
    }

    // 두 텍스트 간 유사도 계산
    public double calculateTextSimilarity(String strA, String strB){
        if(strA == null || strA.isEmpty() || strB == null || strB.isEmpty()){ return 0.0; }

        // 형태소 단위로 분해
        List<String> morphemesA = komoranService.getMeaningfulMorphemes(strA);
        List<String> morphemesB = komoranService.getMeaningfulMorphemes(strB);

        return calculateMorphemesSimilarity(morphemesA, morphemesB);
    }

    // 형태소 리스트끼리 유사도 계산
    private double calculateMorphemesSimilarity(List<String> morphemesA, List<String> morphemesB){
        if(morphemesA.isEmpty() || morphemesB.isEmpty()){
            return 0.0;
        }

        double totalSimilarity = 0.0;

        for(String morphemeA : morphemesA){

            double maxSimilarity = 0.0; // 형태소 A가 리스트 B를 순회하며 얻을 수 있는 최대 점수
            for(String morphemeB : morphemesB){
                // 단어 한 쌍 유사도 계산
                double similarity = calculateWordSimilarity(morphemeA, morphemeB);
                maxSimilarity = Math.max(maxSimilarity, similarity);
            }
            // 최대 점수를 더하기
            totalSimilarity += maxSimilarity;
        }

        // 최대 점수의 평균 반환
        return totalSimilarity / morphemesA.size();
    }

    // 글자 수에 따라 통과하기 위한 유사도 임계값 설정
    private double getMinSimilarityThreshold(String strA, String strB){
        // 두 단어 중 짧은 게 기준
        int minLength = Math.min(strA.length(), strB.length());
        // 단어가 길어질 수록 점점 관대해짐
        return Math.max(MIN_SIMILARITY_THRESHOLD_DEFAULT - (minLength - 2) * 0.1, MIN_SIMILARITY_THRESHOLD_MIN);
    }
}

package net.whgkswo.excuse_bundle.elasticsearch;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.komoran.KomoranService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ElasticService {
    private final KomoranService komoranService;

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
                if (similarity > 0.7) {  // 70% 이상 유사하면 매칭
                    maxScore = Math.max(maxScore, similarity);
                }
            }
        }

        return maxScore;
    }

    // 두 단어 간 유사도 계산
    public double calculateWordSimilarity(String word1, String word2) {
        if (word1.equals(word2)) return 1.0;

        // 레벤슈타인 거리 계산
        int distance = getLevenshteinDistance(word1, word2);
        int maxLength = Math.max(word1.length(), word2.length());
        return maxLength == 0 ? 1.0 : 1.0 - (double) distance / maxLength;
    }

    // 레벤슈타인 거리 계산
    private int getLevenshteinDistance(String s1, String s2) {
        // 두 단어의 각 글자들을 분해해 2차원 배열로 펼침
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= s2.length(); j++) dp[0][j] = j;

        // 배열의 각 칸의 값은 두 글자 간 최소 변환 거리
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i-1) == s2.charAt(j-1)) {
                    dp[i][j] = dp[i-1][j-1];
                } else {
                    dp[i][j] = Math.min(Math.min(dp[i-1][j], dp[i][j-1]), dp[i-1][j-1]) + 1;
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }
}

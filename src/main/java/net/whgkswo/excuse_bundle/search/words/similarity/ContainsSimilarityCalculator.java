package net.whgkswo.excuse_bundle.search.words.similarity;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ContainsSimilarityCalculator{

    public Similarity calculateSimilarity(String target, String searchInput, double floorSimilarityScore) {
        String targetLower = target.toLowerCase();
        String searchLower = searchInput.toLowerCase();

        if (targetLower.equals(searchLower)) {
            return Similarity.of(List.of(target), 1.0);
        } else if (targetLower.contains(searchLower)) {

            double ratio = (double) searchInput.length() / target.length();
            // 최소 임계값 보장 + 임계값 초과분 안에서 길이로 줄세우기
            double similarity = floorSimilarityScore + (1 - floorSimilarityScore) * ratio;
            return Similarity.of(List.of(searchInput), similarity);
        }
        return Similarity.NO_MATCH;
    }
}

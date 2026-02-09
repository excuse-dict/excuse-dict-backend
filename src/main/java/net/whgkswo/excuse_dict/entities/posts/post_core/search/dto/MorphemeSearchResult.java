package net.whgkswo.excuse_dict.entities.posts.post_core.search.dto;

import java.util.List;
import java.util.Map;

public record MorphemeSearchResult(Map<Long, Double> postScoreMap, Map<Long, List<String>> matchedWords) {
    public static MorphemeSearchResult empty() {
        return new MorphemeSearchResult(Map.of(), Map.of());
    }

    public boolean isEmpty() {
        return postScoreMap.isEmpty();
    }
}

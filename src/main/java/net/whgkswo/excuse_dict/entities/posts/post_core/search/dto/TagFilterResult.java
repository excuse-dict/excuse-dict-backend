package net.whgkswo.excuse_dict.entities.posts.post_core.search.dto;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record TagFilterResult(Set<Long> postIds, Map<Long, List<String>> matchedTags) {
    boolean isEmpty() {
        return postIds.isEmpty();
    }
}

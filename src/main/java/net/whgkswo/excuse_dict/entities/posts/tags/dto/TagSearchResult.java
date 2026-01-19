package net.whgkswo.excuse_dict.entities.posts.tags.dto;

import net.whgkswo.excuse_dict.entities.posts.tags.entity.Tag;

public record TagSearchResult(
        Tag tag,
        double score
) {
}

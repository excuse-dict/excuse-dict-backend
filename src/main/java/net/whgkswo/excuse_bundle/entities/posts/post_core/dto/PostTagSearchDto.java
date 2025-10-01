package net.whgkswo.excuse_bundle.entities.posts.post_core.dto;

import net.whgkswo.excuse_bundle.search.Searchable;

import java.util.Set;

public record PostTagSearchDto(
        long id,
        Set<String> tags
) implements Searchable {
}

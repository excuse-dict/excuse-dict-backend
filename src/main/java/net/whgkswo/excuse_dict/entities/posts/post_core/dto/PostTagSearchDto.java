package net.whgkswo.excuse_dict.entities.posts.post_core.dto;

import net.whgkswo.excuse_dict.search.Searchable;

import java.util.Set;

public record PostTagSearchDto(
        long id,
        Set<String> tags
) implements Searchable {
}

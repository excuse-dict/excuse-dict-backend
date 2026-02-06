package net.whgkswo.excuse_dict.search.dto;

import net.whgkswo.excuse_dict.search.Searchable;

import java.util.Set;

public record PostTagSearchDto(
        long id,
        Set<String> tags
) implements Searchable {
}

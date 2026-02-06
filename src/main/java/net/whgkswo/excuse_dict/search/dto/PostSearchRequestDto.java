package net.whgkswo.excuse_dict.search.dto;

import jakarta.annotation.Nullable;
import net.whgkswo.excuse_dict.search.SearchType;

import java.util.Collections;
import java.util.List;

public record PostSearchRequestDto(
        @Nullable Integer page,
        @Nullable Integer size,
        @Nullable String searchInput,
        @Nullable SearchType searchType,
        @Nullable List<String> includedTags,
        @Nullable List<String> excludedTags
) {

    public static int DEFAULT_PAGE = 0;
    public static int DEFAULT_SIZE = 10;

    public int pageOrDefault() {
        return page == null ? DEFAULT_PAGE : page;
    }

    public int sizeOrDefault() {
        return size == null ? DEFAULT_SIZE : size;
    }

    public List<String> includedTagsOrEmpty() {
        return includedTags == null ? Collections.emptyList() : includedTags;
    }

    public List<String> excludedTagsOrEmpty() {
        return excludedTags == null ? Collections.emptyList() : excludedTags;
    }
}

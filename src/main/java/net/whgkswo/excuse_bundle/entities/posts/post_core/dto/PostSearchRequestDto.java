package net.whgkswo.excuse_bundle.entities.posts.post_core.dto;

import jakarta.annotation.Nullable;
import net.whgkswo.excuse_bundle.search.SearchType;

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

    public int pageOrDefault() {
        return page == null ? 0 : page;
    }

    public int sizeOrDefault() {
        return size == null ? 10 : size;
    }

    public List<String> includedTagsOrEmpty() {
        return includedTags == null ? Collections.emptyList() : includedTags;
    }

    public List<String> excludedTagsOrEmpty() {
        return excludedTags == null ? Collections.emptyList() : excludedTags;
    }
}

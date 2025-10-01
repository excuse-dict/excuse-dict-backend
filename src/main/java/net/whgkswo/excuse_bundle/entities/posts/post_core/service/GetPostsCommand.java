package net.whgkswo.excuse_bundle.entities.posts.post_core.service;

import net.whgkswo.excuse_bundle.search.SearchType;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;

public record GetPostsCommand(
        Pageable pageable,
        @Nullable String searchInput,
        @Nullable Long memberId,
        @Nullable SearchType searchType,
        @NonNull List<String> includedTags,
        @NonNull List<String> excludedTags
) {
}

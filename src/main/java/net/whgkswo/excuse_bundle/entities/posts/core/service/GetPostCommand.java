package net.whgkswo.excuse_bundle.entities.posts.core.service;

import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;

public record GetPostCommand(
        Pageable pageable,
        @Nullable String searchInput
) {
}

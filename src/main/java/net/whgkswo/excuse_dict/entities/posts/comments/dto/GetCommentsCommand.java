package net.whgkswo.excuse_dict.entities.posts.comments.dto;

import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;

public record GetCommentsCommand(
        long postId,
        @Nullable Long memberId,
        Pageable pageable
) {
}

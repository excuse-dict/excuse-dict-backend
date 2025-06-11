package net.whgkswo.excuse_bundle.entities.posts.comments.dto;

import org.springframework.lang.Nullable;

public record GetCommentsCommand(
        long postId,
        @Nullable Long memberId,
        int page,
        int size
) {
}

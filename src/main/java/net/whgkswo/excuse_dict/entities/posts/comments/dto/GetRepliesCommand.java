package net.whgkswo.excuse_dict.entities.posts.comments.dto;


import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;

public record GetRepliesCommand(
        long commentId,
        @Nullable Long memberId,
        Pageable pageable
) {
}

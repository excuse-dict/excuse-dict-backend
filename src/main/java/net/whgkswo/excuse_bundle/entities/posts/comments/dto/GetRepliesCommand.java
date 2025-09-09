package net.whgkswo.excuse_bundle.entities.posts.comments.dto;


import org.springframework.data.domain.Pageable;

public record GetRepliesCommand(
        long commentId,
        Pageable pageable
) {
}

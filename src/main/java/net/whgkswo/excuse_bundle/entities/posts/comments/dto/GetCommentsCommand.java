package net.whgkswo.excuse_bundle.entities.posts.comments.dto;

public record GetCommentsCommand(
        long postId,
        int page,
        int size
) {
}

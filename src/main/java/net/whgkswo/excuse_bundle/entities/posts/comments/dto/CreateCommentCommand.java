package net.whgkswo.excuse_bundle.entities.posts.comments.dto;

public record CreateCommentCommand(
        long postId,
        long memberId,
        String content
) {
}

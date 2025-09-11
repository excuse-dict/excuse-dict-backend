package net.whgkswo.excuse_bundle.entities.posts.comments.dto;

public record CreateOrUpdateCommentCommand(
        long parentContentId,
        long memberId,
        String content
) {
}

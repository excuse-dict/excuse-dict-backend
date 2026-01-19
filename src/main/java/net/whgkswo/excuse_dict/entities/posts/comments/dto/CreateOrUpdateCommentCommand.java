package net.whgkswo.excuse_dict.entities.posts.comments.dto;

public record CreateOrUpdateCommentCommand(
        long parentContentId,
        long memberId,
        String content
) {
}

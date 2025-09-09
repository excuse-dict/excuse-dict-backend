package net.whgkswo.excuse_bundle.entities.posts.comments.dto;

import net.whgkswo.excuse_bundle.entities.members.core.entitiy.Member;

public record CreateCommentCommand(
        long parentContentId,
        long memberId,
        String content
) {
}

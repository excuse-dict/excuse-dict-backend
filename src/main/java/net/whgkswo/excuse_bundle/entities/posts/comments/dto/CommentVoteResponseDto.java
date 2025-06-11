package net.whgkswo.excuse_bundle.entities.posts.comments.dto;

import net.whgkswo.excuse_bundle.responses.dtos.Dto;

public record CommentVoteResponseDto(
        long commentId,
        long memberId
) implements Dto {
}

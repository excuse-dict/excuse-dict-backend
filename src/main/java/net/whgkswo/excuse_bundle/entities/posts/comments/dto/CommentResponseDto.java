package net.whgkswo.excuse_bundle.entities.posts.comments.dto;

import net.whgkswo.excuse_bundle.entities.members.core.dto.MemberResponseDto;
import net.whgkswo.excuse_bundle.responses.dtos.Dto;

public record CommentResponseDto(
        boolean isReply,
        MemberResponseDto member
) implements Dto {
}

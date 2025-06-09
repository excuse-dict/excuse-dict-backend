package net.whgkswo.excuse_bundle.entities.posts.core.dto;

import net.whgkswo.excuse_bundle.entities.excuses.dto.ExcuseResponseDto;
import net.whgkswo.excuse_bundle.entities.members.core.dto.MemberResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.comments.dto.CommentResponseDto;
import net.whgkswo.excuse_bundle.responses.dtos.Dto;

import java.time.LocalDateTime;
import java.util.List;

public record MultiPostResponseDto(
        long postId,
        MemberResponseDto author,
        ExcuseResponseDto excuse,
        int upvoteCount,
        int downvoteCount,
        int commentCount,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) implements Dto {
}

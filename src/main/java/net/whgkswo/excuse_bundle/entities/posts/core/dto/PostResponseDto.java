package net.whgkswo.excuse_bundle.entities.posts.core.dto;

import net.whgkswo.excuse_bundle.entities.excuses.dto.ExcuseResponseDto;
import net.whgkswo.excuse_bundle.entities.members.core.dto.MemberResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.comments.dto.CommentResponseDto;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponseDto(
        long postId,
        MemberResponseDto author,
        ExcuseResponseDto excuse,
        int upvoteCount,
        int downvoteCount,
        List<CommentResponseDto> comments,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
}

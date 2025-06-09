package net.whgkswo.excuse_bundle.entities.posts.core.dto;

import net.whgkswo.excuse_bundle.entities.excuses.dto.ExcuseResponseDto;
import net.whgkswo.excuse_bundle.entities.members.core.dto.MemberResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.core.entity.PostVote;

import java.time.LocalDateTime;

public record MultiPostSummaryResponseDto(
        long postId,
        MemberResponseDto author,
        ExcuseResponseDto excuse,
        int upvoteCount,
        int downvoteCount,
        int commentCount,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
}

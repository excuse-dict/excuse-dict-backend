package net.whgkswo.excuse_bundle.entities.posts.comments.dto;

import net.whgkswo.excuse_bundle.entities.members.core.dto.MemberResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.comments.entity.ReplyVoteDto;

import java.time.LocalDateTime;

public record ReplyResponseDto(
        long id,
        String content,
        MemberResponseDto author,
        int upvoteCount,
        int downvoteCount,
        int replyCount,
        ReplyVoteDto myVote,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
}

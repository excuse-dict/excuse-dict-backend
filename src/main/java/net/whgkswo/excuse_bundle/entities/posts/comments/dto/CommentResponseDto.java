package net.whgkswo.excuse_bundle.entities.posts.comments.dto;

import net.whgkswo.excuse_bundle.entities.members.core.dto.MemberResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.comments.entity.CommentVoteDto;
import net.whgkswo.excuse_bundle.responses.dtos.Dto;

import java.time.LocalDateTime;

public record CommentResponseDto(
        long id,
        boolean isReply,
        String content,
        MemberResponseDto author,
        int upvoteCount,
        int downvoteCount,
        int replyCount,
        CommentVoteDto myVote,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) implements Dto {
}

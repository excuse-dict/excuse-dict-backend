package net.whgkswo.excuse_dict.entities.posts.comments.entity;

import net.whgkswo.excuse_dict.entities.vote.entity.VoteType;

public record CommentVoteDto(
        long commentId,
        long memberId,
        VoteType voteType
) {
}

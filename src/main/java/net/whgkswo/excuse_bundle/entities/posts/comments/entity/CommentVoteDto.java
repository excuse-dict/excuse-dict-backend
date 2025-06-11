package net.whgkswo.excuse_bundle.entities.posts.comments.entity;

import net.whgkswo.excuse_bundle.entities.vote.entity.VoteType;

public record CommentVoteDto(
        long commentId,
        long memberId,
        VoteType voteType
) {
}

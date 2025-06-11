package net.whgkswo.excuse_bundle.entities.posts.comments.dto;

import net.whgkswo.excuse_bundle.entities.vote.entity.VoteType;

public record CommentVoteCommand(
        long commentId,
        long memberId,
        VoteType voteType
) {
}

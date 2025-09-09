package net.whgkswo.excuse_bundle.entities.posts.comments.entity;

import net.whgkswo.excuse_bundle.entities.vote.entity.VoteType;

public record ReplyVoteDto(
        long replyId,
        long memberId,
        VoteType voteType
) {
}

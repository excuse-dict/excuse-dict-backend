package net.whgkswo.excuse_bundle.entities.posts.core.dto;

import net.whgkswo.excuse_bundle.entities.vote.entity.VoteType;

public record VoteCommand(
        long targetId,
        long memberId,
        VoteType voteType
) {
}

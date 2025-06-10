package net.whgkswo.excuse_bundle.entities.vote.dto;

import net.whgkswo.excuse_bundle.entities.vote.entity.VoteType;

public record PostVoteDto(
        VoteType voteType,
        long postId,
        long memberId
) {
}

package net.whgkswo.excuse_dict.entities.vote.dto;

import net.whgkswo.excuse_dict.entities.vote.entity.VoteType;

public record PostVoteDto(
        VoteType voteType,
        long postId,
        long memberId
) {
}

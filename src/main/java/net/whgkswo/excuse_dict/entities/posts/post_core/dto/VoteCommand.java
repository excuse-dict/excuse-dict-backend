package net.whgkswo.excuse_dict.entities.posts.post_core.dto;

import net.whgkswo.excuse_dict.entities.vote.entity.VoteType;

public record VoteCommand(
        long targetId,
        long memberId,
        VoteType voteType
) {
}

package net.whgkswo.excuse_bundle.entities.posts.comments.dto;

import net.whgkswo.excuse_bundle.entities.vote.entity.VoteType;

public record CommentVoteRequestDto(
        VoteType voteType
) {
}

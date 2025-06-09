package net.whgkswo.excuse_bundle.entities.vote.dto;

import net.whgkswo.excuse_bundle.entities.vote.entity.VoteType;
import net.whgkswo.excuse_bundle.responses.dtos.Dto;

public record VoteRequestDto(
        VoteType voteType
) implements Dto {
}

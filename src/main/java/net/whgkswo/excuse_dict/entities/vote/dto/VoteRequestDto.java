package net.whgkswo.excuse_dict.entities.vote.dto;

import net.whgkswo.excuse_dict.entities.vote.entity.VoteType;
import net.whgkswo.excuse_dict.general.responses.dtos.Dto;

public record VoteRequestDto(
        VoteType voteType
) implements Dto {
}

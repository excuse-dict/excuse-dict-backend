package net.whgkswo.excuse_dict.entities.members.core.dto;

import net.whgkswo.excuse_dict.entities.members.rank.MemberRank;
import net.whgkswo.excuse_dict.general.responses.dtos.Dto;

public record MemberResponseDto(
        long id,
        String nickname,
        MemberRank.Type rank
) implements Dto {
}

package net.whgkswo.excuse_bundle.entities.members.core.dto;

import net.whgkswo.excuse_bundle.entities.members.rank.MemberRank;
import net.whgkswo.excuse_bundle.general.responses.dtos.Dto;

public record MemberResponseDto(
        long id,
        String nickname,
        MemberRank.Type rank
) implements Dto {
}

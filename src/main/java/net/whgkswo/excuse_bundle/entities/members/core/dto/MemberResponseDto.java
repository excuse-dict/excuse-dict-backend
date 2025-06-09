package net.whgkswo.excuse_bundle.entities.members.core.dto;

import net.whgkswo.excuse_bundle.entities.members.rank.MemberRank;
import net.whgkswo.excuse_bundle.responses.dtos.Dto;

public record MemberResponseDto(
        long id,
        String nickname,
        MemberRank rank
) implements Dto {
}

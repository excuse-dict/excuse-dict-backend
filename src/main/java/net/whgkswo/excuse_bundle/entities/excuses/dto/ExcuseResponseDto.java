package net.whgkswo.excuse_bundle.entities.excuses.dto;

import net.whgkswo.excuse_bundle.entities.posts.tags.dto.TagResponseDto;
import net.whgkswo.excuse_bundle.general.responses.dtos.Dto;

import java.util.Set;

public record ExcuseResponseDto(
        String situation,
        String excuse,
        Set<TagResponseDto> tags
) implements Dto {
}

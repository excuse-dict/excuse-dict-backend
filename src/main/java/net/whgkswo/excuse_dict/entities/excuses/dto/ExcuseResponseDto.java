package net.whgkswo.excuse_dict.entities.excuses.dto;

import net.whgkswo.excuse_dict.entities.posts.tags.dto.TagResponseDto;
import net.whgkswo.excuse_dict.general.responses.dtos.Dto;

import java.util.Set;

public record ExcuseResponseDto(
        String situation,
        String excuse,
        Set<TagResponseDto> tags
) implements Dto {
}

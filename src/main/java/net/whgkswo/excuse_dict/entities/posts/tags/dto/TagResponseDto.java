package net.whgkswo.excuse_dict.entities.posts.tags.dto;


import net.whgkswo.excuse_dict.general.responses.dtos.Dto;

public record TagResponseDto(
        String category,
        String value,
        int popularity
) implements Dto {
}

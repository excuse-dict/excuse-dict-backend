package net.whgkswo.excuse_bundle.entities.posts.tags.dto;


import net.whgkswo.excuse_bundle.general.responses.dtos.Dto;

public record TagResponseDto(
        String category,
        String value,
        int popularity
) implements Dto {
}

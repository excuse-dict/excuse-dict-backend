package net.whgkswo.excuse_bundle.entities.posts.tags.dtos;

import net.whgkswo.excuse_bundle.entities.posts.tags.entities.Tag;
import net.whgkswo.excuse_bundle.responses.dtos.Dto;

import java.util.List;

public record TagResponseDto(
        List<Tag> tags
) implements Dto {
}

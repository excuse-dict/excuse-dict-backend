package net.whgkswo.excuse_bundle.entities.posts.tags.dtos;

import net.whgkswo.excuse_bundle.entities.posts.tags.entities.Tag;
import net.whgkswo.excuse_bundle.responses.dtos.Dto;
import net.whgkswo.excuse_bundle.responses.page.PageInfo;
import org.springframework.data.domain.Page;

public record TagResponseDto(
        Page<Tag> tags,
        PageInfo pageInfo
) implements Dto {
}

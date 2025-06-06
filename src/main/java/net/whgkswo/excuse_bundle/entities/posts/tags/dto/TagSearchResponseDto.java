package net.whgkswo.excuse_bundle.entities.posts.tags.dto;

import net.whgkswo.excuse_bundle.entities.posts.tags.entity.Tag;
import net.whgkswo.excuse_bundle.responses.dtos.Dto;
import net.whgkswo.excuse_bundle.responses.page.PageInfo;
import org.springframework.data.domain.Page;

public record TagSearchResponseDto(
        Page<Tag> tags,
        PageInfo pageInfo
) implements Dto {
}

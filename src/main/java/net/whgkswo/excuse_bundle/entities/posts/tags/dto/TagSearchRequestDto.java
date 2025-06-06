package net.whgkswo.excuse_bundle.entities.posts.tags.dto;

import jakarta.annotation.Nullable;
import net.whgkswo.excuse_bundle.entities.posts.tags.entity.Tag;
import net.whgkswo.excuse_bundle.responses.dtos.Dto;

import java.util.List;

public record TagSearchRequestDto(
        @Nullable List<Tag.Category> categories,
        @Nullable String searchValue,
        @Nullable Integer page,
        @Nullable Integer size
) implements Dto {

    public Integer pageOrDefault(){
        return page == null ? 1 : page;
    }

    public Integer sizeOrDefault(){
        return size == null ? 20 : size;
    }
}

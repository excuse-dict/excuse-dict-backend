package net.whgkswo.excuse_dict.entities.posts.tags.mapper;

import net.whgkswo.excuse_dict.entities.posts.tags.dto.TagResponseDto;
import net.whgkswo.excuse_dict.entities.posts.tags.entity.Tag;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TagMapper {

    TagResponseDto tagToTagResponseDto(Tag tag);
}

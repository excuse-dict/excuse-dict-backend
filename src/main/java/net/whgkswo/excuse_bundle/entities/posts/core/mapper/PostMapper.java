package net.whgkswo.excuse_bundle.entities.posts.core.mapper;

import net.whgkswo.excuse_bundle.entities.excuses.mapper.ExcuseMapper;
import net.whgkswo.excuse_bundle.entities.posts.comments.CommentMapper;
import net.whgkswo.excuse_bundle.entities.posts.core.dto.PostResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.core.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {CommentMapper.class, ExcuseMapper.class})
public interface PostMapper {

    @Mapping(target = "author", source = "member.nickname")
    PostResponseDto postToPostResponseDto(Post post);

    default List<PostResponseDto> postsToPostResponseDtos(List<Post> posts){
        return posts.stream()
                .map(this::postToPostResponseDto)
                .collect(Collectors.toList());
    }

    default Page<PostResponseDto> postsToPostResponseDtos(Page<Post> posts){
        return posts.map(this::postToPostResponseDto);
    }
}

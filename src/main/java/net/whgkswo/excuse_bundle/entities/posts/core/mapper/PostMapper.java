package net.whgkswo.excuse_bundle.entities.posts.core.mapper;

import net.whgkswo.excuse_bundle.entities.posts.core.dto.PostResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.core.entity.Post;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface PostMapper {

    default PostResponseDto postToPostResponseDto(Post post){
        return new PostResponseDto(
                post.getMember().getNickname(),
                post.getExcuse().getSituation(),
                post.getExcuse().getExcuse(),
                post.getUpvoteCount(),
                post.getDownvoteCount(),
                post.getCreatedAt(),
                post.getModifiedAt()
        );
    }

    default List<PostResponseDto> postsToPostResponseDtos(List<Post> posts){
        return posts.stream()
                .map(this::postToPostResponseDto)
                .collect(Collectors.toList());
    }

    default Page<PostResponseDto> postsToPostResponseDtos(Page<Post> posts){
        return posts.map(this::postToPostResponseDto);
    }
}

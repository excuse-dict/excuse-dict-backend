package net.whgkswo.excuse_bundle.entities.posts.core.mapper;

import net.whgkswo.excuse_bundle.entities.excuses.mapper.ExcuseMapper;
import net.whgkswo.excuse_bundle.entities.members.core.mapper.MemberMapper;
import net.whgkswo.excuse_bundle.entities.posts.comments.CommentMapper;
import net.whgkswo.excuse_bundle.entities.posts.comments.entity.Comment;
import net.whgkswo.excuse_bundle.entities.posts.core.dto.MultiPostResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.core.dto.SinglePostResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.core.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {CommentMapper.class, ExcuseMapper.class, MemberMapper.class})
public interface PostMapper {

    @Mapping(target = "author", source = "member")
    SinglePostResponseDto postToPostResponseDto(Post post);

    @Mapping(target = "author", source = "member")
    @Mapping(target = "commentCount", source = "comments", qualifiedByName = "commentsToCount") // 변환기 적용
    MultiPostResponseDto multiPostResponseDto(Post post);

    default Page<SinglePostResponseDto> postsToSinglePostResponseDtos(Page<Post> posts){
        return posts.map(this::postToPostResponseDto);
    }

    default Page<MultiPostResponseDto> postsToMultiPostResponseDtos(Page<Post> posts){
        return posts.map(this::multiPostResponseDto);
    }

    // 댓글 리스트 -> 카운트 변환기
    @Named("commentsToCount")
    default int commentsToCount(List<Comment> comments) {
        return comments != null ? comments.size() : 0;
    }
}

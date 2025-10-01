package net.whgkswo.excuse_bundle.entities.posts.post_core.mapper;

import jakarta.annotation.Nullable;
import net.whgkswo.excuse_bundle.entities.excuses.mapper.ExcuseMapper;
import net.whgkswo.excuse_bundle.entities.members.core.mapper.MemberMapper;
import net.whgkswo.excuse_bundle.entities.posts.comments.entity.AbstractComment;
import net.whgkswo.excuse_bundle.entities.posts.comments.mapper.CommentMapper;
import net.whgkswo.excuse_bundle.entities.posts.comments.entity.Comment;
import net.whgkswo.excuse_bundle.entities.posts.post_core.dto.PostResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.post_core.dto.PostSummaryResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.post_core.dto.WeeklyTopPostResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.post_core.entity.Post;
import net.whgkswo.excuse_bundle.entities.vote.dto.PostVoteDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {CommentMapper.class, ExcuseMapper.class, MemberMapper.class})
public interface PostMapper {

    @Mapping(target = "postId", source = "id")
    @Mapping(target = "author", source = "member")
    @Mapping(target = "commentCount", source = "comments", qualifiedByName = "commentsToCount") // 변환기 적용
    PostSummaryResponseDto postTomultiPostSummaryResponseDto(Post post);

    default Page<PostSummaryResponseDto> postsToMultiPostSummaryResponseDtos(Page<Post> posts){
        return posts.map(this::postTomultiPostSummaryResponseDto);
    }

    default List<PostSummaryResponseDto> postsToMultiPostSummaryResponseDtos(List<Post> posts){
        return posts.stream()
                .map(this::postTomultiPostSummaryResponseDto)
                .toList();
    }

    // 댓글 리스트 -> 카운트 변환기
    @Named("commentsToCount")
    default int commentsToCount(List<Comment> comments) {
        if(comments == null) return 0;

        return (int) comments.stream()
                .filter(comment -> comment.getStatus().equals(AbstractComment.Status.ACTIVE))
                .count();
    }

    default PostResponseDto postSummaryResponseDtoToPostResponseDto(
            PostSummaryResponseDto summary,
            @Nullable PostVoteDto vote,
            List<String> matchedWords,
            List<String> matchedTags
    ){

        return new PostResponseDto(
                summary.getPostId(),
                summary.getAuthor(),
                summary.getExcuse(),
                summary.getUpvoteCount(),
                summary.getDownvoteCount(),
                summary.getCommentCount(),
                summary.getCreatedAt(),
                summary.getModifiedAt(),
                vote,
                matchedWords,
                matchedTags
                );
    }

    WeeklyTopPostResponseDto postResponseDtoToWeeklyTopPostResponseDto(PostResponseDto dto, int hotScore);
}

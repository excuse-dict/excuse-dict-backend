package net.whgkswo.excuse_bundle.entities.posts.core.mapper;

import net.whgkswo.excuse_bundle.entities.excuses.mapper.ExcuseMapper;
import net.whgkswo.excuse_bundle.entities.members.core.mapper.MemberMapper;
import net.whgkswo.excuse_bundle.entities.posts.comments.entity.AbstractComment;
import net.whgkswo.excuse_bundle.entities.posts.comments.mapper.CommentMapper;
import net.whgkswo.excuse_bundle.entities.posts.comments.entity.Comment;
import net.whgkswo.excuse_bundle.entities.posts.core.dto.PostResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.core.dto.PostSummaryResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.core.dto.WeeklyTopPostResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.core.entity.Post;
import net.whgkswo.excuse_bundle.entities.vote.dto.PostVoteDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {CommentMapper.class, ExcuseMapper.class, MemberMapper.class})
public interface PostMapper {

    @Mapping(target = "postId", source = "id")
    @Mapping(target = "author", source = "member")
    @Mapping(target = "commentCount", source = "comments", qualifiedByName = "commentsToCount") // 변환기 적용
    PostSummaryResponseDto postTomultiPostSummaryResponseDto(Post post);

    default Page<PostSummaryResponseDto> postsToMultiPostResponseDtos(Page<Post> posts){
        return posts.map(this::postTomultiPostSummaryResponseDto);
    }

    default List<PostSummaryResponseDto> postsToMultiPostResponseDtos(List<Post> posts){
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

    default PostResponseDto postSummaryResponseDtoToPostResponseDto(PostSummaryResponseDto summary, Optional<PostVoteDto> optionalVote){

        return new PostResponseDto(
                summary.getPostId(),
                summary.getAuthor(),
                summary.getExcuse(),
                summary.getUpvoteCount(),
                summary.getDownvoteCount(),
                summary.getCommentCount(),
                summary.getCreatedAt(),
                summary.getModifiedAt(),
                optionalVote.orElse(null)
                );
    }

    WeeklyTopPostResponseDto postResponseDtoToWeeklyTopPostResponseDto(PostResponseDto dto, int hotScore);
}

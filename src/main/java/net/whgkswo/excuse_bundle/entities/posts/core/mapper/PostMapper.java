package net.whgkswo.excuse_bundle.entities.posts.core.mapper;

import net.whgkswo.excuse_bundle.entities.excuses.mapper.ExcuseMapper;
import net.whgkswo.excuse_bundle.entities.members.core.mapper.MemberMapper;
import net.whgkswo.excuse_bundle.entities.posts.comments.CommentMapper;
import net.whgkswo.excuse_bundle.entities.posts.comments.entity.Comment;
import net.whgkswo.excuse_bundle.entities.posts.core.dto.MultiPostResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.core.dto.MultiPostSummaryResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.core.dto.SinglePostResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.core.entity.Post;
import net.whgkswo.excuse_bundle.entities.posts.core.entity.PostVote;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {CommentMapper.class, ExcuseMapper.class, MemberMapper.class})
public interface PostMapper {

    @Mapping(target = "author", source = "member")
    SinglePostResponseDto postToPostResponseDto(Post post);

    @Mapping(target = "postId", source = "id")
    @Mapping(target = "author", source = "member")
    @Mapping(target = "commentCount", source = "comments", qualifiedByName = "commentsToCount") // 변환기 적용
    MultiPostSummaryResponseDto postTomultiPostSummaryResponseDto(Post post);

    default Page<SinglePostResponseDto> postsToSinglePostResponseDtos(Page<Post> posts){
        return posts.map(this::postToPostResponseDto);
    }

    default Page<MultiPostSummaryResponseDto> postsToMultiPostResponseDtos(Page<Post> posts){
        return posts.map(this::postTomultiPostSummaryResponseDto);
    }

    // 댓글 리스트 -> 카운트 변환기
    @Named("commentsToCount")
    default int commentsToCount(List<Comment> comments) {
        return comments != null ? comments.size() : 0;
    }

    default MultiPostResponseDto summaryToMultiPostResponseDto(MultiPostSummaryResponseDto summary, Post post, Optional<PostVote> optionalVote){

        return new MultiPostResponseDto(
                summary.postId(),
                summary.author(),
                summary.excuse(),
                summary.upvoteCount(),
                summary.downvoteCount(),
                optionalVote.orElse(null),
                summary.commentCount(),
                summary.createdAt(),
                summary.modifiedAt()
        );
    }
}

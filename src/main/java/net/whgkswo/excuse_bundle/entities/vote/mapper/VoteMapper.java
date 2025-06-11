package net.whgkswo.excuse_bundle.entities.vote.mapper;

import net.whgkswo.excuse_bundle.entities.posts.comments.dto.CommentVoteResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.comments.entity.CommentVote;
import net.whgkswo.excuse_bundle.entities.posts.comments.dto.CommentVoteRequestDto;
import net.whgkswo.excuse_bundle.entities.posts.core.entity.PostVote;
import net.whgkswo.excuse_bundle.entities.vote.dto.PostVoteDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VoteMapper {

    @Mapping(target = "postId", source = "post.id")
    @Mapping(target = "memberId", source = "member.id")
    PostVoteDto postVoteToPostVoteDto(PostVote postVote);

    @Mapping(target = "commentId", source = "comment.id")
    @Mapping(target = "memberId", source = "member.id")
    CommentVoteResponseDto commentToCommentVoteResponseDto(CommentVote commentVote);
}

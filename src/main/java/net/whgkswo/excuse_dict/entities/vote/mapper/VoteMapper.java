package net.whgkswo.excuse_dict.entities.vote.mapper;

import net.whgkswo.excuse_dict.entities.posts.comments.entity.CommentVote;
import net.whgkswo.excuse_dict.entities.posts.comments.entity.CommentVoteDto;
import net.whgkswo.excuse_dict.entities.posts.comments.entity.ReplyVoteDto;
import net.whgkswo.excuse_dict.entities.posts.comments.reply.entity.ReplyVote;
import net.whgkswo.excuse_dict.entities.posts.post_core.entity.PostVote;
import net.whgkswo.excuse_dict.entities.vote.dto.PostVoteDto;
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
    CommentVoteDto commentVoteToCommentVoteDto(CommentVote commentVote);

    @Mapping(target = "replyId", source = "reply.id")
    @Mapping(target = "memberId", source = "member.id")
    ReplyVoteDto replyVoteToReplyVoteDto(ReplyVote replyVote);
}

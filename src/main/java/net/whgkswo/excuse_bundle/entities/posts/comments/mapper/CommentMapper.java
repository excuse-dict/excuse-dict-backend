package net.whgkswo.excuse_bundle.entities.posts.comments.mapper;

import net.whgkswo.excuse_bundle.entities.members.core.mapper.MemberMapper;
import net.whgkswo.excuse_bundle.entities.posts.comments.dto.CommentResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.comments.dto.ReplyResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.comments.entity.Comment;
import net.whgkswo.excuse_bundle.entities.posts.comments.entity.CommentVoteDto;
import net.whgkswo.excuse_bundle.entities.posts.comments.reply.entity.Reply;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {MemberMapper.class})
public interface CommentMapper {

    @Mapping(target = "author", source = "comment.member")
    @Mapping(target = "replyCount", source = "comment.replies", qualifiedByName = "repliesToCount")
    CommentResponseDto commentToCommentResponseDto(Comment comment, CommentVoteDto myVote);

    @Named("repliesToCount")
    default int repliesToCount(List<Reply> replies){
        return replies == null ? 0 : replies.size();
    }
}

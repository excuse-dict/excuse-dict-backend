package net.whgkswo.excuse_bundle.entities.posts.comments.reply.mapper;

import net.whgkswo.excuse_bundle.entities.members.core.mapper.MemberMapper;
import net.whgkswo.excuse_bundle.entities.posts.comments.dto.ReplyResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.comments.entity.ReplyVoteDto;
import net.whgkswo.excuse_bundle.entities.posts.comments.reply.entity.Reply;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = MemberMapper.class)
public interface ReplyMapper {

    @Mapping(target = "author", source = "reply.member")
    ReplyResponseDto replyToReplyResponseDto(Reply reply, ReplyVoteDto myVote);
}

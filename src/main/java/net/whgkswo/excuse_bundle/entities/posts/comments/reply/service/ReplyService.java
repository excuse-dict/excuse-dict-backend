package net.whgkswo.excuse_bundle.entities.posts.comments.reply.service;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.entities.members.core.entitiy.Member;
import net.whgkswo.excuse_bundle.entities.members.core.service.MemberService;
import net.whgkswo.excuse_bundle.entities.posts.comments.dto.CreateOrUpdateCommentCommand;
import net.whgkswo.excuse_bundle.entities.posts.comments.dto.GetRepliesCommand;
import net.whgkswo.excuse_bundle.entities.posts.comments.dto.ReplyResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.comments.entity.AbstractComment;
import net.whgkswo.excuse_bundle.entities.posts.comments.entity.Comment;
import net.whgkswo.excuse_bundle.entities.posts.comments.entity.ReplyVoteDto;
import net.whgkswo.excuse_bundle.entities.posts.comments.reply.entity.Reply;
import net.whgkswo.excuse_bundle.entities.posts.comments.reply.entity.ReplyVote;
import net.whgkswo.excuse_bundle.entities.posts.comments.reply.mapper.ReplyMapper;
import net.whgkswo.excuse_bundle.entities.posts.comments.reply.repository.ReplyRepository;
import net.whgkswo.excuse_bundle.entities.posts.comments.repository.CommentRepository;
import net.whgkswo.excuse_bundle.entities.posts.comments.service.CommentService;
import net.whgkswo.excuse_bundle.entities.posts.core.dto.VoteCommand;
import net.whgkswo.excuse_bundle.entities.vote.mapper.VoteMapper;
import net.whgkswo.excuse_bundle.entities.vote.service.VoteService;
import net.whgkswo.excuse_bundle.exceptions.BusinessLogicException;
import net.whgkswo.excuse_bundle.exceptions.ExceptionType;
import net.whgkswo.excuse_bundle.general.dto.DeleteCommand;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReplyService {

    private final CommentRepository commentRepository;
    private final ReplyMapper replyMapper;
    private final ReplyRepository replyRepository;
    private final CommentService commentService;
    private final VoteMapper voteMapper;
    private final VoteService voteService;
    private final MemberService memberService;

    // 대댓글 작성, 댓글의 현재 답글수를 리턴
    @Transactional
    public int createReply(CreateOrUpdateCommentCommand command){
        Comment comment = commentService.getComment(command.parentContentId());

        // 답글 작성자
        Member replyAuthor = memberService.findById(command.memberId());

        Reply reply = new Reply();
        reply.setContent(command.content());
        reply.setComment(comment);
        reply.setMember(replyAuthor);

        commentRepository.save(comment);

        return comment.getReplies().size();
    }

    // 대댓글 조회(Optional)
    private Optional<Reply> findReplyWithFetch(long replyId){
        return replyRepository.findByReplyId(replyId);
    }

    // 대댓글 조회
    public Reply getReply(long replyId){
        return findReplyWithFetch(replyId).orElseThrow(() -> new BusinessLogicException(ExceptionType.REPLY_NOT_FOUND));
    }

    // 대댓글 리스트 조회
    public Page<ReplyResponseDto> getReplies(GetRepliesCommand command){
        Page<Reply> replies = replyRepository.findByCommentIdAndStatus(command.commentId(), AbstractComment.Status.ACTIVE, command.pageable());

        return replies.map(reply ->  {
            // 내가 누른 추천/비추천 있는지 조회
            ReplyVoteDto myVote = reply.getVotes().stream()
                    .filter(vote -> vote.getMember().getId().equals(command.memberId()))
                    .map(voteMapper::replyVoteToReplyVoteDto)
                    .findFirst()
                    .orElse(null);
            return replyMapper.replyToReplyResponseDto(reply, myVote);
        });
    }

    // 랜덤 답글 n개 조회
    public List<Reply> getRandomReplies(int amount){
        return replyRepository.findRandomReplies(amount);
    }

    // 랜덤 답글 n개 조회 (최근 m일간)
    public List<Reply> getRandomReplies(int amount, int maxDaysAgo){
        return replyRepository.findRandomReplies(amount, maxDaysAgo);
    }

    // 대댓글 추천/비추천
    public boolean voteToReplies(VoteCommand command){

        Reply reply = getReply(command.targetId());

        // 자추 불가
        // TODO: 주석 해제
        /*if(reply.getMember().getId() == command.memberId()) throw new BusinessLogicException(ExceptionType.SELF_VOTE_NOT_ALLOWED);*/

        // 이미 추천/비추천했는지
        Optional<ReplyVote> optionalVote = voteService.getReplyVoteFromCertainMember(reply, command.memberId());

        if(optionalVote.isPresent()){
            // 추천 비추천 취소
            ReplyVote vote = optionalVote.get();
            if(vote.getVoteType().equals(command.voteType())){ // 같은 타입일 때만 취소
                voteService.removeReplyVote(reply, vote);
                return false; // 취소됨
            }else{ // 추천 눌렀는데 취소 안하고 비추천 누르거나 그 반대
                throw new BusinessLogicException(ExceptionType.alreadyVoted(command.voteType()));
            }
        }else{
            // 추천 비추천 등록
            voteService.saveReplyVote(reply, command.voteType(), command.memberId());
            return true; // 생성됨
        }
    }

    // 답글 수정
    public void updateReply(CreateOrUpdateCommentCommand command){
        Reply reply = getReply(command.parentContentId());

        // 자기가 쓴 답글만 수정 가능
        if(!reply.getMember().getId().equals(command.memberId())) throw new BusinessLogicException(ExceptionType.UPDATE_FORBIDDEN);

        reply.setContent(command.content());
        reply.setModifiedAt(LocalDateTime.now());

        replyRepository.save(reply);
    }

    // 답글 삭제
    public void deleteReply(DeleteCommand command){
        Reply reply = getReply(command.targetId());

        // 자기가 쓴 댓글만 삭제 가능
        if(reply.getMember().getId() != command.memberId()) throw new BusinessLogicException(ExceptionType.DELETE_FORBIDDEN);

        reply.setStatus(AbstractComment.Status.DELETED);
        replyRepository.save(reply);
    }
}

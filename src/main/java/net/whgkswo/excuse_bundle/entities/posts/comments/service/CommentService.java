package net.whgkswo.excuse_bundle.entities.posts.comments.service;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.entities.members.core.entitiy.Member;
import net.whgkswo.excuse_bundle.entities.members.core.service.MemberService;
import net.whgkswo.excuse_bundle.entities.posts.comments.mapper.CommentMapper;
import net.whgkswo.excuse_bundle.entities.posts.comments.dto.*;
import net.whgkswo.excuse_bundle.entities.posts.comments.entity.AbstractComment;
import net.whgkswo.excuse_bundle.entities.posts.comments.entity.Comment;
import net.whgkswo.excuse_bundle.entities.posts.comments.entity.CommentVote;
import net.whgkswo.excuse_bundle.entities.posts.comments.entity.CommentVoteDto;
import net.whgkswo.excuse_bundle.entities.posts.comments.reply.entity.Reply;
import net.whgkswo.excuse_bundle.entities.posts.comments.reply.mapper.ReplyMapper;
import net.whgkswo.excuse_bundle.entities.posts.comments.reply.repository.ReplyRepository;
import net.whgkswo.excuse_bundle.entities.posts.comments.repository.CommentRepository;
import net.whgkswo.excuse_bundle.entities.posts.core.dto.VoteCommand;
import net.whgkswo.excuse_bundle.entities.posts.core.entity.Post;
import net.whgkswo.excuse_bundle.entities.posts.core.repository.PostRepository;
import net.whgkswo.excuse_bundle.entities.posts.core.service.PostService;
import net.whgkswo.excuse_bundle.entities.vote.mapper.VoteMapper;
import net.whgkswo.excuse_bundle.entities.vote.service.VoteService;
import net.whgkswo.excuse_bundle.exceptions.BusinessLogicException;
import net.whgkswo.excuse_bundle.exceptions.ExceptionType;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;
    private final PostService postService;
    private final MemberService memberService;
    private final PostRepository postRepository;
    private final VoteMapper voteMapper;
    private final CommentMapper commentMapper;
    private final VoteService voteService;
    private final ReplyMapper replyMapper;

    // 댓글 조회 (Optional)
    private Optional<Comment> findComment(long commentId){
        return commentRepository.findByCommentId(commentId);
    }

    // 댓글 조회
    public Comment getComment(long commentId){
        return findComment(commentId).orElseThrow(() -> new BusinessLogicException(ExceptionType.COMMENT_NOT_FOUND));
    }

    // 댓글 작성
    @Transactional
    public void createComment(CreateCommentCommand command){
        Post post = postService.getPost(command.parentContentId());
        Member member = memberService.findById(command.memberId());

        Comment comment = new Comment(post, member, command.content());
        post.getComments().add(comment);

        postRepository.save(post);
    }

    // 댓글 조회
    @Transactional(readOnly = true)
    public Page<CommentResponseDto> getComments(GetCommentsCommand command){

        Page<Comment> comments = postRepository.findCommentsByPostId(command.postId(), command.pageable());

        return comments.map(comment ->  {
            // 내가 누른 추천/비추천 있는지 조회
            CommentVoteDto myVote = comment.getVotes().stream()
                    .filter(vote -> vote.getMember().getId().equals(command.memberId()))
                    .map(voteMapper::commentToCommentVoteDto)
                    .findFirst()
                    .orElse(null);
            return commentMapper.commentToCommentResponseDto(comment, myVote);
        });
    }

    // 댓글 추천
    @Transactional
    public boolean voteToComment(VoteCommand command){
        Optional<Comment> optionalComment = commentRepository.findById(command.targetId());
        Comment comment = optionalComment.orElseThrow(() -> new BusinessLogicException(ExceptionType.POST_NOT_FOUND));

        // 자추 불가
        // TODO: 주석 해제
        /*if(post.getMember().getId().equals(command.memberId()))
            throw new BusinessLogicException(ExceptionType.SELF_VOTE_NOT_ALLOWED);*/

        // 이미 추천/비추천했는지
        Optional<CommentVote> optionalVote = voteService.getCommentVoteFromCertainMember(comment, command.memberId());
        if(optionalVote.isPresent()){
            // 추천 비추천 취소
            CommentVote vote = optionalVote.get();
            if(vote.getVoteType().equals(command.voteType())){ // 같은 타입일 때만 취소
                voteService.removeCommentVote(comment, vote);
                return false; // 취소됨
            }else{ // 추천 눌렀는데 취소 안하고 비추천 누르거나 그 반대
                throw new BusinessLogicException(ExceptionType.alreadyVoted(command.voteType()));
            }
        }else{
            // 추천 비추천 등록
            voteService.saveCommentVote(comment, command.voteType(), command.memberId());
            return true; // 생성됨
        }
    }

    // 대댓글 작성
    @Transactional
    public void createReply(CreateCommentCommand command){
        Comment comment = getComment(command.parentContentId());

        Reply reply = new Reply();
        reply.setContent(command.content());
        reply.setComment(comment);
        reply.setMember(comment.getMember());

        commentRepository.save(comment);
    }

    // 대댓글 리스트 조회
    public Page<ReplyResponseDto> getReplies(GetRepliesCommand command){
        Page<Reply> replies = replyRepository.findByCommentIdAndStatus(command.commentId(), AbstractComment.Status.ACTIVE, command.pageable());

        return replies.map(reply -> replyMapper.replyToReplyResponseDto(reply));
    }
}

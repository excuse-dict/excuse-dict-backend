package net.whgkswo.excuse_bundle.entities.posts.comments.service;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.entities.members.core.entitiy.Member;
import net.whgkswo.excuse_bundle.entities.members.core.service.MemberService;
import net.whgkswo.excuse_bundle.entities.posts.comments.entity.*;
import net.whgkswo.excuse_bundle.entities.posts.comments.mapper.CommentMapper;
import net.whgkswo.excuse_bundle.entities.posts.comments.dto.*;
import net.whgkswo.excuse_bundle.entities.posts.comments.repository.CommentRepository;
import net.whgkswo.excuse_bundle.entities.posts.core.dto.VoteCommand;
import net.whgkswo.excuse_bundle.entities.posts.core.entity.Post;
import net.whgkswo.excuse_bundle.entities.posts.core.repository.PostRepository;
import net.whgkswo.excuse_bundle.entities.posts.core.service.PostService;
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
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostService postService;
    private final MemberService memberService;
    private final PostRepository postRepository;
    private final VoteMapper voteMapper;
    private final CommentMapper commentMapper;
    private final VoteService voteService;

    // 댓글 조회 (Optional)
    private Optional<Comment> findCommentWithFetch(long commentId){
        return commentRepository.findByCommentId(commentId);
    }

    // 댓글 조회
    public Comment getComment(long commentId){
        Comment comment = findCommentWithFetch(commentId).orElseThrow(() -> new BusinessLogicException(ExceptionType.COMMENT_NOT_FOUND));

        // 삭제된 댓글
        if(comment.getStatus().equals(AbstractComment.Status.DELETED)) throw new BusinessLogicException(ExceptionType.DELETED_COMMENT);
        return comment;
    }

    // 댓글 작성
    @Transactional
    public void createComment(CreateOrUpdateCommentCommand command){
        Post post = postService.getPost(command.parentContentId());
        Member member = memberService.findById(command.memberId());

        Comment comment = new Comment(post, member, command.content());
        post.getComments().add(comment);

        postRepository.save(post);
    }

    // 댓글 조회(다수)
    @Transactional(readOnly = true)
    public Page<CommentResponseDto> getComments(GetCommentsCommand command){

        Page<Comment> comments = postRepository.findCommentsByPostId(command.postId(), command.pageable());

        return comments
                .map(comment ->  {
            // 내가 누른 추천/비추천 있는지 조회
            CommentVoteDto myVote = comment.getVotes().stream()
                    .filter(vote -> vote.getMember().getId().equals(command.memberId()))
                    .map(voteMapper::commentVoteToCommentVoteDto)
                    .findFirst()
                    .orElse(null);
            return commentMapper.commentToCommentResponseDto(comment, myVote);
        });
    }

    // 랜덤 댓글 n개 조회
    public List<Comment> getRandomComments(int amount){
        return commentRepository.findRandomComments(amount);
    }

    // 랜덤 댓글 n개 조회(최근 m일간)
    public List<Comment> getRandomComments(int amount, int maxDaysAgo){
        return commentRepository.findRandomComments(amount, maxDaysAgo);
    }

    // 댓글 추천
    @Transactional
    public boolean voteToComment(VoteCommand command){
        Comment comment = getComment(command.targetId());

        // 자추 불가
        if(comment.getMember().getId() == command.memberId())
            throw new BusinessLogicException(ExceptionType.SELF_VOTE_NOT_ALLOWED);

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

    // 댓글 수정
    public void updateComment(CreateOrUpdateCommentCommand command){
        Comment comment = getComment(command.parentContentId());

        // 자기가 쓴 댓글만 수정 가능
        if(!comment.getMember().getId().equals(command.memberId())) throw new BusinessLogicException(ExceptionType.UPDATE_FORBIDDEN);

        comment.setContent(command.content());
        comment.setModifiedAt(LocalDateTime.now());

        commentRepository.save(comment);
    }

    // 댓글 삭제
    public void deleteComment(DeleteCommand command){
        Comment comment = getComment(command.targetId());

        // 자기가 쓴 댓글만 삭제 가능
        if(comment.getMember().getId() != command.memberId()) throw new BusinessLogicException(ExceptionType.DELETE_FORBIDDEN);

        comment.setStatus(AbstractComment.Status.DELETED);
        commentRepository.save(comment);
    }
}

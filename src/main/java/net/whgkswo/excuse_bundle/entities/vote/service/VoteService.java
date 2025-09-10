package net.whgkswo.excuse_bundle.entities.vote.service;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.entities.members.core.entitiy.Member;
import net.whgkswo.excuse_bundle.entities.members.core.service.MemberService;
import net.whgkswo.excuse_bundle.entities.posts.comments.entity.Comment;
import net.whgkswo.excuse_bundle.entities.posts.comments.entity.CommentVote;
import net.whgkswo.excuse_bundle.entities.posts.comments.reply.entity.Reply;
import net.whgkswo.excuse_bundle.entities.posts.comments.reply.entity.ReplyVote;
import net.whgkswo.excuse_bundle.entities.posts.comments.reply.repository.ReplyRepository;
import net.whgkswo.excuse_bundle.entities.posts.comments.repository.CommentRepository;
import net.whgkswo.excuse_bundle.entities.posts.core.entity.Post;
import net.whgkswo.excuse_bundle.entities.posts.core.entity.PostVote;
import net.whgkswo.excuse_bundle.entities.posts.core.repository.PostRepository;
import net.whgkswo.excuse_bundle.entities.vote.entity.VoteType;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VoteService {
    private final MemberService memberService;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;

    // 게시물에 특정 유저가 추천/비추천을 눌렀는지 조회
    public Optional<PostVote> getPostVoteFromCertainMember(Post post, Long memberId) {
        if(memberId == null) return Optional.empty();

        return post.getVotes().stream()
                .filter(vote -> vote.getMember().getId().equals(memberId))
                .findFirst();
    }

    // 게시글 추천/비추천 취소 (이미 앞에서 post-myVote 관계 검증했을 때만 사용)
    public void removePostVote(Post post, PostVote vote){
        post.getVotes().remove(vote);
        if(vote.getVoteType() == VoteType.UPVOTE){
            post.setUpvoteCount(post.getUpvoteCount() - 1);
        }else{
            post.setDownvoteCount(post.getDownvoteCount() - 1);
        }

        postRepository.save(post);
    }

    // 게시글 추천/비추천 등록
    public void savePostVote(Post post, VoteType type, long memberId){
        Member member = memberService.findById(memberId);

        PostVote vote = new PostVote(type, post, member);
        post.addVote(vote);

        if(vote.getVoteType() == VoteType.UPVOTE){
            post.setUpvoteCount(post.getUpvoteCount() + 1);
        }else{
            post.setDownvoteCount(post.getDownvoteCount() + 1);
        }

        postRepository.save(post);
    }

    // 댓글에 특정 유저가 추천/비추천을 눌렀는지 조회
    public Optional<CommentVote> getCommentVoteFromCertainMember(Comment comment, Long memberId) {
        if(memberId == null) return Optional.empty();

        return comment.getVotes().stream()
                .filter(vote -> vote.getMember().getId().equals(memberId))
                .findFirst();
    }

    // 댓글에 추천/비추천 취소 (이미 앞에서 comment-myVote 관계 검증했을 때만 사용)
    public void removeCommentVote(Comment comment, CommentVote vote){
        comment.getVotes().remove(vote);
        if(vote.getVoteType() == VoteType.UPVOTE){
            comment.setUpvoteCount(comment.getUpvoteCount() - 1);
        }else{
            comment.setDownvoteCount(comment.getDownvoteCount() - 1);
        }

        commentRepository.save(comment);
    }

    // 댓글에 추천/비추천 등록
    public void saveCommentVote(Comment comment, VoteType type, long memberId){
        Member member = memberService.findById(memberId);

        CommentVote vote = new CommentVote(type, comment, member);
        comment.addVote(vote);

        if(vote.getVoteType() == VoteType.UPVOTE){
            comment.setUpvoteCount(comment.getUpvoteCount() + 1);
        }else{
            comment.setDownvoteCount(comment.getDownvoteCount() + 1);
        }

        commentRepository.save(comment);
    }

    // 대댓글에 특정 유저가 추천/비추천을 눌렀는지 조회
    public Optional<ReplyVote> getReplyVoteFromCertainMember(Reply reply, Long memberId) {
        if(memberId == null) return Optional.empty();

        return reply.getVotes().stream()
                .filter(vote -> vote.getMember().getId().equals(memberId))
                .findFirst();
    }

    // 대댓글에 추천/비추천 등록
    public void saveReplyVote(Reply reply, VoteType type, long memberId){
        Member member = memberService.findById(memberId);

        ReplyVote vote = new ReplyVote(type, reply, member);
        reply.addVote(vote);

        if(vote.getVoteType() == VoteType.UPVOTE){
            reply.setUpvoteCount(reply.getUpvoteCount() + 1);
        }else{
            reply.setDownvoteCount(reply.getDownvoteCount() + 1);
        }

        replyRepository.save(reply);
    }

    // 대댓글에 추천/비추천 취소 (이미 앞에서 reply-myVote 관계 검증했을 때만 사용)
    public void removeReplyVote(Reply reply, ReplyVote vote){
        reply.getVotes().remove(vote);
        if(vote.getVoteType() == VoteType.UPVOTE){
            reply.setUpvoteCount(reply.getUpvoteCount() - 1);
        }else{
            reply.setDownvoteCount(reply.getDownvoteCount() - 1);
        }

        replyRepository.save(reply);
    }
}

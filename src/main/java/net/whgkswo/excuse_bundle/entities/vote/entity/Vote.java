package net.whgkswo.excuse_bundle.entities.vote.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.whgkswo.excuse_bundle.entities.TimeStampedEntity;
import net.whgkswo.excuse_bundle.entities.members.core.entitiy.Member;
import net.whgkswo.excuse_bundle.entities.posts.comments.entity.Comment;
import net.whgkswo.excuse_bundle.entities.posts.comments.reply.entity.Reply;
import net.whgkswo.excuse_bundle.entities.posts.core.entity.Post;
import net.whgkswo.excuse_bundle.exceptions.BusinessLogicException;
import net.whgkswo.excuse_bundle.exceptions.ExceptionType;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Vote extends TimeStampedEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // Post에 대한 투표 (nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    // Comment에 대한 투표 (nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    // Reply에 대한 투표 (nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_id")
    private Reply reply;

    @Enumerated(EnumType.STRING)
    private VoteType voteType;

    // Post Vote 생성자
    public Vote(Post post, Member member, VoteType voteType) {
        this.post = post;
        this.member = member;
        this.voteType = voteType;
    }

    // Comment Vote 생성자
    public Vote(Comment comment, Member member, VoteType voteType) {
        this.comment = comment;
        this.member = member;
        this.voteType = voteType;
    }

    // Reply Vote 생성자
    public Vote(Reply reply, Member member, VoteType voteType) {
        this.reply = reply;
        this.member = member;
        this.voteType = voteType;
    }

    // Post <-> Vote
    public void setPost(Post post){
        if(comment != null || reply != null) throw new BusinessLogicException(ExceptionType.VOTE_TARGET_CONFLICT);
        this.post = post;
        if(!post.getVotes().contains(this)){
            post.addVote(this);
        }
    }
    // Comment <-> Vote
    public void setPost(Comment comment){
        if(post != null || reply != null) throw new BusinessLogicException(ExceptionType.VOTE_TARGET_CONFLICT);
        this.comment = comment;
        if(!comment.getVotes().contains(this)){
            comment.addVote(this);
        }
    }
    // Reply <-> Vote
    public void setPost(Reply reply){
        if(member != null || comment != null) throw new BusinessLogicException(ExceptionType.VOTE_TARGET_CONFLICT);
        this.reply = reply;
        if(!reply.getVotes().contains(this)){
            reply.addVote(this);
        }
    }
}

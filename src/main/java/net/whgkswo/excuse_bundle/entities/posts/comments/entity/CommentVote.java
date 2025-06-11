package net.whgkswo.excuse_bundle.entities.posts.comments.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.whgkswo.excuse_bundle.entities.TimeStampedEntity;
import net.whgkswo.excuse_bundle.entities.members.core.entitiy.Member;
import net.whgkswo.excuse_bundle.entities.vote.entity.VoteType;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentVote extends TimeStampedEntity {
    @Enumerated(EnumType.STRING)
    private VoteType voteType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Member member;

    // Comment <-> Vote
    public void setComment(Comment comment){
        this.comment = comment;
        if(!comment.getVotes().contains(this)){
            comment.addVote(this);
        }
    }
}

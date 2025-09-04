package net.whgkswo.excuse_bundle.entities.posts.comments.reply.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.whgkswo.excuse_bundle.entities.TimeStampedEntity;
import net.whgkswo.excuse_bundle.entities.members.core.entitiy.Member;
import net.whgkswo.excuse_bundle.entities.posts.comments.entity.AbstractComment;
import net.whgkswo.excuse_bundle.entities.vote.entity.VoteType;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReplyVote extends TimeStampedEntity {
    @Enumerated(EnumType.STRING)
    private VoteType voteType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_id")
    private Reply reply;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Member member;

    // Comment <-> Vote
    public void setComment(Reply reply){
        this.reply = reply;
        if(!reply.getVotes().contains(this)){
            reply.addVote(this);
        }
    }
}

package net.whgkswo.excuse_bundle.entities.posts.core.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.whgkswo.excuse_bundle.entities.TimeStampedEntity;
import net.whgkswo.excuse_bundle.entities.members.core.entitiy.Member;
import net.whgkswo.excuse_bundle.entities.vote.entity.VoteType;

@Entity
@Getter
@Setter
@AllArgsConstructor
public class PostVote extends TimeStampedEntity {

    private VoteType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Member member;

    // Post <-> Vote
    public void setPost(Post post){
        this.post = post;
        if(!post.getVotes().contains(this)) post.addVote(this);
    }
}

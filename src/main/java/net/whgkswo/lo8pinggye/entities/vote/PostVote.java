package net.whgkswo.lo8pinggye.entities.vote;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import net.whgkswo.lo8pinggye.entities.TimeStampedEntity;
import net.whgkswo.lo8pinggye.entities.posts.Post;
import net.whgkswo.lo8pinggye.entities.members.Member;

@Entity
public class PostVote extends TimeStampedEntity {
    private VoteType type;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Member member;
}

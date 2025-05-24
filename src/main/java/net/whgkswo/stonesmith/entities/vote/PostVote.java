package net.whgkswo.stonesmith.entities.vote;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import net.whgkswo.stonesmith.entities.TimeStampedEntity;
import net.whgkswo.stonesmith.entities.posts.Post;
import net.whgkswo.stonesmith.entities.members.Member;

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

package net.whgkswo.excuse_bundle.entities.posts.comments.entity;

import jakarta.persistence.*;
import net.whgkswo.excuse_bundle.entities.TimeStampedEntity;
import net.whgkswo.excuse_bundle.entities.members.core.entitiy.Member;
import net.whgkswo.excuse_bundle.entities.vote.entity.VoteType;

@Entity
public class CommentVote extends TimeStampedEntity {
    @Enumerated(EnumType.STRING)
    private VoteType type;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Member member;
}

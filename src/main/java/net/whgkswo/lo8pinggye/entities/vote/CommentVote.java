package net.whgkswo.lo8pinggye.entities.vote;

import jakarta.persistence.*;
import net.whgkswo.lo8pinggye.entities.TimeStampedEntity;
import net.whgkswo.lo8pinggye.entities.comments.Comment;
import net.whgkswo.lo8pinggye.entities.members.Member;

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

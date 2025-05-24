package net.whgkswo.stonesmith.entities.vote;

import jakarta.persistence.*;
import net.whgkswo.stonesmith.entities.TimeStampedEntity;
import net.whgkswo.stonesmith.entities.comments.Comment;
import net.whgkswo.stonesmith.entities.members.Member;

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

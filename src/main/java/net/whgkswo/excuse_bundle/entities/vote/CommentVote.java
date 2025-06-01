package net.whgkswo.excuse_bundle.entities.vote;

import jakarta.persistence.*;
import net.whgkswo.excuse_bundle.entities.TimeStampedEntity;
import net.whgkswo.excuse_bundle.entities.comments.Comment;
import net.whgkswo.excuse_bundle.entities.members.core.Member;

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

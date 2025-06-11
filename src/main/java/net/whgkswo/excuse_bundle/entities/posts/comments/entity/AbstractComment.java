package net.whgkswo.excuse_bundle.entities.posts.comments.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.whgkswo.excuse_bundle.entities.TimeStampedEntity;
import net.whgkswo.excuse_bundle.entities.members.core.entitiy.Member;

import java.util.ArrayList;
import java.util.List;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
public abstract class AbstractComment extends TimeStampedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    protected Member member;

    protected String content;

    protected int upvoteCount = 0;

    protected int downvoteCount = 0;

    @Enumerated(EnumType.STRING)
    protected Status status = Status.ACTIVE;

    public AbstractComment(Member member, String content) {
        this.member = member;
        this.content = content;
    }

    public enum Status {
        ACTIVE,
        DELETED
    }
}

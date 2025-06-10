package net.whgkswo.excuse_bundle.entities.posts.comments.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.whgkswo.excuse_bundle.entities.TimeStampedEntity;
import net.whgkswo.excuse_bundle.entities.posts.core.entity.Post;
import net.whgkswo.excuse_bundle.entities.members.core.entitiy.Member;

@Entity
@Getter
@Setter
public class Comment extends TimeStampedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;  // 최상위 댓글인 경우 사용

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;    // 대댓글인 경우 사용

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String content;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    public Comment(Post post, Member member, String content){
        this.post = post;
        this.member = member;
        this.content = content;
    }

    public Comment(Comment comment, Member member, String content){
        this.comment = comment;
        this.member = member;
        this.content = content;
    }

    public enum Status{
        ACTIVE,
        DELETED
    }
}

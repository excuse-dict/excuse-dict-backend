package net.whgkswo.lo8pinggye.entities.comments;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import net.whgkswo.lo8pinggye.entities.TimeStampedEntity;
import net.whgkswo.lo8pinggye.entities.posts.Post;
import net.whgkswo.lo8pinggye.entities.members.Member;

@Entity
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
}

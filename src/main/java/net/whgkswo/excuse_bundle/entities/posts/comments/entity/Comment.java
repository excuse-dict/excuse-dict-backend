package net.whgkswo.excuse_bundle.entities.posts.comments.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.whgkswo.excuse_bundle.entities.TimeStampedEntity;
import net.whgkswo.excuse_bundle.entities.posts.core.entity.Post;
import net.whgkswo.excuse_bundle.entities.members.core.entitiy.Member;
import net.whgkswo.excuse_bundle.entities.vote.entity.Vote;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
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

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vote> votes = new ArrayList<>();

    private int upvoteCount = 0;

    private int downvoteCount = 0;

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

    // Vote <-> Comment
    public void addVote(Vote vote){
        votes.add(vote);
        if(vote.getComment() == null){
            vote.setComment(this);
        }
    }

    public enum Status{
        ACTIVE,
        DELETED
    }
}

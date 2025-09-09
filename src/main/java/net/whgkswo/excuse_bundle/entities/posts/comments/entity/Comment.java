package net.whgkswo.excuse_bundle.entities.posts.comments.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.whgkswo.excuse_bundle.entities.TimeStampedEntity;
import net.whgkswo.excuse_bundle.entities.posts.comments.reply.entity.Reply;
import net.whgkswo.excuse_bundle.entities.posts.core.entity.Post;
import net.whgkswo.excuse_bundle.entities.members.core.entitiy.Member;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Comment extends AbstractComment {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentVote> votes = new ArrayList<>();

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reply> replies = new ArrayList<>();

    public Comment(Post post, Member member, String content){
        this.post = post;
        this.member = member;
        this.content = content;
    }

    public Comment(Comment comment, Member member, String content){
        this.member = member;
        this.content = content;
    }

    // Vote <-> Comment
    public void addVote(CommentVote vote){
        votes.add(vote);
        if(vote.getComment() == null) vote.setComment(this);
    }

    // Reply <-> Comment
    public void addReply(Reply reply){
        replies.add(reply);
        if(reply.getComment() == null) reply.setComment(this);
    }

    public enum Status{
        ACTIVE,
        DELETED
    }
}

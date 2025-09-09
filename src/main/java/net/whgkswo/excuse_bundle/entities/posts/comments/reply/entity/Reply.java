package net.whgkswo.excuse_bundle.entities.posts.comments.reply.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.whgkswo.excuse_bundle.entities.posts.comments.entity.AbstractComment;
import net.whgkswo.excuse_bundle.entities.posts.comments.entity.Comment;
import net.whgkswo.excuse_bundle.entities.posts.comments.entity.CommentVote;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reply extends AbstractComment {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @OneToMany(mappedBy = "reply", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReplyVote> votes = new ArrayList<>();

    // Vote <-> Reply
    public void addVote(ReplyVote vote){
        votes.add(vote);
        if(vote.getReply() == null) vote.setComment(this);
    }

    // Comment <-> Reply
    public void setComment(Comment comment){
        this.comment = comment;
        if(!comment.getReplies().contains(this)) comment.addReply(this);
    }
}

package net.whgkswo.excuse_bundle.entities.posts.comments.reply.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.whgkswo.excuse_bundle.entities.posts.comments.entity.AbstractComment;
import net.whgkswo.excuse_bundle.entities.posts.comments.entity.Comment;
import net.whgkswo.excuse_bundle.entities.vote.entity.Vote;

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
    private List<Vote> votes = new ArrayList<>();

    // Vote <-> Reply
    public void addVote(Vote vote){
        votes.add(vote);
        if(vote.getReply() == null){
            vote.setReply(this);
        }
    }
}

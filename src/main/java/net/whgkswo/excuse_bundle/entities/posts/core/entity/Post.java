package net.whgkswo.excuse_bundle.entities.posts.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.whgkswo.excuse_bundle.entities.TimeStampedEntity;
import net.whgkswo.excuse_bundle.entities.comments.Comment;
import net.whgkswo.excuse_bundle.entities.excuses.Excuse;
import net.whgkswo.excuse_bundle.entities.members.core.entitiy.Member;
import net.whgkswo.excuse_bundle.entities.posts.tags.entity.Tag;
import net.whgkswo.excuse_bundle.entities.vote.PostVote;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
public class Post extends TimeStampedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "excuse_id")
    private Excuse excuse;

    private List<String> images;

    @OneToMany(mappedBy = "post")
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    private List<PostVote> votes = new ArrayList<>();

    // votes를 매번 순회하는 것을 막기 위한 반정규화
    private int upvoteCount;
    private int downvoteCount;

    // Excuse <-> Post
    public void setExcuse(Excuse excuse){
        this.excuse = excuse;
        if(excuse.getPost() == null) excuse.setPost(this);
    }
}

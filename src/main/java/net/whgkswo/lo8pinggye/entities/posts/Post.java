package net.whgkswo.lo8pinggye.entities.posts;

import jakarta.persistence.*;
import net.whgkswo.lo8pinggye.entities.TimeStampedEntity;
import net.whgkswo.lo8pinggye.entities.comments.Comment;
import net.whgkswo.lo8pinggye.entities.excuses.Excuse;
import net.whgkswo.lo8pinggye.entities.members.Member;
import net.whgkswo.lo8pinggye.entities.vote.PostVote;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Post extends TimeStampedEntity {
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "excuse_id")
    private Excuse excuse;

    @OneToMany(mappedBy = "post")
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    private List<PostVote> votes = new ArrayList<>();

    // votes를 매번 순회하는 것을 막기 위한 반정규화
    private int upvoteCount;
    private int downvoteCount;
}

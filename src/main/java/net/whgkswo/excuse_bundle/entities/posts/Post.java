package net.whgkswo.excuse_bundle.entities.posts;

import jakarta.persistence.*;
import net.whgkswo.excuse_bundle.entities.TimeStampedEntity;
import net.whgkswo.excuse_bundle.entities.comments.Comment;
import net.whgkswo.excuse_bundle.entities.excuses.Excuse;
import net.whgkswo.excuse_bundle.entities.members.core.Member;
import net.whgkswo.excuse_bundle.entities.vote.PostVote;

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

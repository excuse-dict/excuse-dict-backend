package net.whgkswo.stonesmith.entities.posts;

import jakarta.persistence.*;
import net.whgkswo.stonesmith.entities.TimeStampedEntity;
import net.whgkswo.stonesmith.entities.cards.Card;
import net.whgkswo.stonesmith.entities.comments.Comment;
import net.whgkswo.stonesmith.entities.vote.PostVote;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Post extends TimeStampedEntity {
    private String title;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "main_card_id")
    private Card mainCard;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id") // Post를 통해 Card는 조회하지만 그 반대는 필요가 없어서 예외적으로 1:N만 연결. 연관관계의 주인이 반대이기 때문에 여기에다 조인컬럼 설정
    private List<Card> relatedCards = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    private List<PostVote> votes = new ArrayList<>();

    // votes를 매번 순회하는 것을 막기 위한 반정규화
    private int upvoteCount;
    private int downvoteCount;
}

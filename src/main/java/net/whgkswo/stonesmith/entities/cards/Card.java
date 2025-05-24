package net.whgkswo.stonesmith.entities.cards;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.whgkswo.stonesmith.entities.TimeStampedEntity;
import net.whgkswo.stonesmith.entities.members.Member;

@Getter
@Setter
@Entity
// 통합 검색을 위해 카드 타입 분류하지 않고 싱글 테이블 사용
public class Card extends TimeStampedEntity {

    private String name;

    @Enumerated(EnumType.STRING)
    private Rarity rarity;

    private int cost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // 하수인 전용
    private int attack;
    private int health;
    private String playLine;
    private String attackLine;
    private String deathLine;

    // 주문 전용
    private SpellType spellType;

    // 무기, 장소 전용
    private int durability;

    // 영웅 변신 전용
    private int armor;

    public enum Rarity{
        TOKEN,
        COMMON,
        RARE,
        EPIC,
        LEGEND
    }
}

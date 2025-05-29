package net.whgkswo.lo8pinggye.entities.cards.sets;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import net.whgkswo.lo8pinggye.entities.BaseEntity;
import net.whgkswo.lo8pinggye.entities.cards.Card;

import java.time.LocalDate;
import java.util.List;

@Entity
public class CardSet extends BaseEntity {
    @Nullable
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_set_id")
    private CardSet parentSet;

    private String name;

    private LocalDate releaseDate;

    @OneToMany(mappedBy = "cardSet")
    private List<Card> cards;
}

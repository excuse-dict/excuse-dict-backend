package net.whgkswo.stonesmith.entities.posts;

import net.whgkswo.stonesmith.entities.Entity;
import net.whgkswo.stonesmith.entities.cards.Card;

import java.util.ArrayList;
import java.util.List;

public class Post extends Entity {
    private String title;
    private Card mainCard;
    private List<Card> relatedCards = new ArrayList<>();
}

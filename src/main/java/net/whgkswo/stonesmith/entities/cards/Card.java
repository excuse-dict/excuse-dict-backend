package net.whgkswo.stonesmith.entities.cards;

import lombok.Getter;
import lombok.Setter;
import net.whgkswo.stonesmith.entities.Entity;
import net.whgkswo.stonesmith.entities.users.User;

@Getter
@Setter
public class Card extends Entity {
    private String name;
    private String namespace; // 기존 카드 - 커스텀 카드 구분용
    private User author;
}

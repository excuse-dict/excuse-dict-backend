package net.whgkswo.lo8pinggye.entities.cards;

public enum SpellType {
    ARCANE("비전"),
    FEL("지옥"),
    FIRE("화염"),
    FROST("냉기"),
    HOLY("신성"),
    NATURE("자연"),
    SHADOW("암흑")
    ;

    String name;

    SpellType(String name){
        this.name = name;
    }
}

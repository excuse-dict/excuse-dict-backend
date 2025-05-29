package net.whgkswo.lo8pinggye.entities.cards;

public enum MinionType{
    ALL("모두"),
    BEAST("야수"),
    DEMON("악마"),
    DRAENEI("드레나이"),
    DRAGON("용족"),
    ELEMENTAL("정령"),
    MECH("기계"),
    MURLOC("멀록"),
    NAGA("나가"),
    PIRATE("해적"),
    UNDEAD("언데드"),
    TOTEM("토템"),
    QUILBOAR("가시멧돼지")
    ;

    String name;

    MinionType(String name){
        this.name = name;
    }
}

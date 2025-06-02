package net.whgkswo.excuse_bundle.entities.posts;

import jakarta.persistence.*;
import net.whgkswo.excuse_bundle.entities.BaseEntity;

import java.util.Set;

@Entity
public class Tag extends BaseEntity {
    private String value;

    @Enumerated(EnumType.STRING)
    private Type type;

    public enum Type{
        ACCIDENT, // 사고
        COMPANY, // 직장
        EVENT, // 경조사
        FAMILY, // 가족사
        HEALTH, // 건강
        HOME_FACILITY, // 집 설비
        RELIGIOUS, // 종교
        TRANSPORT, // 교통
        WEATHER, // 날씨
        ETC // 기타
    }
}

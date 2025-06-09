package net.whgkswo.excuse_bundle.komoran;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

// 의미 있는 품사 (은는이가 등 제외)
@AllArgsConstructor
@Getter
public enum MeaningfulPosTag {
    NNG("일반명사"),          // 감기, 병원, 회사
    NNP("고유명사"),          // 삼성, 서울, 김철수
    VV("동사"),             // 가다, 먹다, 아프다
    VA("형용사"),           // 좋다, 나쁘다, 크다
    MAG("일반부사"),         // 빨리, 천천히, 많이
    VX("보조용언"),          // 하다, 되다, 있다
    XR("어근")             // 새-, 헛-, 군-
    ;

    private final String description;

    // 의미 있는 품사 체크
    public static boolean isMeaningful(String posTag){
        try{
            valueOf(posTag);
            return true;
        }catch (IllegalArgumentException e){
            return false;
        }
    }

    public static Set<String> getAllTags() {
        return Arrays.stream(values())
                .map(Enum::name)
                .collect(Collectors.toSet());
    }
}
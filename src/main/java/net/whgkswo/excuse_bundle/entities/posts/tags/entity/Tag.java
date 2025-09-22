package net.whgkswo.excuse_bundle.entities.posts.tags.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.whgkswo.excuse_bundle.entities.BaseEntity;
import net.whgkswo.excuse_bundle.lib.json.StringJsonToSetConverter;

import java.util.Set;

@Entity
@Getter
@Setter
public class Tag extends BaseEntity {
    private String value;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Convert(converter = StringJsonToSetConverter.class)
    private Set<String> tagKeywords;

    @Column(columnDefinition = "INT DEFAULT 0")
    private int popularity;

    @Getter
    public enum Category {
        ACCIDENT(Set.of("사고", "부상", "다침", "응급", "아픔", "피남", "위험", "안전")),
        COMPANY(Set.of("직장", "회사", "일", "근무", "근로", "노동", "출근", "퇴근", "출퇴근", "통근", "워크")),
        EVENT(Set.of("경조사", "상", "행사", "모임", "잔치", "기념", "파티", "축하", "이벤트")),
        EXERCISE(Set.of("운동", "헬스", "유산소", "근육", "근력", "짐")),
        FAMILY(Set.of("가족", "가정", "부모", "아이", "자녀", "집안", "친척")),
        FINANCIAL(Set.of("금전", "경제", "돈", "재정", "수입", "지출", "경비", "비용", "허리띠")),
        HEALTH(Set.of("건강", "병원", "아픔", "몸", "치료", "의료", "진료", "진찰", "시술", "수술", "컨디션")),
        HOME_FACILITY(Set.of("집", "주거", "주택", "설비", "수리", "고장", "문제", "이상", "불량", "결함", "공사", "작동", "안됨", "안나옴", "끊김")),
        LOVE(Set.of("연애", "사랑", "썸", "남자친구", "여자친구", "남친", "여친", "애인", "교제", "만남")),
        RELIGIOUS(Set.of("종교", "신", "신앙", "수양", "믿음")),
        STUDY(Set.of("학교", "자격증", "취업", "공무원", "자기계발", "합격", "차가운", "뜨거운")),
        TRANSPORT(Set.of("교통", "차", "도로", "이동", "운송", "통행")),
        WEATHER(Set.of("날씨", "기상", "기후", "천재지변", "자연재해")),
        ETC(Set.of("기타", "개인사", "사정"))
        ;

        final Set<String> categoryKeywords;

        Category(Set<String> categoryKeywords){
            this.categoryKeywords = categoryKeywords;
        }
    }
}

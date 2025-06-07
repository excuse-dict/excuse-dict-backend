package net.whgkswo.excuse_bundle.elasticsearch;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MatchType {
    EXACT(3.0, "정확한 매칭"),
    TAG_KEYWORD(2.0, "태그 키워드로 매칭"),
    CATEGORY_KEYWORD(1.0, "카테고리 키워드로 매칭"),
    ;

    private final double score;
    private final String description;
}

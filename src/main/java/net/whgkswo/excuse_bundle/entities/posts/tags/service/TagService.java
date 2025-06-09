package net.whgkswo.excuse_bundle.entities.posts.tags.service;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.elasticsearch.ElasticService;
import net.whgkswo.excuse_bundle.entities.posts.tags.commands.SearchTagCommand;
import net.whgkswo.excuse_bundle.entities.posts.tags.elasticsearch.TagSearchResult;
import net.whgkswo.excuse_bundle.entities.posts.tags.entity.Tag;
import net.whgkswo.excuse_bundle.entities.posts.tags.repository.TagRepository;
import net.whgkswo.excuse_bundle.responses.page.PageUtil;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;
    private final ElasticService elasticService;

    // 가중치 상수
    private static final double TAG_NAME_WEIGHT = 1.0;      // 태그명 직접 매치
    private static final double TAG_KEYWORD_WEIGHT = 0.7;   // 태그 키워드 매치
    private static final double CATEGORY_KEYWORD_WEIGHT = 0.4; // 카테고리 키워드 매치


    // 컨트롤러 요청 받아 페이지로 래핑해 반환
    public Page<Tag> searchTags(SearchTagCommand command){
        List<Tag> tags = findTagsByCondition(command.filterCategories(), command.searchValue());

        return PageUtil.createPageFromList(tags, command.page(), command.size());
    }

    // 태그 검색 요청 분기처리
    private List<Tag> findTagsByCondition(List<Tag.Category> filterCategories, String searchValue){

        if(searchValue == null || searchValue.isBlank()){
            if(filterCategories == null || filterCategories.isEmpty()){
                // 카테고리 x, 검색어 x
                return new ArrayList<>();
            }else {
                // 카테고리 o, 검색어 x
                return tagRepository.findByCategoryIn(filterCategories);
            }
        }else{
            // 검색어는 엘라스틱 서치 적용
            return elasticSearchTags(searchValue, filterCategories).stream()
                    .map(TagSearchResult::tag)
                    .collect(Collectors.toList());
        }
    }

    // 카테고리 필터 적용하여 태그 조회
    private List<Tag> getFilteredTags(List<Tag.Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return Collections.emptyList();
        }
        return tagRepository.findByCategoryIn(categories);
    }

    public List<TagSearchResult> elasticSearchTags(String userInput, List<Tag.Category> categories) {
        // 사용자 입력 형태소 분해
        List<String> morphemes = elasticService.getMeaningfulMorphemes(userInput.trim());

        if (morphemes.isEmpty()) {
            return Collections.emptyList();
        }

        // 타깃 태그에 대해 유사도 계산
        List<Tag> targetTags = getFilteredTags(categories);
        List<TagSearchResult> matchResults = new ArrayList<>();

        for (Tag tag : targetTags) {
            double totalScore = calculateTotalMatchScore(morphemes, tag);

            if (totalScore > 0) {
                matchResults.add(new TagSearchResult(tag, totalScore));
            }
        }

        // 유사도 순으로 정렬해 반환
        return matchResults.stream()
                .sorted((a, b) -> Double.compare(b.score(), a.score()))
                .collect(Collectors.toList());
    }

    // 형태소 -> 태그 유사도 계산
    private double calculateTotalMatchScore(List<String> morphemes, Tag tag) {
        double maxScore = 0.0;

        // 태그명과 직접 매치 (최고 가중치)
        double tagNameScore = elasticService.calculateMatchScore(morphemes, tag.getValue());
        maxScore = Math.max(maxScore, tagNameScore * TAG_NAME_WEIGHT);

        // 태그 키워드와 매치
        if (tag.getTagKeywords() != null && !tag.getTagKeywords().isEmpty()) {
            double tagKeywordScore = elasticService.calculateKeywordMatchScore(morphemes, tag.getTagKeywords());
            maxScore = Math.max(maxScore, tagKeywordScore * TAG_KEYWORD_WEIGHT);
        }

        // 카테고리 키워드와 매치 (최저 가중치)
        if (tag.getCategory() != null) {
            double categoryKeywordScore = elasticService.calculateKeywordMatchScore(
                    morphemes,
                    tag.getCategory().getCategoryKeywords()
            );
            maxScore = Math.max(maxScore, categoryKeywordScore * CATEGORY_KEYWORD_WEIGHT);
        }

        return maxScore;
    }
}

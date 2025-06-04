package net.whgkswo.excuse_bundle.entities.posts.tags.services;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.entities.posts.tags.repositories.TagSearchRepository;
import net.whgkswo.excuse_bundle.entities.posts.tags.entities.Tag;
import net.whgkswo.excuse_bundle.entities.posts.tags.entities.TagDocument;
import net.whgkswo.excuse_bundle.responses.page.PageUtil;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagSearchRepository tagSearchRepository;

    // els document -> jpa 엔티티 변환
    private List<Tag> documentsToEntities(List<TagDocument> documents){
        return documents.stream()
                .map(document -> document.toEntity())
                .collect(Collectors.toList());
    }

    // 컨트롤러 요청 받아 페이지로 래핑해 반환
    public Page<Tag> searchTags(List<Tag.Category> filterCategories, String searchValue, int page, int size){
        List<Tag> tags = findTagsByCondition(filterCategories, searchValue);

        return PageUtil.createPageFromList(tags, page - 1, size); // 컨트롤러에서 넘어온 page는 1부터 시작, 내부적으론 0부터 시작
    }

    // 태그 검색 요청 분기처리
    private List<Tag> findTagsByCondition(List<Tag.Category> filterCategories, String searchValue){

        if(searchValue == null || searchValue.isBlank()){
            if(filterCategories == null || filterCategories.isEmpty()){
                // 카테고리 x, 검색어 x
                return getAllTags();
            }else {
                // 카테고리 o, 검색어 x
                return searchTagsByType(filterCategories);
            }
        }else{
            if(filterCategories == null || filterCategories.isEmpty()){
                // 카테고리 o, 검색어 x
                return searchTagsByValue(searchValue);
            }else{
                // 카테고리 o, 검색어 o
                return searchTagsByTypeAndValue(filterCategories, searchValue);
            }
        }
    }

    // 전부 가져오기
    private List<Tag> getAllTags(){
        Iterable<TagDocument> documents = tagSearchRepository.findAll();
        List<TagDocument> documentList = StreamSupport.stream(documents.spliterator(), false)
                .collect(Collectors.toList());
        return documentsToEntities(documentList);
    }

    // 타입으로 검색
    private List<Tag> searchTagsByType(List<Tag.Category> categories){
        List<String> typeNames = categories.stream()
                .map(category -> category.name())
                .collect(Collectors.toList());

        List<TagDocument> tagDocuments = tagSearchRepository.findByCategoryIn(typeNames);

        return documentsToEntities(tagDocuments);
    }

    // 카테고리 + 일치값으로 검색
    private List<Tag> searchTagsByTypeAndValue(List<Tag.Category> categories, String value){
        List<String> typeNames = categories.stream()
                .map(category -> category.name())
                .collect(Collectors.toList());

        List<TagDocument> tagDocuments = tagSearchRepository.findByCategoryInAndValueContaining(typeNames, value);

        return documentsToEntities(tagDocuments);
    }

    // 일치값으로 검색
    private List<Tag> searchTagsByValue(String value){
        List<TagDocument> tagDocuments = tagSearchRepository.findByValueContaining(value);

        return documentsToEntities(tagDocuments);
    }
}

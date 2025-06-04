package net.whgkswo.excuse_bundle.entities.posts.tags.services;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.entities.posts.tags.repositories.TagSearchRepository;
import net.whgkswo.excuse_bundle.entities.posts.tags.entities.Tag;
import net.whgkswo.excuse_bundle.entities.posts.tags.entities.TagDocument;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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

    // 태그 검색 요청 분기처리
    public List<Tag> searchTags(List<Tag.Type> filterTypes, String searchValue){

        if(searchValue == null){
            if(filterTypes == null || filterTypes.isEmpty()){
                // 타입 x, 검색어 x
                return getAllTags();
            }else {
                // 타입 o, 검색어 x
                return searchTagsByType(filterTypes);
            }
        }else{
            if(filterTypes == null || filterTypes.isEmpty()){
                // 타입 o, 검색어 x
                return searchTagsByValue(searchValue);
            }else{
                // 타입 o, 검색어 o
                return searchTagsByTypeAndValue(filterTypes, searchValue);
            }
        }
    }

    // 전부 가져오기
    private List<Tag> getAllTags(){
        List<TagDocument> documents = (List<TagDocument>) tagSearchRepository.findAll();
        return documentsToEntities(documents);
    }

    // 타입으로 검색
    private List<Tag> searchTagsByType(List<Tag.Type> types){
        List<String> typeNames = types.stream()
                .map(type -> type.name())
                .collect(Collectors.toList());

        List<TagDocument> tagDocuments = tagSearchRepository.findByTypes(typeNames);

        return documentsToEntities(tagDocuments);
    }

    // 타입 + 일치값으로 검색
    private List<Tag> searchTagsByTypeAndValue(List<Tag.Type> types, String value){
        List<String> typeNames = types.stream()
                .map(type -> type.name())
                .collect(Collectors.toList());

        List<TagDocument> tagDocuments = tagSearchRepository.findByTypesAndValueContaining(typeNames, value);

        return documentsToEntities(tagDocuments);
    }

    // 일치값으로 검색
    private List<Tag> searchTagsByValue(String value){
        List<TagDocument> tagDocuments = tagSearchRepository.findByValueContaining(value);

        return documentsToEntities(tagDocuments);
    }
}

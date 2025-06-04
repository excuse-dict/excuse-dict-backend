package net.whgkswo.excuse_bundle.entities.posts.tags.repositories;

import net.whgkswo.excuse_bundle.entities.posts.tags.entities.TagDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagSearchRepository extends ElasticsearchRepository<TagDocument, String> {
    List<TagDocument> findByTypes(List<String> types);

    // 부분 값 입력으로 검색
    List<TagDocument> findByValueContaining(String value);

    // 타입과 부분 값으로 검색
    List<TagDocument> findByTypesAndValueContaining(List<String> types, String value);

    // JPA ID로 검색
    TagDocument findByEntityId(Long entityId);
}

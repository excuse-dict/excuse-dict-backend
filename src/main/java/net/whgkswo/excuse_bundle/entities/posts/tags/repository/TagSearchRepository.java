package net.whgkswo.excuse_bundle.entities.posts.tags.repository;

import net.whgkswo.excuse_bundle.entities.posts.tags.entity.TagDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagSearchRepository extends ElasticsearchRepository<TagDocument, String> {
    // 카테고리로 검색
    List<TagDocument> findByCategoryIn(List<String> categories);

    // 부분 값 입력으로 검색
    List<TagDocument> findByValueContaining(String value);

    // 카테고리와 부분 값으로 검색
    List<TagDocument> findByCategoryInAndValueContaining(List<String> categories, String value);

    // JPA ID로 검색
    TagDocument findByEntityId(Long entityId);
}

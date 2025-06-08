package net.whgkswo.excuse_bundle.entities.posts.tags.elasticsearch;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.whgkswo.excuse_bundle.entities.posts.tags.entity.Tag;
import net.whgkswo.excuse_bundle.entities.posts.tags.repository.TagRepository;
import net.whgkswo.excuse_bundle.exceptions.BusinessLogicException;
import net.whgkswo.excuse_bundle.exceptions.ExceptionType;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class TagSyncService {
    private final TagRepository tagRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    @PostConstruct
    @Transactional(readOnly = true)
    public void syncAllTagsToElasticsearch() {
        try {
            log.info("=== Tag ES 동기화 시작 ===");

            // 기존 인덱스 삭제 후 재생성
            recreateIndex();

            // 모든 Tag 데이터 조회
            List<Tag> allTags = tagRepository.findAll();
            log.info("DB에서 Tag {} 개 조회", allTags.size());

            // ES Document로 변환
            List<TagDocument> tagDocuments = allTags.stream()
                    .map(TagDocument::from)
                    .collect(Collectors.toList());
            log.info("TagDocument {} 개 변환 완료", tagDocuments.size());

            // ES 데이터 저장
            elasticsearchOperations.save(tagDocuments);
            log.info("ES에 Tag {} 개 저장 완료", tagDocuments.size());

        } catch (Exception e) {
            log.error("Tag ES 동기화 실패: {}", e.getMessage(), e);
            throw new BusinessLogicException(ExceptionType.ES_SYNC_FAILED);
        }
    }

    private void recreateIndex() {
        try {
            // 인덱스 존재하면 삭제
            if (elasticsearchOperations.indexOps(TagDocument.class).exists()) {
                elasticsearchOperations.indexOps(TagDocument.class).delete();
                log.info("기존 tags 인덱스 삭제");
            }

            // 인덱스 생성
            elasticsearchOperations
                    .indexOps(TagDocument.class).create();
            log.info("tags 인덱스 생성 완료");

        } catch (Exception e) {
            log.error("인덱스 재생성 실패: {}", e.getMessage(), e);
            throw e;
        }
    }
}

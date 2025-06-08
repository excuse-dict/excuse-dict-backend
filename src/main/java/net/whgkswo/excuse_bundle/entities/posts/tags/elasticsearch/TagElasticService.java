package net.whgkswo.excuse_bundle.entities.posts.tags.elasticsearch;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.elasticsearch.ESQueryLoader;
import net.whgkswo.excuse_bundle.elasticsearch.ElasticService;
import net.whgkswo.excuse_bundle.responses.page.PageUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagElasticService {
    private final ESQueryLoader esQueryLoader;
    private final ElasticService elasticService;

    public Page<TagDocument> searchTags(String searchText, int page, int size) {
        int from = (page - 1) * size;

        String queryJson = esQueryLoader.getQuery("tagSearch", Map.of(
                "searchText", searchText,
                "from", from,
                "size", size
        ));

        SearchResponse<TagDocument> response = elasticService.executeSearchWithResponse(queryJson, TagDocument.INDEX_NAME, TagDocument.class);

        List<TagDocument> documents = response.hits().hits().stream()
                .map(hit -> hit.source())
                .collect(Collectors.toList());

        long totalCount = response.hits().total().value();

        return new PageImpl<>(documents, PageRequest.of(page - 1, size), totalCount);
    }
}

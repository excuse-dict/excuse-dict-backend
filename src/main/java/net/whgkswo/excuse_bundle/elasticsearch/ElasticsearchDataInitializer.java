package net.whgkswo.excuse_bundle.elasticsearch;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.entities.posts.tags.entities.TagDocument;
import net.whgkswo.excuse_bundle.entities.posts.tags.repositories.TagSearchRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
// 일반 db -> els db 자동 동기화
public class ElasticsearchDataInitializer {
    private final JdbcTemplate jdbcTemplate;
    private final TagSearchRepository tagSearchRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void syncDataOnStartup(){

        syncTags(); // 태그
    }

    private void syncTags(){
        try {
            if (tagSearchRepository.count() == 0) {
                String sql = "SELECT id, value, category FROM tag";
                List<TagDocument> documents = jdbcTemplate.query(sql, (rs, rowNum) ->
                        new TagDocument(
                                rs.getString("value"),
                                rs.getString("category"),
                                rs.getLong("id")
                        )
                );
                tagSearchRepository.saveAll(documents);
            }
        } catch (Exception e) {
        }
    }
}

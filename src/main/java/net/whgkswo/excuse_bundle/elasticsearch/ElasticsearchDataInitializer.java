package net.whgkswo.excuse_bundle.elasticsearch;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.entities.posts.tags.entity.TagDocument;
import net.whgkswo.excuse_bundle.entities.posts.tags.repository.TagSearchRepository;
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
            // DB 개수 확인
            Integer dbCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tag", Integer.class);

            // ES 개수 확인
            long esCount = tagSearchRepository.count();

            // 다르면 동기화
            if(dbCount.longValue() == esCount) {
                return;
            }

            tagSearchRepository.deleteAll();

            String sql = "SELECT id, value, category FROM tag";
            List<TagDocument> documents = jdbcTemplate.query(sql, (rs, rowNum) ->
                    new TagDocument(
                            rs.getString("value"),
                            rs.getString("category"),
                            rs.getLong("id")
                    )
            );

            tagSearchRepository.saveAll(documents);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

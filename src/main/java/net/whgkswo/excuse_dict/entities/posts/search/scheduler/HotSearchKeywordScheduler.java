package net.whgkswo.excuse_dict.entities.posts.search.scheduler;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_dict.auth.redis.RedisKey;
import net.whgkswo.excuse_dict.auth.redis.RedisService;
import net.whgkswo.excuse_dict.entities.posts.post_core.service.PostService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class HotSearchKeywordScheduler {

    private final RedisService redisService;

    @Scheduled(cron = "0 0 0 * * *")
    public void updateHotSearchKeyword() {

        LocalDate today = LocalDate.now();

        Map<String, Integer> result = new HashMap<>();

        // 일주일치 합산
        for(int i = 0; i < PostService.SEARCH_KEYWORD_EXPIRE_DAYS; i++) {

            LocalDate date = today.minusDays(i);
            RedisKey key = new RedisKey(RedisKey.Prefix.SEARCH, date.toString());

            Map<String, Double> keywords = redisService.getAllOfSortedSetEntries(key, false);

            keywords.forEach((keyword, count)
                    -> result.merge(keyword, count.intValue(), Integer::sum)); // 합산
        }

        RedisKey key = new RedisKey(RedisKey.Prefix.SEARCH, PostService.RECENT_SEARCHED_KEYWORDS_KEY);

        // 기존 데이터 삭제하고 덮어씌우기
        redisService.remove(key);
        result.forEach((keyword, count) ->
                redisService.putSortedSet(key, keyword, count, 0)
        );
    }
}

package net.whgkswo.excuse_dict.search.scheduler;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_dict.auth.redis.RedisKey;
import net.whgkswo.excuse_dict.auth.redis.RedisService;
import net.whgkswo.excuse_dict.entities.posts.post_core.search.service.PostSearchService;
import net.whgkswo.excuse_dict.entities.posts.post_core.service.PostService;
import net.whgkswo.excuse_dict.komoran.KomoranHelper;
import net.whgkswo.excuse_dict.random.RandomHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Component
@RequiredArgsConstructor
public class SearchScheduler {

    private final PostService postService;
    private final KomoranHelper komoranHelper;
    private final RedisService redisService;

    // 일주일치 검색어 캐시 업데이트
    @Scheduled(cron = "0 0 0 * * *")
    public void updateHotSearchKeyword() {

        List<RedisKey> keys = new ArrayList<>();

        // 어제부터 일주일
        for(int i = 1; i <= PostSearchService.SEARCH_KEYWORD_EXPIRE_DAYS; i++) {
            String dateStr = LocalDate.now().minusDays(i).toString();
            keys.add(new RedisKey(RedisKey.Prefix.SEARCH, dateStr));
        }

        // Redis에서 합산
        RedisKey resultKey = new RedisKey(RedisKey.Prefix.SEARCH, PostSearchService.RECENT_SEARCHED_KEYWORDS_KEY);

        redisService.remove(resultKey);  // 기존 데이터 삭제
        redisService.unionSortedSets(resultKey, keys, 0, 7);  // 합산해서 저장
    }

    // 랜덤 검색어 생성
    @Scheduled(cron = "0 0 1 * * *")
    public void generateDummySearchKeywords(){
        Map<String, Double> randomKeywords = generateDummyKeywords();

        LocalDate today = LocalDate.now();
        RedisKey key = new RedisKey(RedisKey.Prefix.SEARCH, today.toString());

        randomKeywords.forEach((keyword, count) ->
                redisService.putMemberToSortedSet(key, keyword, count, 7));
    }

    // 랜덤 게시물 조회 후 검색어 추출
    private Map<String, Double> generateDummyKeywords() {
        List<Long> postIds = postService.getRandomPostIds(5);
        Map<String, Double> randomKeywords = new HashMap<>();
        Random random = new Random();

        for(Long postId : postIds){
            String content = postService.getPost(postId).getExcuse().getExcuse();
            List<String> morphemes = komoranHelper.getMeaningfulMorphemes(content);
            String keyword = morphemes.get(random.nextInt(morphemes.size()));
            randomKeywords.put(keyword,
                    (double) RandomHelper.getWeightedRandomValue(Map.of(1, 2, 2, 1, 3, 1)
                    ));
        }
        return randomKeywords;
    }
}

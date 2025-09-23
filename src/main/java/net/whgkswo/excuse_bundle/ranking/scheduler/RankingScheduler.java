package net.whgkswo.excuse_bundle.ranking.scheduler;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.auth.redis.RedisKey;
import net.whgkswo.excuse_bundle.auth.redis.RedisService;
import net.whgkswo.excuse_bundle.dummy.comments.DummyCommentsHelper;
import net.whgkswo.excuse_bundle.dummy.member.DummyMemberGenerator;
import net.whgkswo.excuse_bundle.entities.posts.core.entity.Post;
import net.whgkswo.excuse_bundle.entities.posts.core.repository.PostRepository;
import net.whgkswo.excuse_bundle.entities.posts.core.service.PostService;
import net.whgkswo.excuse_bundle.entities.posts.hotscore.PostIdWithHotScoreDto;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RankingScheduler {

    private final PostService postService;
    private final RedisService redisService;

    private static final int HALL_OF_FAME_SIZE = 100;
    public static final RedisKey HALL_OF_FAME_REDISKEY = new RedisKey(RedisKey.Prefix.HALL_OF_FAME, "posts");

    public static final int WEEKLY_TOP_SIZE = 20;
    public static final RedisKey WEEKLY_TOP_REDISKEY = new RedisKey(RedisKey.Prefix.WEEKLY_TOP, "posts");

    @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void initialize(){
        // TODO: 개발환경용 - 서버 시작시 바로 캐시 준비
        setHallOfFame();
        setWeeklyTop();
    }

    @Scheduled(cron = "0 0 4 * * *")
    @Transactional(readOnly = true)
    public void setHallOfFame(){

        Pageable pageable = PageRequest.of(0, HALL_OF_FAME_SIZE);
        List<Post> posts = postService.getTopNetLikes(pageable).stream().toList();

        // 레디스에는 ID만 저장
        List<Long> postIdList = posts.stream()
                        .map(post -> post.getId())
                        .toList();

        redisService.put(HALL_OF_FAME_REDISKEY, postIdList);
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional(readOnly = true)
    public void setWeeklyTop(){

        List<PostIdWithHotScoreDto> posts = postService.getRecentTopNetLikes(7);

        // 레디스에 저장
        redisService.put(WEEKLY_TOP_REDISKEY, posts);
    }
}

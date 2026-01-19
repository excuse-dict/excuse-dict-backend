package net.whgkswo.excuse_dict.ranking.service;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_dict.auth.redis.RedisService;
import net.whgkswo.excuse_dict.ranking.scheduler.RankingScheduler;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final RedisService redisService;

    public boolean isPostInHallOfFame(long postId){

        List<Long> hallOfFame = redisService.getAsList(RankingScheduler.HALL_OF_FAME_REDISKEY, Long.class);

        return hallOfFame.contains(postId);
    }
}

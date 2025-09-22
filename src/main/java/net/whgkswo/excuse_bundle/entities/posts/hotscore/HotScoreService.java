package net.whgkswo.excuse_bundle.entities.posts.hotscore;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.entities.posts.core.entity.Post;
import net.whgkswo.excuse_bundle.entities.vote.entity.VoteType;
import net.whgkswo.excuse_bundle.lib.TimeHelper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class HotScoreService {

    private final TimeHelper timeHelper;

    // 좋아요 구간별 가중치 설정
    // 1 ~ 10 -> 100점
    // 11 ~ 50 -> 65점
    // 51 ~ 100 -> 40점
    // 101 ~ 200 -> 25점
    // 201 ~ 300 -> 15점
    // 301+ -> 10점
    private static final List<Integer> SCORE_THRESHOLD = List.of(10, 50, 100, 200, 300, Integer.MAX_VALUE);
    private static final List<Integer> SCORE_VALUES = List.of(100, 65, 40, 25, 15, 10);

    private static final int WEEK_TO_MINUTES = 60 * 24 * 7;

    private static final int RECENT_RANGE_IN_MINUTES = 60 * 24;
    private static final double RECENT_VOTE_WEIGHT = 2.0;

    private static final int NEGATIVE_SCORE_PENALTY = 50;


    // 게시물의 Hot 스코어 계산
    public int calculateHotScore(Post post){
        int hotScore = 0;

        // 최근 작성글일 수록 가중치 up
        hotScore += calculateTimeScore(post.getCreatedAt());

        // 최근 추천 많을수록 가중치 up
        hotScore += calculateWeightVoteScore(post);

        return hotScore;
    }

    // 작성일시 점수: 최근일수록 높음 (최대 10080)
    private int calculateTimeScore(LocalDateTime createdAt) {
        long minutesAgo = timeHelper.getMinutesAgo(createdAt);

        if (minutesAgo >= WEEK_TO_MINUTES) return 0; // 1주일 이후 최소값

        // 방금 작성된 글 -> 최대, 시간 지날수록 감소
        return (int) (WEEK_TO_MINUTES - minutesAgo);
    }

    // 좋아요 점수: 최근 좋아요가 많을수록 보너스
    private int calculateWeightVoteScore(Post post) {

        LocalDateTime oneDayAgo = LocalDateTime.now().minusMinutes(RECENT_RANGE_IN_MINUTES);

        // 최근 1일간 순 좋아요 수
        int recentVotesCount = post.getVotes().stream()
                .filter(vote -> vote.getCreatedAt().isAfter(oneDayAgo))
                .mapToInt(vote -> vote.getVoteType().equals(VoteType.UPVOTE) ? 1 : -1)
                .sum();
        // 그 외 순 좋아요 수
        int netVotesCount = post.getUpvoteCount() - post.getDownvoteCount();
        int baseVotesCount = netVotesCount - recentVotesCount;

        // 가중치 적용해 점수 합산
        int baseScore = calculateWeightVoteScore(baseVotesCount, 1.0);
        int recentScore = calculateWeightVoteScore(recentVotesCount, RECENT_VOTE_WEIGHT);

        return baseScore + recentScore;
    }

    // 좋아요 갯수 구간별 가중치 적용해 점수 계산
    private int calculateWeightVoteScore(int votesCount, double scaleFactor) {

        // 좋아요 갯수가 음수일 경우 점수 내려버리기
        if(votesCount < 0) return -WEEK_TO_MINUTES + votesCount * NEGATIVE_SCORE_PENALTY;

        int score = 0;
        int remaining = votesCount;
        int prevThreshold = 0;

        for (int i = 0; i < SCORE_THRESHOLD.size(); i++) {
            int threshold = SCORE_THRESHOLD.get(i);
            int value = SCORE_VALUES.get(i);
            int currentInterval = threshold - prevThreshold;

            if (remaining <= currentInterval) {
                score += value * remaining;
                break;
            }

            score += value * currentInterval;
            remaining -= currentInterval;
            prevThreshold = threshold;
        }

        return (int) (score * scaleFactor);
    }
}

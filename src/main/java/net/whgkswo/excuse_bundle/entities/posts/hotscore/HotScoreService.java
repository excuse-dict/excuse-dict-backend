package net.whgkswo.excuse_bundle.entities.posts.hotscore;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.entities.posts.comments.entity.Comment;
import net.whgkswo.excuse_bundle.entities.posts.post_core.entity.Post;
import net.whgkswo.excuse_bundle.entities.posts.post_core.entity.PostVote;
import net.whgkswo.excuse_bundle.entities.vote.entity.VoteType;
import net.whgkswo.excuse_bundle.lib.time.TimeHelper;
import net.whgkswo.excuse_bundle.ranking.dto.RecentHotPostDto;
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
    private static final List<Integer> SCORE_VALUES_LIKES = List.of(100, 65, 40, 25, 15, 10); // 좋아요
    private static final List<Integer> SCORE_VALUES_COMMENTS = List.of(300, 180, 120, 80, 50, 30); // 이건 댓글

    private static final int WEEK_TO_MINUTES = 60 * 24 * 7;

    private static final int ONE_DAY_IN_MINUTES = 60 * 24;

    private static final double RECENT_WEIGHT = 1.5;
    private static final double RECENT_3DAY_WEIGHT = 1.0;
    private static final double REST_WEIGHT = 0.5;

    // 이걸로 hot스코어 기본 배율 조정. 오디오 마스터 볼륨 같은 거
    private static final double MASTER_SCALE_FACTOR = 0.1;


    // 게시물의 Hot 스코어 계산
    public int calculateHotScore(RecentHotPostDto post){
        double hotScore = 0.0;

        // 최근 작성글일 수록 가중치 up
        double timeWeight = calculateTimeWeight(post.createdAt());

        // 최근 추천 많을수록 가중치 보너스
        hotScore += calculateVoteScore(post, timeWeight);

        // 최근 댓글 많으면 가중치 보너스
        hotScore += calculateCommentScore(post, timeWeight);

        return (int)(hotScore * MASTER_SCALE_FACTOR);
    }

    // 작성일시 가중치: 최근일수록 높음
    private double calculateTimeWeight(LocalDateTime createdAt) {
        long minutesAgo = timeHelper.getMinutesAgo(createdAt);

        if (minutesAgo >= WEEK_TO_MINUTES) return 0; // 1주일 이후 최소값

        // 방금 작성된 글 -> 최대, 시간 지날수록 감소
        return 1.0 - ((double) minutesAgo / WEEK_TO_MINUTES);
    }

    // 댓글 점수: 최근 댓글이 많을수록 보너스
    private double calculateCommentScore(RecentHotPostDto post, double timeWeight) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneDayAgo = now.minusMinutes(ONE_DAY_IN_MINUTES);
        LocalDateTime threeDaysAgo = now.minusMinutes(ONE_DAY_IN_MINUTES * 3);

        int recentCommentCount = 0; // 최근 1일간 댓글수
        int recent3DaysCommentsCount = 0; // 1일 ~ 3일간 댓글수

        for (Comment comment : post.comments()) {
            LocalDateTime createdAt = comment.getCreatedAt();

            if (createdAt.isAfter(oneDayAgo)) { // 최근 1일
                recentCommentCount++;
            } else if (createdAt.isAfter(threeDaysAgo)) { // 최근 1 ~ 3일
                recent3DaysCommentsCount++;
            }
        }

        // 그 외 댓글수
        int baseCommentCount = post.comments().size() - recentCommentCount - recent3DaysCommentsCount;

        // 가중치 적용해 점수 합산
        double recentScore = getCommentScore(recentCommentCount, timeWeight * RECENT_WEIGHT); // 최근 1일
        double oneToThreeDaysScore = getCommentScore(recent3DaysCommentsCount, timeWeight * RECENT_3DAY_WEIGHT); // 1 ~ 3일
        double baseScore = getCommentScore(baseCommentCount, timeWeight * REST_WEIGHT); // 3일 ~ 7일

        return baseScore + oneToThreeDaysScore + recentScore;
    }

    // 좋아요 점수: 최근 좋아요가 많을수록 보너스
    private double calculateVoteScore(RecentHotPostDto post, double timeWeight) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneDayAgo = now.minusMinutes(ONE_DAY_IN_MINUTES);
        LocalDateTime threeDaysAgo = now.minusMinutes(ONE_DAY_IN_MINUTES * 3);

        int recentVotesCount = 0; // 최근 1일간 순 좋아요 수
        int recent3DaysVotesCount = 0; // 1일 ~ 3일간 순 좋아요 수

        for (PostVote vote : post.votes()) {
            LocalDateTime createdAt = vote.getCreatedAt();
            int voteValue = vote.getVoteType().equals(VoteType.UPVOTE) ? 1 : -1;

            if (createdAt.isAfter(oneDayAgo)) { // 최근 1일
                recentVotesCount += voteValue;
            } else if (createdAt.isAfter(threeDaysAgo)) { // 최근 1 ~ 3일
                recent3DaysVotesCount += voteValue;
            }
        }

        // 그 외 순 좋아요 수
        int netVotesCount = post.upvoteCount() - post.downvoteCount();
        int baseVotesCount = netVotesCount - recentVotesCount - recent3DaysVotesCount;

        // 가중치 적용해 점수 합산
        double recentScore = getVoteScore(recentVotesCount, timeWeight * RECENT_WEIGHT); // 최근 1일
        double oneToThreeDaysScore = getVoteScore(recent3DaysVotesCount, timeWeight * RECENT_3DAY_WEIGHT); // 1 ~ 3일
        double baseScore = getVoteScore(baseVotesCount, timeWeight * REST_WEIGHT); // 3일 ~ 7일

        return baseScore + oneToThreeDaysScore + recentScore;
    }

    // 댓글 갯수 구간별 가중치 적용해 점수 계산
    private double getCommentScore(int commentCount, double scaleFactor) {
        return getCumulativeScore(commentCount, scaleFactor, SCORE_VALUES_COMMENTS);
    }

    // 좋아요 갯수 구간별 가중치 적용해 점수 계산
    private double getVoteScore(int votesCount, double scaleFactor) {

        return getCumulativeScore(votesCount, scaleFactor, SCORE_VALUES_LIKES);
    }

    private double getCumulativeScore(int votesCount, double scaleFactor, List<Integer> scores) {
        double score = 0.0;
        int remaining = votesCount;
        int prevThreshold = 0;

        for (int i = 0; i < SCORE_THRESHOLD.size(); i++) {
            int threshold = SCORE_THRESHOLD.get(i);
            int value = scores.get(i);
            int currentInterval = threshold - prevThreshold;

            if (remaining <= currentInterval) {
                score += value * remaining;
                break;
            }

            score += value * currentInterval;
            remaining -= currentInterval;
            prevThreshold = threshold;
        }

        return score * scaleFactor;
    }
}

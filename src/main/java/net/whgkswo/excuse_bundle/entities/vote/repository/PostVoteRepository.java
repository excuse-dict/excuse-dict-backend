package net.whgkswo.excuse_bundle.entities.vote.repository;

import net.whgkswo.excuse_bundle.entities.posts.post_core.entity.PostVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostVoteRepository extends JpaRepository<PostVote, Long> {

    // 게시글들에 달린 추천 / 비추천 중 특정 회원이 누른 거
    @Query("SELECT pv FROM PostVote pv " +
            "JOIN FETCH pv.post " +
            "WHERE pv.post.id IN :postIds " +
            "AND pv.member.id = :memberId"
    )
    List<PostVote> findAllByPostIdsAndMemberId(@Param("postIds") List<Long> postIds, @Param("memberId") Long memberId);

    // Post id 리스트 -> PostVote 리스트 조회
    @Query("SELECT pv FROM PostVote pv " +
            "WHERE pv.post.id IN :postIds")
    List<PostVote> findVotesByPostIds(@Param("postIds") List<Long> postIds);
}

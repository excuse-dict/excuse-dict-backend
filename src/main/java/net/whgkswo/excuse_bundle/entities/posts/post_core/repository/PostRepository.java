package net.whgkswo.excuse_bundle.entities.posts.post_core.repository;

import net.whgkswo.excuse_bundle.entities.posts.comments.entity.Comment;
import net.whgkswo.excuse_bundle.entities.posts.post_core.dto.PostSearchDto;
import net.whgkswo.excuse_bundle.entities.posts.post_core.entity.Post;
import net.whgkswo.excuse_bundle.entities.posts.post_core.entity.PostVote;
import net.whgkswo.excuse_bundle.ranking.dto.RecentHotPostDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 경량화된 검색용 DTO 조회
    @Query("SELECT new " + PostSearchDto.PACKAGE_NAME +
            "(p.id, p.excuse.situation, p.excuse.excuse, p.member.nickname, p.createdAt) FROM Post p " +
            "JOIN p.excuse JOIN p.member " +
            "WHERE p.status = :status"
    )
    List<PostSearchDto> findAllSearchDtoByStatus(@Param("status") Post.Status status);

    // 게시물 목록용 (페이지)
    @EntityGraph(attributePaths = {"member", "member.memberRank", "excuse"})
    @Query("SELECT p FROM Post p " +
            "WHERE p.status = :status " +
            "ORDER BY p.createdAt DESC"
    )
    Page<Post> findPostForPage(Pageable pageable, @Param("status") Post.Status status);

    // 순수천수 Top 게시물 조회 (DTO 데이터)
    @Query(value = "SELECT p.id, (p.upvote_count - p.downvote_count) AS net_likes " +
            "FROM post p " +
            "WHERE p.status = 'ACTIVE' " +
            "ORDER BY net_likes DESC " +
            "LIMIT :amount",
            nativeQuery = true)
    List<Object[]> findTopNetLikes(@Param("amount") int amount);

    // 최근 n일간 게시글 조회
    @Query(value = "SELECT p FROM Post p " +
            "JOIN FETCH p.member m " +
            "JOIN FETCH m.memberRank " +
            "JOIN FETCH p.excuse e " +
            "WHERE p.status = :status " +
            "AND p.createdAt >= :startDateTime"
    )
    List<Post> findRecentPosts(@Param("status") Post.Status status,
                               @Param("startDateTime") LocalDateTime startDateTime
    );

    // Post 기본 정보 조회
    @Query("SELECT p.id, p.upvoteCount, p.downvoteCount, p.createdAt FROM Post p " +
            "WHERE p.status = :status " +
            "AND p.createdAt >= :startDateTime")
    List<Object[]> findRecentPostIds(
            @Param("status") Post.Status status,
            @Param("startDateTime") LocalDateTime startDateTime
    );

    // 랜덤 게시물 n개 조회
    @Query(value = "SELECT id FROM post " +
            "WHERE status = 'ACTIVE' " +
            "ORDER BY RAND() " +
            "LIMIT :amount",
            nativeQuery = true
    )
    List<Long> findRandomPostsId(@Param("amount") int amount);

    // 랜덤 게시물 n개 조회 (최근 m일간)
    @Query(value = "SELECT id FROM post " +
            "WHERE status = 'ACTIVE' " +
            "AND created_at >= :startDateTime " +
            "ORDER BY RAND() " +
            "LIMIT :amount",
            nativeQuery = true
    )
    List<Long> findRandomPostsId(@Param("amount") int amount, @Param("startDateTime") LocalDateTime startDateTime);

    @Query("SELECT p FROM Post p WHERE p.id IN :postIds")
    List<Post> findAllByIdList(@Param("postIds") List<Long> postIds);

    // 게시물 상세 조회용
    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.member m " +
            "JOIN FETCH m.memberRank " +
            "JOIN FETCH p.excuse e " +
            "LEFT JOIN FETCH p.votes " +
            "WHERE p.id = :id")
    Optional<Post> findByIdForDetail(@Param("id") Long id);

    // 댓글 조회
    @Query("SELECT c FROM Comment c " +
            "WHERE c.post.id = :targetId " +
            "AND c.status = 'ACTIVE'")
    Page<Comment> findCommentsByPostId(@Param("targetId") long postId, Pageable pageable);

    // 태그 필터링용 - Post id와 태그만 조회 (Tags 후처리 필요) - 태그 있는 것만
    @Query("SELECT p.id, CONCAT(t.category, ':', t.value) FROM Post p " +
            "JOIN p.excuse e " +
            "JOIN e.tags t " +  // 태그 없는 건 포함 x
            "WHERE p.id IN :postIds AND p.status = :status")
    List<Object[]> findPostIdAndTagsInnerJoin(@Param("postIds") List<Long> postIds,
                                              @Param("status") Post.Status status);

    // 태그 필터링용 - Post id와 태그만 조회 (Tags 후처리 필요) - 태그 없는 것도
    @Query("SELECT p.id, CASE WHEN t.category IS NULL THEN NULL ELSE CONCAT(t.category, ':', t.value) END " +
            "FROM Post p " +
            "JOIN p.excuse e " +
            "LEFT JOIN e.tags t " +  // 태그 없는 것도 포함
            "WHERE p.id IN :postIds AND p.status = :status")
    List<Object[]> findPostIdAndTagsLeftJoin(@Param("postIds") List<Long> postIds,
                                             @Param("status") Post.Status status);
}

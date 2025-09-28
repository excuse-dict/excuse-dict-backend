package net.whgkswo.excuse_bundle.entities.posts.core.repository;

import net.whgkswo.excuse_bundle.entities.posts.comments.entity.Comment;
import net.whgkswo.excuse_bundle.entities.posts.core.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    // 게시물 목록용
    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.member m " +
            "JOIN FETCH m.memberRank " +
            "JOIN FETCH p.excuse e " +
            "WHERE p.status = :status " +
            "ORDER BY p.createdAt DESC"
    )
    List<Post> findAllForList(@Param("status") Post.Status status);

    // 순추천수 Top 게시물 조회
    @Query(value = "SELECT p FROM Post p " +
            "JOIN FETCH p.member m " +
            "JOIN FETCH m.memberRank " +
            "JOIN FETCH p.excuse e " +
            "WHERE p.status = :status " +
            "ORDER BY p.upvoteCount - p.downvoteCount DESC ",
            countQuery = "SELECT COUNT(p) FROM Post p WHERE p.status = :status"
    )
    Page<Post> findTopNetLikes(Pageable pageable, @Param("status") Post.Status status);

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

    // 랜덤 게시물 n개 조회
    @Query(value = "SELECT * FROM post " +
            "WHERE status = 'ACTIVE' " +
            "ORDER BY RAND() " +
            "LIMIT :amount",
            nativeQuery = true
    )
    List<Post> findRandomPosts(@Param("amount") int amount);

    // 랜덤 게시물 n개 조회 (최근 m일간)
    @Query(value = "SELECT * FROM post " +
            "WHERE status = 'ACTIVE' " +
            "AND created_at >= CURRENT_DATE - :maxDaysAgo " +
            "ORDER BY RAND() " +
            "LIMIT :amount",
            nativeQuery = true
    )
    List<Post> findRandomPosts(@Param("amount") int amount, @Param("maxDaysAgo") int maxDaysAgo);

    // votes와 member까지 함께 조회
    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.votes v " +
            "LEFT JOIN FETCH v.member " +
            "WHERE p.id IN :postIds")
    List<Post> findAllByIdWithVotes(@Param("postIds") List<Long> postIds);

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
}

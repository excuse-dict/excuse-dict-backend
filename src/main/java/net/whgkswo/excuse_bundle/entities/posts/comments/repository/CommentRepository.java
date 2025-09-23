package net.whgkswo.excuse_bundle.entities.posts.comments.repository;

import net.whgkswo.excuse_bundle.entities.posts.comments.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Comment -> Member 지연 로딩으로 인한 null 문제 방지차 Fetch join 사용
    @Query("SELECT c FROM Comment c " +
            "JOIN FETCH c.member " +
            "WHERE c.id = :commentId")
    Optional<Comment> findByCommentId(@Param("commentId") long commentId);

    // 랜덤 댓글 n개 조회
    @Query(value = "SELECT * FROM comment c " +
            "WHERE c.status = 'ACTIVE' " +
            "ORDER BY RAND() " +
            "LIMIT :amount",
            nativeQuery = true
    )
    List<Comment> findRandomComments(@Param("amount") int amount);

    // 랜덤 댓글 n개 조회 (최근 m일간)
    @Query(value = "SELECT * FROM comment c " +
            "WHERE c.status = 'ACTIVE' " +
            "AND c.createdAt >= CURRENT_DATE - :maxDaysAgo " +
            "ORDER BY RAND() " +
            "LIMIT :amount",
            nativeQuery = true
    )
    List<Comment> findRandomComments(@Param("amount") int amount, @Param("maxDaysAgo") int maxDaysAgo);
}

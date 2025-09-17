package net.whgkswo.excuse_bundle.entities.posts.core.repository;

import net.whgkswo.excuse_bundle.entities.posts.comments.entity.Comment;
import net.whgkswo.excuse_bundle.entities.posts.core.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    // 게시물 목록용
    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.member m " +
            "JOIN FETCH m.memberRank " +
            "JOIN FETCH p.excuse e " +
            "JOIN FETCH e.tags " +
            "WHERE p.status = :status")
    Page<Post> findAllForList(Pageable pageable, @Param("status") Post.Status status);

    // 순추천수 Top 게시물 조회
    @Query(value = "SELECT p FROM Post p " +
            "JOIN FETCH p.member m " +
            "JOIN FETCH m.memberRank " +
            "JOIN FETCH p.excuse e " +
            "JOIN FETCH e.tags " +
            "WHERE p.status = :status " +
            "ORDER BY p.upvoteCount - p.downvoteCount DESC ",
            countQuery = "SELECT COUNT(p) FROM Post p"
    )
    Page<Post> findTopNetLikes(Pageable pageable, @Param("status") Post.Status status);

    // 게시물 상세 조회용
    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.member m " +
            "JOIN FETCH m.memberRank " +
            "JOIN FETCH p.excuse e " +
            "JOIN FETCH e.tags " +
            "WHERE p.id = :id")
    Optional<Post> findByIdForDetail(@Param("id") Long id);

    // 댓글 조회
    @Query("SELECT c FROM Comment c " +
            "WHERE c.post.id = :targetId " +
            "AND c.status = 'ACTIVE' " +
            "ORDER BY c.createdAt DESC")
    Page<Comment> findCommentsByPostId(@Param("targetId") long postId, Pageable pageable);
}

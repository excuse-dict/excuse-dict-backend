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
            "LEFT JOIN FETCH p.member m " +
            "LEFT JOIN FETCH m.memberRank " +
            "LEFT JOIN FETCH p.excuse e " +
            "LEFT JOIN FETCH e.tags " +
            "WHERE p.status = :status")
    Page<Post> findAllForList(Pageable pageable, @Param("status") Post.Status status);

    // 게시물 상세 조회용
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.member m " +
            "LEFT JOIN FETCH m.memberRank " +
            "LEFT JOIN FETCH p.excuse e " +
            "LEFT JOIN FETCH e.tags " +
            "WHERE p.id = :id")
    Optional<Post> findByIdForDetail(@Param("id") Long id);

    // 댓글 조회
    @Query("SELECT c FROM Comment c " +
            "WHERE c.post.id = :targetId " +
            "ORDER BY c.createdAt DESC")
    Page<Comment> findCommentsByPostId(@Param("targetId") long postId, Pageable pageable);
}

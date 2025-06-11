package net.whgkswo.excuse_bundle.entities.posts.comments.repository;

import net.whgkswo.excuse_bundle.entities.posts.comments.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Optional<Comment> findByIdAndStatus(long commentId, Comment.Status status);
}

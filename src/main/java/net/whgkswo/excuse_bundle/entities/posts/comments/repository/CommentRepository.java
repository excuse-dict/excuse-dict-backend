package net.whgkswo.excuse_bundle.entities.posts.comments.repository;

import net.whgkswo.excuse_bundle.entities.posts.comments.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

}

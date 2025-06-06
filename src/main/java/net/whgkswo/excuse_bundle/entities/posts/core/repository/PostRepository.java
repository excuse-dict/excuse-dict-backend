package net.whgkswo.excuse_bundle.entities.posts.core.repository;

import net.whgkswo.excuse_bundle.entities.posts.core.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}

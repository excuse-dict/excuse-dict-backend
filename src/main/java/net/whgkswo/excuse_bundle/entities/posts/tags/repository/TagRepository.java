package net.whgkswo.excuse_bundle.entities.posts.tags.repository;

import net.whgkswo.excuse_bundle.entities.posts.tags.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByCategoryAndValue(Tag.Category category, String value);
}

package net.whgkswo.excuse_bundle.entities.posts.tags.repository;

import net.whgkswo.excuse_bundle.entities.posts.tags.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByCategoryAndValue(Tag.Category category, String value);

    List<Tag> findByCategoryIn(List<Tag.Category> categories);

    // 랜덤 태그 n개 조회
    @Query(value = "SELECT * FROM tag ORDER BY RAND() LIMIT :amount", nativeQuery = true)
    List<Tag> findRandomTags(@Param("amount") int amount);
}

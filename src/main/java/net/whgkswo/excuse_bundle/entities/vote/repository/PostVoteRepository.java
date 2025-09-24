package net.whgkswo.excuse_bundle.entities.vote.repository;

import net.whgkswo.excuse_bundle.entities.posts.core.entity.PostVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostVoteRepository extends JpaRepository<PostVote, Long> {

    @Query("SELECT pv FROM PostVote pv WHERE pv.post.id IN :postIds")
    List<PostVote> findAllByPostIds(@Param("postIds") List<Long> postIds);
}

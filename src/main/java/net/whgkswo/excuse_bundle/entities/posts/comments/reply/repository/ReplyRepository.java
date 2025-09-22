package net.whgkswo.excuse_bundle.entities.posts.comments.reply.repository;

import net.whgkswo.excuse_bundle.entities.posts.comments.entity.AbstractComment;
import net.whgkswo.excuse_bundle.entities.posts.comments.reply.entity.Reply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReplyRepository extends JpaRepository<Reply, Long> {

    Page<Reply> findByCommentIdAndStatus(long commentId, AbstractComment.Status status, Pageable pageable);

    // 지연로딩 문제로 fetch join 사용
    @Query("SELECT r FROM Reply r " +
            "JOIN FETCH r.member " +
            "WHERE r.id = :replyId")
    Optional<Reply> findByReplyId(@Param("replyId") long replyId);

    // 랜덤 답글 n개 조회
    @Query(value = "SELECT * FROM reply " +
            "ORDER BY RAND() " +
            "LIMIT :amount",
            nativeQuery = true
    )
    List<Reply> findRandomReplies(@Param("amount") int amount);
}

package net.whgkswo.excuse_bundle.entities.posts.comments.reply.repository;

import net.whgkswo.excuse_bundle.entities.posts.comments.entity.AbstractComment;
import net.whgkswo.excuse_bundle.entities.posts.comments.reply.entity.Reply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReplyRepository extends JpaRepository<Reply, Long> {

    Page<Reply> findByCommentIdAndStatus(long commentId, AbstractComment.Status status, Pageable pageable);
}

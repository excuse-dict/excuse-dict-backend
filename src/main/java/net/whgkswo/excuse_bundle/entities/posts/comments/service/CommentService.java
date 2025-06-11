package net.whgkswo.excuse_bundle.entities.posts.comments.service;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.entities.posts.comments.entity.Comment;
import net.whgkswo.excuse_bundle.entities.posts.comments.repository.CommentRepository;
import net.whgkswo.excuse_bundle.exceptions.BusinessLogicException;
import net.whgkswo.excuse_bundle.exceptions.ExceptionType;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    private Optional<Comment> findComment(long commentId){
        return commentRepository.findById(commentId);
    }

    public Comment getComment(long commentId){
        return findComment(commentId).orElseThrow(() -> new BusinessLogicException(ExceptionType.COMMENT_NOT_FOUND));
    }
}

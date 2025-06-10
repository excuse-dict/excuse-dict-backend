package net.whgkswo.excuse_bundle.entities.posts.core.dto;

import net.whgkswo.excuse_bundle.entities.posts.comments.entity.Comment;
import net.whgkswo.excuse_bundle.responses.dtos.Dto;

import java.time.LocalDateTime;
import java.util.List;

public record PostCommentDto(
        List<Comment> comments
) implements Dto {
}

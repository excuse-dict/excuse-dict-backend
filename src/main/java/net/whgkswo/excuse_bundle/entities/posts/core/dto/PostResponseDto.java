package net.whgkswo.excuse_bundle.entities.posts.core.dto;

import java.time.LocalDateTime;

public record PostResponseDto(
        String author,
        String situation,
        String excuse,
        int upvoteCount,
        int downvoteCount,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
}

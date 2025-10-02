package net.whgkswo.excuse_bundle.ranking.dto;

import net.whgkswo.excuse_bundle.entities.posts.comments.entity.Comment;
import net.whgkswo.excuse_bundle.entities.posts.post_core.entity.PostVote;

import java.time.LocalDateTime;
import java.util.List;

public record RecentHotPostDto(
        long id,
        int upvoteCount,
        int downvoteCount,
        List<PostVote> votes,
        List<Comment> comments,
        LocalDateTime createdAt
) {
}

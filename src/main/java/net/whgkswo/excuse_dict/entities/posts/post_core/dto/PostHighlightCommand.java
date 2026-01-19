package net.whgkswo.excuse_dict.entities.posts.post_core.dto;

public record PostHighlightCommand(
        long postId,
        Long memberId
) {
}

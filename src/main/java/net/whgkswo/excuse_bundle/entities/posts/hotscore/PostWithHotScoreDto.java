package net.whgkswo.excuse_bundle.entities.posts.hotscore;

import net.whgkswo.excuse_bundle.entities.posts.core.entity.Post;

public record PostWithHotScoreDto(Post post, int hotScore) {
}

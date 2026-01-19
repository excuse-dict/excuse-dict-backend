package net.whgkswo.excuse_dict.entities.posts.hotscore;

import net.whgkswo.excuse_dict.entities.posts.post_core.entity.Post;

public record PostWithHotScoreDto(Post post, int hotScore) {
}

package net.whgkswo.excuse_bundle.entities.posts.post_core.dto;

import net.whgkswo.excuse_bundle.general.responses.dtos.Dto;
import org.springframework.data.domain.Page;

public record PostOverviewResponseDto(
        Page<PostResponseDto> recentPosts,
        Page<WeeklyTopPostResponseDto> weeklyTopPosts,
        Page<PostResponseDto> hallOfFamePosts
) implements Dto {
}

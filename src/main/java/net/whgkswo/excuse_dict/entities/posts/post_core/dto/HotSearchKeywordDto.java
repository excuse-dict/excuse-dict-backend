package net.whgkswo.excuse_dict.entities.posts.post_core.dto;

import net.whgkswo.excuse_dict.general.responses.dtos.Dto;

public record HotSearchKeywordDto (
        String keyword,
        int count
) implements Dto{
}

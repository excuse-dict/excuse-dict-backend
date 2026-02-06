package net.whgkswo.excuse_dict.search.dto;

import net.whgkswo.excuse_dict.general.responses.dtos.Dto;

public record HotSearchKeywordDto (
        String keyword,
        int count,
        Integer rankChange
) implements Dto{
}

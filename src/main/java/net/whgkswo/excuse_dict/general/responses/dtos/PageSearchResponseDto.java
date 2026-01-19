package net.whgkswo.excuse_dict.general.responses.dtos;

import net.whgkswo.excuse_dict.general.responses.page.PageInfo;
import org.springframework.data.domain.Page;

public record PageSearchResponseDto<T>(
        Page<T> page,
        PageInfo pageInfo
) implements Dto {
}

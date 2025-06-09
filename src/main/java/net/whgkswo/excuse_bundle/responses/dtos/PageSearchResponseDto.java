package net.whgkswo.excuse_bundle.responses.dtos;

import net.whgkswo.excuse_bundle.responses.page.PageInfo;
import org.springframework.data.domain.Page;

public record PageSearchResponseDto<T>(
        Page<T> page,
        PageInfo pageInfo
) implements Dto {
}

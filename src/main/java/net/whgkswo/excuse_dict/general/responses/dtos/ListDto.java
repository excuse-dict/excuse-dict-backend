package net.whgkswo.excuse_dict.general.responses.dtos;

import java.util.List;

public record ListDto<T extends Dto>(
        List<T> list
) implements Dto {
}

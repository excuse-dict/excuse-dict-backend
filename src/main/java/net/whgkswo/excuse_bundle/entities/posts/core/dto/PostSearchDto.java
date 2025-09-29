package net.whgkswo.excuse_bundle.entities.posts.core.dto;

import net.whgkswo.excuse_bundle.search.Searchable;

import java.time.LocalDateTime;

public record PostSearchDto(
        long id,
        String situation,
        String excuse,
        String authorName,
        LocalDateTime createdAt
) implements Searchable {

    public static final String PACKAGE_PATH =
            "net.whgkswo.excuse_bundle.entities.posts.core.dto.PostSearchDto";
}

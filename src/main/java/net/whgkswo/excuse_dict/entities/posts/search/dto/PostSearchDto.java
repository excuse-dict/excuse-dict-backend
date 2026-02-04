package net.whgkswo.excuse_dict.entities.posts.search.dto;

import net.whgkswo.excuse_dict.search.Searchable;

import java.time.LocalDateTime;

public record PostSearchDto(
        long id,
        String situation,
        String excuse,
        String authorName,
        LocalDateTime createdAt
) implements Searchable {

    public static final String PACKAGE_NAME =
            "net.whgkswo.excuse_dict.entities.posts.search.dto.PostSearchDto";
}

package net.whgkswo.excuse_bundle.entities.posts.tags.commands;

import net.whgkswo.excuse_bundle.entities.posts.tags.entity.Tag;

import java.util.List;

public record SearchTagCommand(
        List<Tag.Category> filterCategories,
        String searchValue,
        int page,
        int size) {
}

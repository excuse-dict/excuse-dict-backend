package net.whgkswo.excuse_bundle.search;

import java.util.List;

public record SearchResult<T extends Searchable> (
        T searchedContent,
        List<String> matchedWords
) {
}

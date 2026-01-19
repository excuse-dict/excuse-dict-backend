package net.whgkswo.excuse_dict.search;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record SearchResult<T extends Searchable> (
        T searchedContent,
        List<String> matchedWords
) {

    public static <T extends Searchable> Map<Long, List<String>> mapByMatchedWords(List<SearchResult<T>> searchResults){
        return searchResults.stream()
                .collect(Collectors.toMap(
                        result -> result.searchedContent().id(),
                        SearchResult::matchedWords
                ));
    }
}

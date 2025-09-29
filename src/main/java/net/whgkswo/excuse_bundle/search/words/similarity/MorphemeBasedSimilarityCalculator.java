package net.whgkswo.excuse_bundle.search.words.similarity;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.search.words.WordService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MorphemeBasedSimilarityCalculator {

    private final WordService wordService;

    public Similarity calculateSimilarity(String target, String searchInput){
        return wordService.calculateTextSimilarity(target, searchInput);
    }
}

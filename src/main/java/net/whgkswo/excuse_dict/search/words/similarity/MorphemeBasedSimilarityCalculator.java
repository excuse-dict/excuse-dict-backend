package net.whgkswo.excuse_dict.search.words.similarity;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_dict.search.words.WordHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MorphemeBasedSimilarityCalculator {

    private final WordHelper wordHelper;

    public Similarity calculateSimilarity(String target, String searchInput){
        return wordHelper.calculateTextSimilarity(target, searchInput);
    }
}

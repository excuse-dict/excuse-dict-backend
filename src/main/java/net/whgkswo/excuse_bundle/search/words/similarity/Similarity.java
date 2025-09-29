package net.whgkswo.excuse_bundle.search.words.similarity;

import java.util.Collections;
import java.util.List;

public record Similarity(List<String> matchedWords, double similarityScore) {

    public static final Similarity NO_MATCH = new Similarity(Collections.emptyList(), 0);
    public static final Similarity EXACTLY_SAME = new Similarity(Collections.emptyList(), 1);

    public static Similarity of(List<String> words, double similarity) {return new Similarity(words, similarity);}
}

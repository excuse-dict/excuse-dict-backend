package net.whgkswo.excuse_dict.gemini.fallback;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class GeminiFallbackProvider<T> {
    @JsonIgnore
    public abstract T getFallback();
}

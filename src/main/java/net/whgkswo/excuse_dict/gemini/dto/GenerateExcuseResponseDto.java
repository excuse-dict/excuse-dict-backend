package net.whgkswo.excuse_dict.gemini.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.whgkswo.excuse_dict.gemini.fallback.GeminiFallbackProvider;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class GenerateExcuseResponseDto extends GeminiFallbackProvider<GenerateExcuseResponseDto> {

    private List<String> data;

    private static final GenerateExcuseResponseDto FALLBACK = new GenerateExcuseResponseDto(List.of("Gemini 요청 실패"));

    @Override
    public GenerateExcuseResponseDto getFallback() {
        return FALLBACK;
    }

    @Override
    public String toString() {
        return data.toString();
    }
}

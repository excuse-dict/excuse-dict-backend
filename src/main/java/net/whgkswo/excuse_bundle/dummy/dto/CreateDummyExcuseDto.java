package net.whgkswo.excuse_bundle.dummy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.whgkswo.excuse_bundle.entities.excuses.dto.ExcuseRequestDto;
import net.whgkswo.excuse_bundle.gemini.fallback.GeminiFallbackProvider;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CreateDummyExcuseDto extends GeminiFallbackProvider<CreateDummyExcuseDto> {

    private List<ExcuseRequestDto> excuses;

    private static final CreateDummyExcuseDto FALLBACK = new CreateDummyExcuseDto(List.of());

    @Override
    public CreateDummyExcuseDto getFallback() {
        return FALLBACK;
    }
}

package net.whgkswo.excuse_bundle.entities.excuses.dto;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.whgkswo.excuse_bundle.gemini.fallback.GeminiFallbackProvider;
import net.whgkswo.excuse_bundle.general.responses.dtos.Dto;
import org.hibernate.validator.constraints.Length;

import java.util.Optional;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ExcuseRequestDto extends GeminiFallbackProvider<ExcuseRequestDto> implements Dto {
    @Length(min = 3, message = "상황은 3글자 이상으로 입력해주세요.")
    private String situation;

    @Length(min = 5, max = 100, message = "핑계는 5~100글자 사이로 입력해주세요.")
    private String excuse;

    @Nullable
    private Set<String> tags;

    private static final ExcuseRequestDto DUMMY = new ExcuseRequestDto("", "", null);

        public UpdateExcuseCommand toUpdateCommand(){
                return new UpdateExcuseCommand(
                        Optional.ofNullable(situation),
                        Optional.ofNullable(excuse),
                        Optional.ofNullable(tags)
                );
        }

        @Override
        public ExcuseRequestDto getFallback(){
            return DUMMY;
        }
}

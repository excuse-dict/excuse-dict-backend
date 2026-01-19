package net.whgkswo.excuse_dict.entities.excuses.dto;

import jakarta.annotation.Nullable;
import net.whgkswo.excuse_dict.general.responses.dtos.Dto;
import org.hibernate.validator.constraints.Length;

import java.util.Optional;
import java.util.Set;

public record ExcuseRequestDto(
        @Length(min = 3, message = "상황은 3글자 이상으로 입력해주세요.")
        String situation,

        @Length(min = 5, max = 100, message = "핑계는 5~100글자 사이로 입력해주세요.")
        String excuse,

        @Nullable
        Set<String> tags
) implements Dto {


        public UpdateExcuseCommand toUpdateCommand(){
                return new UpdateExcuseCommand(
                        Optional.ofNullable(situation),
                        Optional.ofNullable(excuse),
                        Optional.ofNullable(tags)
                );
        }
}

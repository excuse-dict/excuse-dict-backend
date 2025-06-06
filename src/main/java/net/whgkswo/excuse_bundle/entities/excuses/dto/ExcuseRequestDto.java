package net.whgkswo.excuse_bundle.entities.excuses.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import net.whgkswo.excuse_bundle.entities.posts.tags.entity.Tag;
import net.whgkswo.excuse_bundle.responses.dtos.Dto;
import org.hibernate.validator.constraints.Length;

import java.util.List;

public record ExcuseRequestDto(
        @NotBlank(message = "상황을 입력해주세요.") String situation,
        @Length(min = 10, max = 500, message = "핑계는 10~500글자 사이로 입력해주세요.") String excuse,
        @Nullable List<Tag> tags
        ) implements Dto {
}

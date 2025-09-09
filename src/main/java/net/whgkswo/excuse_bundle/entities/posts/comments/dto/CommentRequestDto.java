package net.whgkswo.excuse_bundle.entities.posts.comments.dto;

import net.whgkswo.excuse_bundle.responses.dtos.Dto;
import org.hibernate.validator.constraints.Length;

public record CommentRequestDto(
        @Length(min = 1, max = 500, message = "댓글은 1~500자 사이로 입력해야 합니다.")
        String comment
) implements Dto {
}

package net.whgkswo.excuse_bundle.entities.posts.core.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.whgkswo.excuse_bundle.entities.excuses.dto.ExcuseResponseDto;
import net.whgkswo.excuse_bundle.entities.members.core.dto.MemberResponseDto;
import net.whgkswo.excuse_bundle.general.responses.dtos.Dto;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostSummaryResponseDto implements Dto {
    private long postId;
    private MemberResponseDto author;
    private ExcuseResponseDto excuse;
    private int upvoteCount;
    private int downvoteCount;
    private int commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}

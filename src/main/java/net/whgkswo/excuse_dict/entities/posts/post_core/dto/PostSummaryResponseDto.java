package net.whgkswo.excuse_dict.entities.posts.post_core.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.whgkswo.excuse_dict.entities.excuses.dto.ExcuseResponseDto;
import net.whgkswo.excuse_dict.entities.members.core.dto.MemberResponseDto;
import net.whgkswo.excuse_dict.general.responses.dtos.Dto;

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

package net.whgkswo.excuse_bundle.entities.posts.core.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.whgkswo.excuse_bundle.entities.excuses.dto.ExcuseResponseDto;
import net.whgkswo.excuse_bundle.entities.members.core.dto.MemberResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.core.entity.PostVote;
import net.whgkswo.excuse_bundle.responses.dtos.Dto;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
public class PostResponseDto extends PostSummaryResponseDto implements Dto {
    private PostVote myVote;

    public PostResponseDto(long postId,
                           MemberResponseDto author,
                           ExcuseResponseDto excuse,
                           int upvoteCount,
                           int downvoteCount,
                           int commentCount,
                           LocalDateTime createdAt,
                           LocalDateTime modifiedAt,
                           PostVote myVote){
        super(postId, author, excuse, upvoteCount, downvoteCount, commentCount, createdAt, modifiedAt);
        this.myVote = myVote;
    }
}

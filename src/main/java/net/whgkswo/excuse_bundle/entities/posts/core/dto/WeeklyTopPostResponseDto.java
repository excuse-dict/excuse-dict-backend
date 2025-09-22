package net.whgkswo.excuse_bundle.entities.posts.core.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.whgkswo.excuse_bundle.entities.excuses.dto.ExcuseResponseDto;
import net.whgkswo.excuse_bundle.entities.members.core.dto.MemberResponseDto;
import net.whgkswo.excuse_bundle.entities.vote.dto.PostVoteDto;
import net.whgkswo.excuse_bundle.general.responses.dtos.Dto;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class WeeklyTopPostResponseDto extends PostResponseDto implements Dto {

    private int hotScore;

    public WeeklyTopPostResponseDto(
            long postId,
            MemberResponseDto author,
            ExcuseResponseDto excuse,
            int upvoteCount,
            int downvoteCount,
            int commentCount,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt,
            PostVoteDto myVote,
            int hotScore) {
        super(postId, author, excuse, upvoteCount, downvoteCount, commentCount, createdAt, modifiedAt, myVote);
        this.hotScore = hotScore;
    }
}

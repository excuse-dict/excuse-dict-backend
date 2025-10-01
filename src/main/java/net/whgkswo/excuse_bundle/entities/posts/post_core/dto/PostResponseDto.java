package net.whgkswo.excuse_bundle.entities.posts.post_core.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.whgkswo.excuse_bundle.entities.excuses.dto.ExcuseResponseDto;
import net.whgkswo.excuse_bundle.entities.members.core.dto.MemberResponseDto;
import net.whgkswo.excuse_bundle.entities.vote.dto.PostVoteDto;
import net.whgkswo.excuse_bundle.general.responses.dtos.Dto;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PostResponseDto extends PostSummaryResponseDto implements Dto {
    private PostVoteDto myVote;
    private List<String> matchedWords;
    private List<String> matchedTags;

    public PostResponseDto(long postId,
                           MemberResponseDto author,
                           ExcuseResponseDto excuse,
                           int upvoteCount,
                           int downvoteCount,
                           int commentCount,
                           LocalDateTime createdAt,
                           LocalDateTime modifiedAt,
                           PostVoteDto myVote,
                           List<String> matchedWords,
                           List<String> matchedTags){

        super(postId, author, excuse, upvoteCount, downvoteCount, commentCount, createdAt, modifiedAt);

        this.myVote = myVote;
        this.matchedWords = matchedWords;
        this.matchedTags = matchedTags;
    }
}

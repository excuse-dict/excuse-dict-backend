package net.whgkswo.excuse_bundle.entities.posts.post_core.service;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.entities.posts.post_core.dto.PostResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.post_core.dto.PostSummaryResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.post_core.entity.Post;
import net.whgkswo.excuse_bundle.entities.posts.post_core.entity.PostVote;
import net.whgkswo.excuse_bundle.entities.posts.post_core.mapper.PostMapper;
import net.whgkswo.excuse_bundle.entities.vote.mapper.VoteMapper;
import net.whgkswo.excuse_bundle.entities.vote.repository.PostVoteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostDtoConverter {

    private final PostVoteRepository postVoteRepository;
    private final PostMapper postMapper;
    private final VoteMapper voteMapper;

    // Post -> PostResponseDto 변환 (Page -> Page)
    public Page<PostResponseDto> convertPostsToResponseDtos(
            Page<Post> posts,
            Long memberId,
            Map<Long, List<String>> matchedWordsMap,
            Map<Long, List<String>> matchedTagsMap
    ){
        if(posts.isEmpty()) return new PageImpl<>(Collections.emptyList());

        List<PostResponseDto> responses = convertPostsToResponseDtos(
                posts.getContent(),
                memberId,
                matchedWordsMap,
                matchedTagsMap
        );

        return new PageImpl<>(
                responses,
                posts.getPageable(),
                posts.getTotalElements()
        );
    }

    // Post -> PostResponseDto 변환 (List -> List)
    public List<PostResponseDto> convertPostsToResponseDtos(
            List<Post> posts,
            Long memberId,
            Map<Long, List<String>> matchedWordsMap,
            Map<Long, List<String>> matchedTagsMap
    ) {
        if (posts.isEmpty()) {
            return Collections.emptyList();
        }

        // 요청한 사용자가 누른 Post별 추천/비추천
        Map<Long, PostVote> myVotesByPostId = Collections.emptyMap();

        if (memberId != null) { // memberId 있을 때만 조회
            List<Long> postIds = posts.stream().map(Post::getId).toList();
            List<PostVote> myVotes = postVoteRepository.findAllByPostIdsAndMemberId(postIds, memberId);

            // Post ID 별로 맵에다 넣기
            myVotesByPostId = myVotes.stream()
                    .collect(Collectors.toMap(
                            vote -> vote.getPost().getId(),
                            vote -> vote
                    ));
        }

        Map<Long, PostVote> finalMyVotesByPostId = myVotesByPostId; // 재선언으로 effective final 만들기

        return posts.stream().map(post -> {
            // 게시물에 해당하는 Vote
            PostVote myVote = finalMyVotesByPostId.get(post.getId());

            // post -> summary
            PostSummaryResponseDto summary = postMapper.postTomultiPostSummaryResponseDto(post);

            List<String> matchedWords = matchedWordsMap != null
                    ? matchedWordsMap.get(post.getId())
                    : null;

            List<String> matchedTags = matchedTagsMap != null
                    ? matchedTagsMap.get(post.getId())
                    : null;

            // summary -> response
            return postMapper.postSummaryResponseDtoToPostResponseDto(
                    summary,
                    myVote != null ? voteMapper.postVoteToPostVoteDto(myVote) : null,
                    matchedWords,
                    matchedTags
            );
        }).toList();
    }
}

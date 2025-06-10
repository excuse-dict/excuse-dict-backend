package net.whgkswo.excuse_bundle.entities.posts.core.service;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.entities.excuses.Excuse;
import net.whgkswo.excuse_bundle.entities.excuses.service.ExcuseService;
import net.whgkswo.excuse_bundle.entities.members.core.entitiy.Member;
import net.whgkswo.excuse_bundle.entities.members.core.service.MemberService;
import net.whgkswo.excuse_bundle.entities.posts.core.dto.PostCommentDto;
import net.whgkswo.excuse_bundle.entities.posts.core.dto.PostResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.core.dto.VoteCommand;
import net.whgkswo.excuse_bundle.entities.posts.core.entity.Post;
import net.whgkswo.excuse_bundle.entities.posts.core.mapper.PostMapper;
import net.whgkswo.excuse_bundle.entities.posts.core.repository.PostRepository;
import net.whgkswo.excuse_bundle.entities.posts.core.entity.PostVote;
import net.whgkswo.excuse_bundle.entities.vote.entity.VoteType;
import net.whgkswo.excuse_bundle.exceptions.BusinessLogicException;
import net.whgkswo.excuse_bundle.exceptions.ExceptionType;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PostService {
    private final ExcuseService excuseService;
    private final MemberService memberService;
    private final PostRepository postRepository;
    private final PostMapper postMapper;

    @Transactional
    public Post createPost(long memberId, String situation, String excuseStr, Set<String> tags){

        Member member = memberService.findById(memberId);
        Excuse excuse = excuseService.createExcuse(situation, excuseStr, tags);

        Post post = new Post();

        post.setExcuse(excuse);
        post.setMember(member);

        return postRepository.save(post);
    }

    @Transactional(readOnly = true)
    public Page<PostResponseDto> getPosts(GetPostsCommand command){

        Page<Post> posts = postRepository.findAllForList(command.pageable());

        return postMapper.postsToMultiPostResponseDtos(posts)
                .map(summary -> {
                    Post post = findPost(summary.getPostId());
                    Optional<PostVote> optionalVote = getVoteFromCertainMember(post, command.memberId());

                    return postMapper.summaryToMultiPostResponseDto(summary, post, optionalVote);
                });
    }

    @Transactional(readOnly = true)
    public PostCommentDto getPost(long postId){
        Post post = findPost(postId);

        return postMapper.postToPostCommentDto(post);
    }

    @Transactional(readOnly = true)
    private Post findPost(long postId){
        Optional<Post> optionalPost = postRepository.findByIdForDetail(postId);
        Post post = optionalPost.orElseThrow(() -> new BusinessLogicException(ExceptionType.POST_NOT_FOUND));

        return post;
    }

    @Transactional
    public boolean vote(VoteCommand command){
        Optional<Post> optionalPost = postRepository.findById(command.postId());
        Post post = optionalPost.orElseThrow(() -> new BusinessLogicException(ExceptionType.POST_NOT_FOUND));

        // 자추 불가
        /*if(post.getMember().getId().equals(command.memberId()))
            throw new BusinessLogicException(ExceptionType.SELF_VOTE_NOT_ALLOWED);*/

        // 이미 추천/비추천했는지
        Optional<PostVote> optionalVote = getVoteFromCertainMember(post, command.memberId());
        if(optionalVote.isPresent()){
            // 추천 비추천 취소
            PostVote vote = optionalVote.get();
            if(vote.getType().equals(command.voteType())){ // 같은 타입일 때만 취소
                removeVote(post, vote);
                return false; // 취소됨
            }else{ // 추천 눌렀는데 취소 안하고 비추천 누르거나 그 반대
                throw new BusinessLogicException(ExceptionType.alreadyVoted(command.voteType()));
            }
        }else{
            // 추천 비추천 등록
            saveVote(post, command.voteType(), command.memberId());
            return true; // 생성됨
        }
    }

    // 게시물에 특정 유저가 추천/비추천을 눌렀는지 조회
    private Optional<PostVote> getVoteFromCertainMember(Post post, Long memberId) {
        if(memberId == null) return Optional.empty();

        return post.getVotes().stream()
                .filter(vote -> vote.getMember().getId().equals(memberId))
                .findFirst();
    }

    // 추천/비추천 취소 (이미 앞에서 post-myVote 관계 검증했을 때만 사용)
    private void removeVote(Post post, PostVote vote){
        post.getVotes().remove(vote);

        postRepository.save(post);
    }

    // 추천/비추천 등록
    private void saveVote(Post post, VoteType type, long memberId){
        Member member = memberService.findById(memberId);

        PostVote vote = new PostVote(type, post, member);

        post.addVote(vote);
        postRepository.save(post);
    }
}

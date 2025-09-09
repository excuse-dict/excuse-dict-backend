package net.whgkswo.excuse_bundle.entities.posts.core.service;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.entities.excuses.Excuse;
import net.whgkswo.excuse_bundle.entities.excuses.service.ExcuseService;
import net.whgkswo.excuse_bundle.entities.members.core.entitiy.Member;
import net.whgkswo.excuse_bundle.entities.members.core.service.MemberService;
import net.whgkswo.excuse_bundle.entities.posts.core.dto.PostResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.core.dto.VoteCommand;
import net.whgkswo.excuse_bundle.entities.posts.core.entity.Post;
import net.whgkswo.excuse_bundle.entities.posts.core.mapper.PostMapper;
import net.whgkswo.excuse_bundle.entities.posts.core.repository.PostRepository;
import net.whgkswo.excuse_bundle.entities.posts.core.entity.PostVote;
import net.whgkswo.excuse_bundle.entities.vote.mapper.VoteMapper;
import net.whgkswo.excuse_bundle.entities.vote.service.VoteService;
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
    private final VoteService voteService;
    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final VoteMapper voteMapper;

    // 게시글 등록
    @Transactional
    public Post createPost(long memberId, String situation, String excuseStr, Set<String> tags){

        Member member = memberService.findById(memberId);
        Excuse excuse = excuseService.createExcuse(situation, excuseStr, tags);

        Post post = new Post();

        post.setExcuse(excuse);
        post.setMember(member);

        return postRepository.save(post);
    }

    // 게시글 리스트 조회
    @Transactional(readOnly = true)
    public Page<PostResponseDto> getPosts(GetPostsCommand command){

        Page<Post> posts = postRepository.findAllForList(command.pageable(), Post.Status.ACTIVE);

        return postMapper.postsToMultiPostResponseDtos(posts)
                .map(summary -> {
                    Post post = getPost(summary.getPostId());
                    Optional<PostVote> optionalVote = voteService.getPostVoteFromCertainMember(post, command.memberId());

                    return postMapper.summaryToMultiPostResponseDto(summary, optionalVote.map(voteMapper::postVoteToPostVoteDto));
                });
    }

    private Optional<Post> findPost(long postId){
        return postRepository.findByIdForDetail(postId);
    }

    public Post getPost(long postId){
        Optional<Post> optionalPost = findPost(postId);
        return optionalPost.orElseThrow(() -> new BusinessLogicException(ExceptionType.POST_NOT_FOUND));
    }

    // 게시글 추천
    @Transactional
    public boolean voteToPost(VoteCommand command){
        Optional<Post> optionalPost = postRepository.findById(command.targetId());
        Post post = optionalPost.orElseThrow(() -> new BusinessLogicException(ExceptionType.POST_NOT_FOUND));

        // 자추 불가
        // TODO: 주석 해제
        /*if(post.getMember().getId().equals(command.memberId()))
            throw new BusinessLogicException(ExceptionType.SELF_VOTE_NOT_ALLOWED);*/

        // 이미 추천/비추천했는지
        Optional<PostVote> optionalVote = voteService.getPostVoteFromCertainMember(post, command.memberId());
        if(optionalVote.isPresent()){
            // 추천 비추천 취소
            PostVote vote = optionalVote.get();
            if(vote.getVoteType().equals(command.voteType())){ // 같은 타입일 때만 취소
                voteService.removePostVote(post, vote);
                return false; // 취소됨
            }else{ // 추천 눌렀는데 취소 안하고 비추천 누르거나 그 반대
                throw new BusinessLogicException(ExceptionType.alreadyVoted(command.voteType()));
            }
        }else{
            // 추천 비추천 등록
            voteService.savePostVote(post, command.voteType(), command.memberId());
            return true; // 생성됨
        }
    }
}

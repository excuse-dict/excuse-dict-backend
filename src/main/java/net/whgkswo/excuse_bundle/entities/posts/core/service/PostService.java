package net.whgkswo.excuse_bundle.entities.posts.core.service;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.auth.redis.RedisService;
import net.whgkswo.excuse_bundle.entities.excuses.Excuse;
import net.whgkswo.excuse_bundle.entities.excuses.dto.UpdateExcuseCommand;
import net.whgkswo.excuse_bundle.entities.excuses.service.ExcuseService;
import net.whgkswo.excuse_bundle.entities.members.core.entitiy.Member;
import net.whgkswo.excuse_bundle.entities.members.core.service.MemberService;
import net.whgkswo.excuse_bundle.entities.posts.core.dto.PostResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.core.dto.PostSummaryResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.core.dto.VoteCommand;
import net.whgkswo.excuse_bundle.entities.posts.core.entity.Post;
import net.whgkswo.excuse_bundle.entities.posts.core.mapper.PostMapper;
import net.whgkswo.excuse_bundle.entities.posts.core.repository.PostRepository;
import net.whgkswo.excuse_bundle.entities.posts.core.entity.PostVote;
import net.whgkswo.excuse_bundle.entities.vote.mapper.VoteMapper;
import net.whgkswo.excuse_bundle.entities.vote.service.VoteService;
import net.whgkswo.excuse_bundle.exceptions.BusinessLogicException;
import net.whgkswo.excuse_bundle.exceptions.ExceptionType;
import net.whgkswo.excuse_bundle.general.dto.DeleteCommand;
import net.whgkswo.excuse_bundle.pager.PageHelper;
import net.whgkswo.excuse_bundle.ranking.scheduler.RankingScheduler;
import net.whgkswo.excuse_bundle.ranking.service.RankingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PostService {
    private final ExcuseService excuseService;
    private final MemberService memberService;
    private final VoteService voteService;
    private final RankingService rankingService;
    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final VoteMapper voteMapper;
    private final RedisService redisService;
    private final PageHelper pageHelper;

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
                .map(summary -> mapSummaryToResponseDto(summary, command.memberId()));
    }

    private PostResponseDto mapSummaryToResponseDto(PostSummaryResponseDto summary, Long memberId){
        Post post = getPost(summary.getPostId());
        Optional<PostVote> optionalVote = voteService.getPostVoteFromCertainMember(post, memberId);

        return postMapper.postSummaryResponseDtoToPostResponseDto(summary, optionalVote.map(voteMapper::postVoteToPostVoteDto));
    }

    private Optional<Post> findPost(long postId){
        return postRepository.findByIdForDetail(postId);
    }

    public Post getPost(long postId){
        Optional<Post> optionalPost = findPost(postId);
        return optionalPost.orElseThrow(() -> new BusinessLogicException(ExceptionType.POST_NOT_FOUND));
    }

    // 순추천수 Top 게시글 조회
    public Page<Post> getTopNetLikes(Pageable pageable){
        return postRepository.findTopNetLikes(pageable, Post.Status.ACTIVE);
    }

    // 명예의 전당 게시글 조회
    public Page<PostResponseDto> getHallOfFamePosts(Pageable pageable, Long memberId){
        List<Long> postIdList = redisService.getAsList(RankingScheduler.HALL_OF_FAME_REDISKEY, Long.class);

        // ID를 바탕으로 게시글 조회
        List<Post> posts = postRepository.findAllById(postIdList);

        List<PostSummaryResponseDto> summaries = postMapper.postsToMultiPostResponseDtos(posts);

        List<PostResponseDto> dtos = summaries.stream()
                .map(summary -> mapSummaryToResponseDto(summary, memberId))
                .toList();

        return pageHelper.paginate(dtos, pageable);
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

    // 게시물 수정
    public void updatePost(long memberId, long postId, UpdateExcuseCommand command){

        Post post = getPost(postId);

        // 자기 게시물만 수정 가능
        if(memberId != post.getMember().getId()) throw new BusinessLogicException(ExceptionType.UPDATE_FORBIDDEN);

        // 명예의 전당 게시글은 수정 불가
        if(rankingService.isPostInHallOfFame(postId)) throw new BusinessLogicException(ExceptionType.HALL_OF_FAME_PROTECTED);

        excuseService.updateExcuse(post.getExcuse(), command);

        post.setModifiedAt(LocalDateTime.now());

        postRepository.save(post);
    }

    // 게시물 삭제
    public void deletePost(DeleteCommand command){
        Post post = getPost(command.targetId());

        // 본인 작성글만 삭제 가능
        if(!post.getMember().getId().equals(command.memberId())) throw new BusinessLogicException(ExceptionType.DELETE_FORBIDDEN);

        // 명예의 전당 게시물은 삭제 불가
        if(rankingService.isPostInHallOfFame(command.targetId())) throw new BusinessLogicException(ExceptionType.HALL_OF_FAME_PROTECTED);

        post.setStatus(Post.Status.DELETED);

        postRepository.save(post);
    }
}

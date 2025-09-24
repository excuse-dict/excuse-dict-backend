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
import net.whgkswo.excuse_bundle.entities.posts.core.dto.WeeklyTopPostResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.core.entity.Post;
import net.whgkswo.excuse_bundle.entities.posts.core.mapper.PostMapper;
import net.whgkswo.excuse_bundle.entities.posts.core.repository.PostRepository;
import net.whgkswo.excuse_bundle.entities.posts.core.entity.PostVote;
import net.whgkswo.excuse_bundle.entities.posts.hotscore.PostIdWithHotScoreDto;
import net.whgkswo.excuse_bundle.entities.posts.hotscore.HotScoreService;
import net.whgkswo.excuse_bundle.entities.posts.hotscore.PostWithHotScoreDto;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final HotScoreService hotScoreService;

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

    // 올타임 순추천수 Top 게시글 조회
    public Page<Post> getTopNetLikes(Pageable pageable){
        return postRepository.findTopNetLikes(pageable, Post.Status.ACTIVE);
    }

    // 최근 n일 순추천수 Top 게시글 조회
    public List<PostIdWithHotScoreDto> getRecentTopNetLikes(int days){

        LocalDateTime startDateTime = LocalDateTime.now().minusDays(days);

        List<Post> posts = postRepository.findRecentPosts(Post.Status.ACTIVE, startDateTime);

        // 가중치 적용하여 재정렬
        List<PostIdWithHotScoreDto> sortedPosts = posts.stream()
                .map(post -> new PostWithHotScoreDto(post, hotScoreService.calculateHotScore(post)))
                .sorted((a, b) -> Double.compare(b.hotScore(), a.hotScore()))
                .limit(RankingScheduler.WEEKLY_TOP_SIZE)
                .map(dto -> new PostIdWithHotScoreDto(dto.post().getId(), dto.hotScore()))
                .toList();
        
        return sortedPosts;
    }

    // 명예의 전당 게시글 조회
    public Page<PostResponseDto> getHallOfFamePosts(Pageable pageable, Long memberId){
        List<Long> postIdList = redisService.getAsList(RankingScheduler.HALL_OF_FAME_REDISKEY, Long.class);

        // redis에서 추출한 ID를 바탕으로 게시글 조회
        // fetch조인으로 Votes까지 같이 조회
        List<Post> posts = postRepository.findAllByIdWithVotes(postIdList);

        List<PostSummaryResponseDto> summaries = postMapper.postsToMultiPostResponseDtos(posts);

        List<PostResponseDto> dtos = summaries.stream()
                .map(summary -> mapSummaryToResponseDto(summary, memberId))
                .toList();

        return pageHelper.paginate(dtos, pageable);
    }

    // 주간 top 게시글 조회
    public Page<WeeklyTopPostResponseDto> getWeeklyTopPosts(Pageable pageable, Long memberId){
        // redis에서 id, hotscore 조회
        List<PostIdWithHotScoreDto> hotPostDtos = redisService.getAsList(RankingScheduler.WEEKLY_TOP_REDISKEY, PostIdWithHotScoreDto.class);

        List<Long> postIdList = hotPostDtos.stream()
                .map(dto -> dto.postId())
                .toList();

        // redis에서 추출한 ID를 바탕으로 게시글 조회
        List<Post> posts = getPostsFromIdList(postIdList);

        // hotScore 매핑용 중간다리
        Map<Long, Integer> hotScoreMap = hotPostDtos.stream()
                .collect(Collectors.toMap(
                        PostIdWithHotScoreDto::postId,
                        PostIdWithHotScoreDto::hotScore
                ));

        // post -> summary -> response -> weekly dto로 3단 변환
        List<WeeklyTopPostResponseDto> dtos = posts.stream()
                .map(post -> postMapper.postTomultiPostSummaryResponseDto(post))
                .map(summary -> mapSummaryToResponseDto(summary, memberId))
                .map(dto -> {
                    int hotScore = hotScoreMap.get(dto.getPostId());
                    return postMapper.postResponseDtoToWeeklyTopPostResponseDto(dto, hotScore);
                })
                .toList();

        return pageHelper.paginate(dtos, pageable);
    }

    // 랜덤 게시글 n개 조회
    public List<Post> getRandomPosts(int amount){
        return postRepository.findRandomPosts(amount);
    }

    // 랜덤 게시글 n개 조회(최근 m일간)
    public List<Post> getRandomPosts(int amount, int maxDaysAgo){
        return postRepository.findRandomPosts(amount, maxDaysAgo);
    }

    // id 리스트 -> 객체 리스트 변환 (순서 유지하며)
    private List<Post> getPostsFromIdList(List<Long> postIdList){
        // 그냥 이걸로 조회하면 순서 보장 안 됨
        List<Post> posts = postRepository.findAllById(postIdList);

        // id, post 맵 생성
        Map<Long, Post> postMap = posts.stream()
                .collect(Collectors.toMap(Post::getId, post -> post));
        // 원래의 순서를 유지하며 리스트 반환
        return postIdList.stream()
                .map(postMap::get)
                .toList();
    }

    // 게시글 추천
    @Transactional
    public boolean voteToPost(VoteCommand command){
        Optional<Post> optionalPost = postRepository.findById(command.targetId());
        Post post = optionalPost.orElseThrow(() -> new BusinessLogicException(ExceptionType.POST_NOT_FOUND));

        // 자추 불가
        if(post.getMember().getId().equals(command.memberId()))
            throw new BusinessLogicException(ExceptionType.SELF_VOTE_NOT_ALLOWED);

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

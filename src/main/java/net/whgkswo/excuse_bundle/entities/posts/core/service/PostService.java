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
import net.whgkswo.excuse_bundle.entities.posts.core.search.SearchResult;
import net.whgkswo.excuse_bundle.entities.posts.core.search.SearchType;
import net.whgkswo.excuse_bundle.entities.posts.hotscore.PostIdWithHotScoreDto;
import net.whgkswo.excuse_bundle.entities.posts.hotscore.HotScoreService;
import net.whgkswo.excuse_bundle.entities.posts.hotscore.PostWithHotScoreDto;
import net.whgkswo.excuse_bundle.entities.vote.mapper.VoteMapper;
import net.whgkswo.excuse_bundle.entities.vote.repository.PostVoteRepository;
import net.whgkswo.excuse_bundle.entities.vote.service.VoteService;
import net.whgkswo.excuse_bundle.exceptions.BusinessLogicException;
import net.whgkswo.excuse_bundle.exceptions.ExceptionType;
import net.whgkswo.excuse_bundle.general.dto.DeleteCommand;
import net.whgkswo.excuse_bundle.pager.PageHelper;
import net.whgkswo.excuse_bundle.ranking.scheduler.RankingScheduler;
import net.whgkswo.excuse_bundle.ranking.service.RankingService;
import net.whgkswo.excuse_bundle.words.Similarity;
import net.whgkswo.excuse_bundle.words.WordService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
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
    private final PostVoteRepository postVoteRepository;
    private final RedisService redisService;
    private final PageHelper pageHelper;
    private final HotScoreService hotScoreService;
    private final WordService wordService;

    private static final double MIN_SIMILARITY = 0.5;

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

        // 검색어 없으면 DB에서 바로 페이징
        if(command.searchInput() == null || command.searchInput().isBlank()){
            return getPostsWithoutSearch(command);
        }

        // 검색어 있으면 메모리에 다 올리고 필터링
        return getPostsWithSearch(command);
    }

    // 검색어 없을 때: DB 페이징
    private Page<PostResponseDto> getPostsWithoutSearch(GetPostsCommand command) {
        Page<Post> postPage = postRepository.findPostForPage(command.pageable(), Post.Status.ACTIVE);

        return convertPostsToResponseDtos(
                postPage,
                command.memberId(),
                null  // 매칭된 키워드 없음
        );
    }

    // 검색어 있을 때: 검색어로 필터 후 자체 페이징
    private Page<PostResponseDto> getPostsWithSearch(GetPostsCommand command) {
        // TODO: 추후 성능 개선 방안 고민
        List<Post> posts = postRepository.findAllForList(Post.Status.ACTIVE);

        // 검색어로 필터링
        List<SearchResult<Post>> searchedPosts = searchPosts(
                command.searchInput(),
                SearchType.SITUATION,
                posts
        );

        // 게시물 별 매칭된 키워드
        Map<Long, List<String>> matchedWordsMap = searchedPosts.stream()
                .collect(Collectors.toMap(
                        result -> result.searchedContent().getId(),
                        SearchResult::matchedWords
                ));

        // Post 리스트 추출
        List<Post> postList = searchedPosts.stream()
                .map(SearchResult::searchedContent)
                .toList();

        // 응답 dto로 리패키징
        List<PostResponseDto> responses = convertPostsToResponseDtos(
                postList,
                command.memberId(),
                matchedWordsMap
        );

        return pageHelper.paginate(responses, command.pageable());
    }

    // Post -> PostResponseDto 변환 (Page -> Page)
    private Page<PostResponseDto> convertPostsToResponseDtos(Page<Post> posts, Long memberId, Map<Long, List<String>> matchedWordsMap){
        if(posts.isEmpty()) return new PageImpl<>(Collections.emptyList());

        List<PostResponseDto> responses = convertPostsToResponseDtos(
                posts.getContent(),
                memberId,
                matchedWordsMap
        );

        return new PageImpl<>(
                responses,
                posts.getPageable(),
                posts.getTotalElements()
        );
    }

    // Post -> PostResponseDto 변환 (List -> List)
    private List<PostResponseDto> convertPostsToResponseDtos(
            List<Post> posts,
            Long memberId,
            Map<Long, List<String>> matchedWordsMap
    ) {
        if (posts.isEmpty()) {
            return Collections.emptyList();
        }

        // Post ID 추출
        List<Long> postIds = posts.stream()
                .map(Post::getId)
                .toList();

        // 게시물 id로 Vote 조회
        List<PostVote> votes = postVoteRepository.findAllByPostIds(postIds);

        // 게시물 id <-> Vote 맵핑
        Map<Long, List<PostVote>> votesByPostId = votes.stream()
                .collect(Collectors.groupingBy(vote -> vote.getPost().getId()));

        return posts.stream().map(post -> {
            // 좋아요 / 싫어요 가져오기
            List<PostVote> postVotes = votesByPostId.getOrDefault(post.getId(), Collections.emptyList());

            // 요청한 회원이 누른 거 있나 보기
            Optional<PostVote> myVote = postVotes.stream()
                    .filter(vote -> memberId != null && vote.getMember().getId().equals(memberId))
                    .findFirst();

            // post -> summary
            PostSummaryResponseDto summary = postMapper.postTomultiPostSummaryResponseDto(post);

            // matchedWords 가져오기 (없으면 null)
            List<String> matchedWords = matchedWordsMap != null
                    ? matchedWordsMap.get(post.getId())
                    : null;

            // summary -> response
            return postMapper.postSummaryResponseDtoToPostResponseDto(
                    summary,
                    myVote.map(voteMapper::postVoteToPostVoteDto).orElse(null),
                    matchedWords
            );
        }).toList();
    }

    private PostResponseDto mapSummaryToResponseDto(PostSummaryResponseDto summary, Long memberId){
        Post post = getPost(summary.getPostId());
        Optional<PostVote> optionalVote = voteService.getPostVoteFromCertainMember(post, memberId);

        return postMapper.postSummaryResponseDtoToPostResponseDto(summary, optionalVote.map(voteMapper::postVoteToPostVoteDto).orElse(null), null);
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

        List<PostSummaryResponseDto> summaries = postMapper.postsToMultiPostSummaryResponseDtos(posts);

        List<PostResponseDto> dtos = summaries.stream()
                .map(summary -> mapSummaryToResponseDto(summary, memberId))
                .toList();

        return pageHelper.paginate(dtos, pageable);
    }

    // 주간 top 게시글 조회
    public Page<WeeklyTopPostResponseDto> getWeeklyTopPosts(Pageable pageable, Long memberId){
        // redis에서 id, hotscore 조회
        List<PostIdWithHotScoreDto> hotPostDtos = redisService.getAsList(RankingScheduler.WEEKLY_TOP_REDISKEY, PostIdWithHotScoreDto.class);

        // 게시물 id 추출
        List<Long> postIdList = hotPostDtos.stream()
                .map(dto -> dto.postId())
                .toList();

        // 추출한 ID를 바탕으로 게시글 조회
        List<Post> posts = getPostsFromIdList(postIdList);

        // hotScore 매핑용 중간다리
        Map<Long, Integer> hotScoreMap = hotPostDtos.stream()
                .collect(Collectors.toMap(
                        PostIdWithHotScoreDto::postId,
                        PostIdWithHotScoreDto::hotScore
                ));

        // post -> summary -> response -> weekly dto로 3단 변환
        List<WeeklyTopPostResponseDto> dtos = posts.stream()
                .map(post -> {
                    // post -> summary
                    PostSummaryResponseDto summary = postMapper.postTomultiPostSummaryResponseDto(post);
                    // vote 유무 확인
                    Optional<PostVote> optionalVote = voteService.getPostVoteFromCertainMember(post, memberId);
                    // summary -> response
                    PostResponseDto responseDto = postMapper.postSummaryResponseDtoToPostResponseDto(
                            summary, optionalVote.map(voteMapper::postVoteToPostVoteDto).orElse(null), null);
                    // response -> weekly
                    int hotScore = hotScoreMap.get(post.getId());
                    return postMapper.postResponseDtoToWeeklyTopPostResponseDto(responseDto, hotScore);
                })
                .toList();

        return pageHelper.paginate(dtos, pageable);
    }

    // 랜덤 게시글 id n개 조회
    public List<Long> getRandomPostIds(int amount){
        return postRepository.findRandomPostsId(amount);
    }

    // 랜덤 게시글 id n개 조회(최근 m일간)
    public List<Long> getRandomPostIds(int amount, int maxDaysAgo){
        LocalDateTime startDateTime = LocalDateTime.now().minusDays(maxDaysAgo);
        return postRepository.findRandomPostsId(amount, startDateTime);
    }

    // id 리스트 -> 객체 리스트 변환 (순서 유지하며)
    private List<Post> getPostsFromIdList(List<Long> postIdList){
        // 그냥 이걸로 조회하면 순서 보장 안 됨
        // fetch조인으로 Votes까지 같이 조회
        List<Post> posts = postRepository.findAllByIdWithVotes(postIdList);

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

    // 게시물 검색
    List<SearchResult<Post>> searchPosts(String searchInput, SearchType searchType, List<Post> posts){

        return posts.stream()
                .map(post -> { // 유사도 계산 후 유사도 포함 랩핑
                    String targetString = post.getExcuse().getSituation();
                    Similarity similarity = wordService.calculateTextSimilarity(targetString, searchInput);
                    return Map.entry(post, similarity);
                })
                .filter(entry -> entry.getValue().similarityScore() > MIN_SIMILARITY)
                .sorted((a, b) -> Double.compare(b.getValue().similarityScore(), a.getValue().similarityScore())) // 유사도순 정렬
                .map(entry -> { // 다시 유사도 빼고 랩핑
                    Post post = entry.getKey();
                    return new SearchResult<>(post, entry.getValue().matchedWords());
                })
                .toList();
    }
}

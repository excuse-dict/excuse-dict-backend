package net.whgkswo.excuse_dict.entities.posts.post_core.service;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_dict.auth.redis.RedisKey;
import net.whgkswo.excuse_dict.auth.redis.RedisService;
import net.whgkswo.excuse_dict.entities.excuses.Excuse;
import net.whgkswo.excuse_dict.entities.excuses.dto.UpdateExcuseCommand;
import net.whgkswo.excuse_dict.entities.excuses.service.ExcuseService;
import net.whgkswo.excuse_dict.entities.members.core.entitiy.Member;
import net.whgkswo.excuse_dict.entities.members.core.service.MemberService;
import net.whgkswo.excuse_dict.entities.posts.comments.entity.Comment;
import net.whgkswo.excuse_dict.entities.posts.comments.repository.CommentRepository;
import net.whgkswo.excuse_dict.entities.posts.post_core.dto.*;
import net.whgkswo.excuse_dict.entities.posts.post_core.entity.Post;
import net.whgkswo.excuse_dict.entities.posts.post_core.mapper.PostMapper;
import net.whgkswo.excuse_dict.entities.posts.post_core.repository.PostRepository;
import net.whgkswo.excuse_dict.entities.posts.post_core.entity.PostVote;
import net.whgkswo.excuse_dict.search.dto.HotSearchKeywordDto;
import net.whgkswo.excuse_dict.search.dto.PostSearchDto;
import net.whgkswo.excuse_dict.search.dto.PostSearchRequestDto;
import net.whgkswo.excuse_dict.komoran.KomoranService;
import net.whgkswo.excuse_dict.ranking.dto.RecentHotPostDto;
import net.whgkswo.excuse_dict.ranking.dto.TopNetLikesPostDto;
import net.whgkswo.excuse_dict.search.SearchResult;
import net.whgkswo.excuse_dict.search.SearchType;
import net.whgkswo.excuse_dict.entities.posts.hotscore.PostIdWithHotScoreDto;
import net.whgkswo.excuse_dict.entities.posts.hotscore.HotScoreService;
import net.whgkswo.excuse_dict.entities.vote.dto.PostVoteDto;
import net.whgkswo.excuse_dict.entities.vote.mapper.VoteMapper;
import net.whgkswo.excuse_dict.entities.vote.repository.PostVoteRepository;
import net.whgkswo.excuse_dict.entities.vote.service.VoteService;
import net.whgkswo.excuse_dict.exceptions.BusinessLogicException;
import net.whgkswo.excuse_dict.exceptions.ExceptionType;
import net.whgkswo.excuse_dict.general.dto.DeleteCommand;
import net.whgkswo.excuse_dict.pager.PageHelper;
import net.whgkswo.excuse_dict.ranking.scheduler.RankingScheduler;
import net.whgkswo.excuse_dict.ranking.service.RankingService;
import net.whgkswo.excuse_dict.search.dto.PostTagSearchDto;
import net.whgkswo.excuse_dict.search.words.similarity.ContainsSimilarityCalculator;
import net.whgkswo.excuse_dict.search.words.similarity.MorphemeBasedSimilarityCalculator;
import net.whgkswo.excuse_dict.search.words.similarity.Similarity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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
    private final PostDtoConverter postDtoConverter;
    private final RedisService redisService;
    private final PageHelper pageHelper;
    private final HotScoreService hotScoreService;
    private final CommentRepository commentRepository;

    private final MorphemeBasedSimilarityCalculator morphemeBasedSimilarityCalculator;
    private final ContainsSimilarityCalculator containsSimilarityCalculator;

    public static final double MIN_SIMILARITY = 0.5;
    public static final int SEARCH_KEYWORD_EXPIRE_DAYS = 7;
    public static final int SEARCH_KEYWORD_MAX_SIZE = 10;
    public static final String RECENT_SEARCHED_KEYWORDS_KEY = "SEARCHED_KEYWORDS_LAST_DAYS";

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

        // 검색어와 태그 필터링 조건 있는지 확인
        boolean hasTagFilter = !command.excludedTags().isEmpty() || !command.includedTags().isEmpty();
        boolean hasSearchFilter = command.searchInput() != null && !command.searchInput().isBlank() && command.searchType() != null;

        // 둘 다 없으면 DB에서 다이렉트 페이징
        if(!hasTagFilter && !hasSearchFilter){
            return getPostsWithoutFiltering(command);
        }

        // 검색어 카운트 증가
        if(hasSearchFilter) addSearchCount(command.searchInput());

        // 있으면 메모리에 올려놓고 서비스에서 필터링
        return getPostsWithFiltering(command);
    }

    // 태그로 필터링
    private List<SearchResult<PostTagSearchDto>> filterByTags(List<PostTagSearchDto> posts, List<String> includedTags, List<String> excludedTags){

        if(includedTags.isEmpty() && excludedTags.isEmpty()) {
            return posts.stream()
                    .map(post -> new SearchResult<>(post, Collections.emptyList()))
                    .toList();
        }

        return posts.stream()
                .map(dto -> {
                    // 게시물에 붙은 태그 확인
                    Set<String> postTags = dto.tags();

                    // 제외 태그 - 하나라도 있으면 제외
                    if(!excludedTags.isEmpty() && postTags.stream().anyMatch(excludedTags::contains)) {
                        return null;
                    }

                    // 포함 태그 - 하나라도 있으면 포함
                    List<String> matchedTags = Collections.emptyList();
                    if(!includedTags.isEmpty()) {
                        matchedTags = postTags.stream()
                                .filter(includedTags::contains)
                                .toList();

                        // 매칭 안 됨
                        if(matchedTags.isEmpty()) return null;

                        return new SearchResult<>(dto, matchedTags);
                    }

                    return new SearchResult<>(dto, matchedTags);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // 검색어나 태그 필터링 없을 때: DB 다이렉트 페이징
    private Page<PostResponseDto> getPostsWithoutFiltering(GetPostsCommand command) {
        Page<Post> postPage = postRepository.findPostForPage(command.pageable(), Post.Status.ACTIVE);

        return postDtoConverter.convertPostsToResponseDtos(
                postPage,
                command.memberId(),
                null,  // 매칭된 키워드 없음
                null // 매칭된 태그 없음
        );
    }

    // 검색어나 태그 필터링 있을 때: 메모리로 가져와서 필터링 후 직접 페이징
    private Page<PostResponseDto> getPostsWithFiltering(GetPostsCommand command) {

        // 검색용 dto 가볍게 조회
        List<PostSearchDto> searchDtos = postRepository.findAllSearchDtoByStatus(Post.Status.ACTIVE);

        // 검색어로 필터링
        List<SearchResult<PostSearchDto>> searchedPosts = searchPosts(
                command.searchInput(),
                command.searchType(),
                searchDtos
        );

        // 검색으로 필터링된 ID
        List<Long> searchedPostIds = searchedPosts.stream()
                .map(result -> result.searchedContent().id())
                .toList();

        // 게시물 별 매칭된 키워드
        Map<Long, List<String>> matchedWordsMap = SearchResult.mapByMatchedWords(searchedPosts);

        // 검색 결과에 태그로 추가 필터링 (하기 위한 dto 조회)
        List<PostTagSearchDto> tagSearchDtos = getTagSearchDtos(searchedPostIds, !command.includedTags().isEmpty());

        // 태그 필터링 실행
        List<SearchResult<PostTagSearchDto>> tagFilteredResults = filterByTags(
                tagSearchDtos,
                command.includedTags(),
                command.excludedTags()
        );

        // 게시물 별 매칭된 태그
        Map<Long, List<String>> matchedTagsMap = SearchResult.mapByMatchedWords(tagFilteredResults);

        // 검색 + 필터링된 게시물 실제 객체 가져오기
        List<Long> postIdList = tagFilteredResults.stream()
                .map(result -> result.searchedContent().id())
                .toList();
        List<Post> postList = getOrderedPostsFromIdList(postIdList);

        // 응답 dto로 리패키징
        List<PostResponseDto> responses = postDtoConverter.convertPostsToResponseDtos(
                postList,
                command.memberId(),
                matchedWordsMap,
                matchedTagsMap
        );

        return pageHelper.paginate(responses, command.pageable());
    }

    // 게시물 태그 필터링용 경량화 태그 조회
    private List<PostTagSearchDto> getTagSearchDtos(List<Long> postIds, boolean hasIncludedTags) {

        // '포함 태그' 조건이 비어 있다면 태그 없는 게시물도 가져와야 하지만, '포함 태그' 조건이 있을 경우 태그 없는 게시물은 자동으로 제외되기에
        // 메모리에 굳이 모든 게시물을 다 올릴 필요가 없음. DB 조건부 호출로 최적화 가능
        // Post Id와 tags 조회
        List<Object[]> results = hasIncludedTags
                ? postRepository.findPostIdAndTagsInnerJoin(postIds, Post.Status.ACTIVE)  // 포함 태그 있으면 INNER JOIN
                : postRepository.findPostIdAndTagsLeftJoin(postIds, Post.Status.ACTIVE);  // 포함 태그 없으면 LEFT JOIN

        // postId 별 태그 정보 그룹핑
        Map<Long, Set<String>> tagsByPostId = new HashMap<>();

        for (Object[] row : results) {
            Long postId = ((Number) row[0]).longValue();
            String tagValue = (String) row[1];

            tagsByPostId.computeIfAbsent(postId, k -> new HashSet<>()).add(tagValue);
        }

        // DTO 생성
        return tagsByPostId.entrySet().stream()
                .map(entry -> new PostTagSearchDto(entry.getKey(), entry.getValue()))
                .toList();
    }

    // 게시물 검색어 카운트 증가
    public void addSearchCount(String searchInput){

        String todayStr = LocalDate.now().toString();
        RedisKey key = new RedisKey(RedisKey.Prefix.SEARCH, todayStr);

        redisService.putMemberToSortedSet(key, searchInput, 1, SEARCH_KEYWORD_EXPIRE_DAYS);
    }

    // 인기 검색어 조회
    public List<HotSearchKeywordDto> getHotSearchKeywords(){

        // 오늘자 기록
        String todayStr = LocalDate.now().toString();
        RedisKey key = new RedisKey(RedisKey.Prefix.SEARCH, todayStr);
        Map<String, Double> todayKeywords = redisService.getAllOfSortedSetEntriesAsMap(key, false);

        // 지난 일주일치(오늘 제외) 기록
        RedisKey lastKey = new RedisKey(RedisKey.Prefix.SEARCH, PostService.RECENT_SEARCHED_KEYWORDS_KEY);
        Map<String, Double> lastKeywords = redisService.getAllOfSortedSetEntriesAsMap(lastKey, false);

        // 합산
        Map<String, Double> merged = new HashMap<>(lastKeywords);
        todayKeywords.forEach((keyword, count) ->
                merged.merge(keyword, count, Double::sum)
        );

        // 어제까지 순위
        Map<String, Integer> lastRanks = new HashMap<>();
        int rank = 1;
        for (String keyword : lastKeywords.keySet()) {
            lastRanks.put(keyword, rank++);
        }

        // 오늘 순위와 비교
        AtomicInteger currentRank = new AtomicInteger(1);
        return merged.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(e -> {
                    Integer lastRank = lastRanks.get(e.getKey());
                    Integer rankChange = lastRank == null? null : lastRank - currentRank.getAndIncrement();

                    return new HotSearchKeywordDto(e.getKey(), e.getValue().intValue(), rankChange);
                })
                .limit(SEARCH_KEYWORD_MAX_SIZE)
                .toList();
    }

    // 특정 게시물을 포함한 페이지 반환
    public Page<PostResponseDto> getPageIncludesHighlightedPost(PostHighlightCommand command){

        // 해당 게시물이 없음
        if(!postRepository.existsByIdAndStatus(command.postId(), Post.Status.ACTIVE))
            throw new BusinessLogicException(ExceptionType.POST_NOT_FOUND);

        int pageNumber = postRepository.findPageNumberByPostId(command.postId(), PostSearchRequestDto.DEFAULT_SIZE);
        Pageable pageable = PageRequest.of(pageNumber, PostSearchRequestDto.DEFAULT_SIZE);

        Page<PostResponseDto> posts = getPosts(
                new GetPostsCommand(
                        pageable,
                        null,
                        command.memberId(),
                        null,
                        Collections.emptyList(),
                        Collections.emptyList()
                )
        );

        return posts;
    }

    private Optional<Post> findPost(long postId){
        return postRepository.findByIdForDetail(postId);
    }

    public Post getPost(long postId){
        Optional<Post> optionalPost = findPost(postId);
        return optionalPost.orElseThrow(() -> new BusinessLogicException(ExceptionType.POST_NOT_FOUND));
    }

    // 올타임 순추천수 Top 게시글 조회
    public List<TopNetLikesPostDto> getTopNetLikes(int amount){
        List<Object[]> topPosts = postRepository.findTopNetLikes(amount);

        return topPosts.stream()
                .map(row -> new TopNetLikesPostDto((Long) row[0], ((Number) row[1]).intValue()))
                .toList();
    }

    // 최근 n일 순추천수 Top 게시글 조회
    public List<PostIdWithHotScoreDto> getRecentTopNetLikes(int days){

        // 최근 작성글 dto로 조회
        List<RecentHotPostDto> posts = getRecentHotPosts(days);

        // 가중치 적용하여 재정렬
        return posts.stream()
                .map(post -> new PostIdWithHotScoreDto(
                        post.id(),
                        hotScoreService.calculateHotScore(post)
                ))
                .sorted((a, b) -> Double.compare(b.hotScore(), a.hotScore()))
                .limit(RankingScheduler.WEEKLY_TOP_SIZE)
                .toList();
    }

    // 최근 n일 게시물 조회 (hot 스코어 계산에 필요한 정보만)
    public List<RecentHotPostDto> getRecentHotPosts(int days) {
        LocalDateTime startDatetime = LocalDateTime.now().minusDays(days);

        // 최근 게시물 Post id, 좋아요/싫어요 수, 작성일시 조회
        List<Object[]> postData = postRepository.findRecentPostIds(Post.Status.ACTIVE, startDatetime);

        // id만 추출
        List<Long> postIds = postData.stream()
                .map(row -> (Long) row[0])
                .toList();

        // id로 Vote, Comment 조회
        List<PostVote> allVotes = postVoteRepository.findVotesByPostIds(postIds);
        List<Comment> allComments = commentRepository.findCommentsByPostIds(postIds);

        // id별로 그룹핑
        Map<Long, List<PostVote>> votesByPost = allVotes.stream()
                .collect(Collectors.groupingBy(pv -> pv.getPost().getId()));

        Map<Long, List<Comment>> commentsByPost = allComments.stream()
                .collect(Collectors.groupingBy(c -> c.getPost().getId()));

        // DTO로 래핑
        return postData.stream()
                .map(row -> new RecentHotPostDto(
                        (Long) row[0],      // id
                        (Integer) row[1],   // 좋아요 수
                        (Integer) row[2],   // 싫어요 수
                        votesByPost.getOrDefault((Long) row[0], List.of()),
                        commentsByPost.getOrDefault((Long) row[0], List.of()),
                        (LocalDateTime) row[3]
                ))
                .toList();
    }

    // 명예의 전당 게시글 조회
    public Page<PostResponseDto> getHallOfFamePosts(Pageable pageable, Long memberId){

        // 레디스에서 명예의 전당 게시물 id 가져오기
        List<Long> postIdList = redisService.getAsList(RankingScheduler.HALL_OF_FAME_REDISKEY, Long.class);

        // 가져온 id로 Post 조회 (순서 보장 o)
        List<Post> posts = getOrderedPostsFromIdList(postIdList);

        // 요청한 회원이 누른 추천 / 비추천 조회
        Map<Long, PostVote> voteMap = getVoteMapByMemberId(postIdList, memberId);

        // post -> summary
        List<PostSummaryResponseDto> summaries = postMapper.postsToMultiPostSummaryResponseDtos(posts);

        List<PostResponseDto> dtos = summaries.stream()
                .map(summary -> {
                    PostVote vote = voteMap.get(summary.getPostId());
                    PostVoteDto voteDto = vote == null ? null : voteMapper.postVoteToPostVoteDto(vote);
                    return postMapper.postSummaryResponseDtoToPostResponseDto(summary, voteDto, null, null);
                })
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

        // 추출한 ID를 바탕으로 게시글 조회 (순서 보장 o)
        List<Post> posts = getOrderedPostsFromIdList(postIdList);

        // hot score 맵핑용
        Map<Long, Integer> hotScoreMap = hotPostDtos.stream()
                .collect(Collectors.toMap(
                        PostIdWithHotScoreDto::postId,
                        PostIdWithHotScoreDto::hotScore
                ));

        // 요청한 회원이 누른 게시물별 추천 / 비추천
        Map<Long, PostVote> voteMap = getVoteMapByMemberId(postIdList, memberId);

        // post -> summary
        List<PostSummaryResponseDto> summaries = postMapper.postsToMultiPostSummaryResponseDtos(posts);

        // summary -> response -> weekly dto
        List<WeeklyTopPostResponseDto> dtos = summaries.stream()
                .map(summary -> {
                    long postId = summary.getPostId();

                    // myVote 유무 확인
                    PostVoteDto voteDto = Optional.ofNullable(voteMap.get(postId))
                            .map(voteMapper::postVoteToPostVoteDto)
                            .orElse(null);

                    // summary -> response
                    PostResponseDto responseDto = postMapper.postSummaryResponseDtoToPostResponseDto(
                            summary, voteDto, null, null);

                    // response -> weekly
                    int hotScore = hotScoreMap.get(postId);
                    return postMapper.postResponseDtoToWeeklyTopPostResponseDto(responseDto, hotScore);
                })
                .toList();

        return pageHelper.paginate(dtos, pageable);
    }

    // 게시물 리스트로부터 특정 회원이 누른 추천/비추천을 게시물별로 반환
    private Map<Long, PostVote> getVoteMapByMemberId(List<Long> postIdList, Long memberId) {
        if (memberId == null) {
            return Collections.emptyMap();
        }

        List<PostVote> votes = postVoteRepository.findAllByPostIdsAndMemberId(postIdList, memberId);

        return votes.stream()
                .collect(Collectors.toMap(
                        vote -> vote.getPost().getId(),
                        vote -> vote
                ));
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

    // id 리스트 제공받아 순서 보장하며 Post 반환
    private List<Post> getOrderedPostsFromIdList(List<Long> postIdList){
        // 그냥 이걸로 조회하면 순서 보장 안 됨
        List<Post> posts = postRepository.findAllByIdList(postIdList);

        // id, post 맵 생성
        Map<Long, Post> postMap = posts.stream()
                .collect(Collectors.toMap(Post::getId, post -> post));

        // 원래의 순서를 유지하며 리스트 반환
        return postIdList.stream()
                .map(postMap::get)
                .filter(Objects::nonNull)
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
    List<SearchResult<PostSearchDto>> searchPosts(String searchInput, SearchType searchType, List<PostSearchDto> dtos){

        // 검색어 없으면 전체 반환
        if(searchInput == null || searchInput.isBlank() || searchType == null)
            return dtos.stream()
                    .map(dto -> new SearchResult<>(dto, Collections.emptyList()))
                    .toList();

        return dtos.stream()
                .map(post -> { // 유사도 계산 후 유사도 포함 랩핑
                    Similarity similarity = getPostSimilarity(post, searchInput, searchType);
                    return Map.entry(post, similarity);
                })
                .filter(entry -> entry.getValue().similarityScore() > MIN_SIMILARITY)
                .sorted((a, b) -> Double.compare(b.getValue().similarityScore(), a.getValue().similarityScore())) // 유사도순 정렬
                .map(entry -> new SearchResult<>(entry.getKey(), entry.getValue().matchedWords())) // 다시 유사도 빼고 랩핑
                .toList();
    }

    private Similarity getPostSimilarity(PostSearchDto dto, String searchInput, SearchType searchType){
        return switch (searchType) {
            case SITUATION -> morphemeBasedSimilarityCalculator.calculateSimilarity(dto.situation(), searchInput);
            case EXCUSE -> morphemeBasedSimilarityCalculator.calculateSimilarity(dto.excuse(), searchInput);
            case SITUATION_AND_EXCUSE -> {
                Similarity sitSim = morphemeBasedSimilarityCalculator.calculateSimilarity(dto.situation(), searchInput);
                Similarity excSim = morphemeBasedSimilarityCalculator.calculateSimilarity(dto.excuse(), searchInput);
                yield sitSim.similarityScore() > excSim.similarityScore() ? sitSim : excSim;
            }
            case AUTHOR -> containsSimilarityCalculator.calculateSimilarity(dto.authorName(), searchInput, MIN_SIMILARITY);
        };
    }
}

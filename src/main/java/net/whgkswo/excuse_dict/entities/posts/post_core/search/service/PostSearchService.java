package net.whgkswo.excuse_dict.entities.posts.post_core.search.service;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_dict.auth.redis.RedisKey;
import net.whgkswo.excuse_dict.auth.redis.RedisService;
import net.whgkswo.excuse_dict.entities.excuses.repository.ExcuseRepository;
import net.whgkswo.excuse_dict.entities.excuses.service.ExcuseService;
import net.whgkswo.excuse_dict.entities.posts.post_core.dto.PostHighlightCommand;
import net.whgkswo.excuse_dict.entities.posts.post_core.dto.PostResponseDto;
import net.whgkswo.excuse_dict.entities.posts.post_core.entity.Post;
import net.whgkswo.excuse_dict.entities.posts.post_core.repository.PostRepository;
import net.whgkswo.excuse_dict.entities.posts.post_core.search.dto.MorphemeData;
import net.whgkswo.excuse_dict.entities.posts.post_core.search.dto.MorphemeSearchResult;
import net.whgkswo.excuse_dict.entities.posts.post_core.search.dto.TagFilterResult;
import net.whgkswo.excuse_dict.entities.posts.post_core.service.GetPostsCommand;
import net.whgkswo.excuse_dict.entities.posts.post_core.service.PostDtoConverter;
import net.whgkswo.excuse_dict.exceptions.BusinessLogicException;
import net.whgkswo.excuse_dict.exceptions.ExceptionType;
import net.whgkswo.excuse_dict.komoran.KomoranHelper;
import net.whgkswo.excuse_dict.pager.PageHelper;
import net.whgkswo.excuse_dict.search.SearchResult;
import net.whgkswo.excuse_dict.search.SearchType;
import net.whgkswo.excuse_dict.search.dto.HotSearchKeywordDto;
import net.whgkswo.excuse_dict.search.dto.PostSearchRequestDto;
import net.whgkswo.excuse_dict.search.dto.PostTagSearchDto;
import net.whgkswo.excuse_dict.search.words.WordHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostSearchService {

    private final PostRepository postRepository;
    private final PostDtoConverter postDtoConverter;
    private final ExcuseRepository excuseRepository;
    private final ExcuseService excuseService;
    private final RedisService redisService;

    public static final double MIN_SIMILARITY = 0.5;
    public static final int SEARCH_KEYWORD_EXPIRE_DAYS = 7;
    public static final int SEARCH_KEYWORD_MAX_SIZE = 10;
    public static final String RECENT_SEARCHED_KEYWORDS_KEY = "SEARCHED_KEYWORDS_LAST_DAYS";


    @Transactional(readOnly = true)
    public Page<PostResponseDto> getPosts(GetPostsCommand command) {

        boolean hasTagFilter = !command.excludedTags().isEmpty() || !command.includedTags().isEmpty();
        boolean hasSearchFilter = command.searchInput() != null && !command.searchInput().isBlank() && command.searchType() != null;

        // 검색어 카운트 증가
        if (hasSearchFilter) addSearchCount(command.searchInput());

        if (!hasTagFilter && !hasSearchFilter) {
            // 둘 다 없으면 DB 다이렉트 페이징
            return getPostsWithoutFiltering(command);

        } else if (!hasTagFilter && hasSearchFilter) {
            // 검색어만 있으면 형태소 검색
            return getPostsWithSearchOnly(command);

        } else if (hasTagFilter && !hasSearchFilter) {
            // 태그만 있으면 DB 태그 필터링
            return getPostsWithTagFilterOnly(command);

        } else {
            // 둘 다 있으면 태그 → 형태소 순차 필터링
            return getPostsWithBothFilters(command);
        }
    }

    // 둘 다 없음
    private Page<PostResponseDto> getPostsWithoutFiltering(GetPostsCommand command) {
        Page<Post> postPage = postRepository.findPostForPage(command.pageable(), Post.Status.ACTIVE);

        return postDtoConverter.convertPostsToResponseDtos(
                postPage,
                command.memberId(),
                null,
                null
        );
    }

    // 검색어만 있음
    private Page<PostResponseDto> getPostsWithSearchOnly(GetPostsCommand command) {
        MorphemeSearchResult result = searchByMorphemes(
                command.searchInput(),
                command.searchType(),
                null
        );

        if (result.isEmpty()) return Page.empty(command.pageable());

        List<Post> posts = postRepository.findAllByIdWithDetails(result.postScoreMap().keySet());

        Comparator<Post> byScoreAndDate = Comparator
                .comparingDouble((Post p) -> result.postScoreMap().getOrDefault(p.getId(), 0.0))
                .reversed()
                .thenComparing(Post::getCreatedAt, Comparator.reverseOrder());

        posts.sort(byScoreAndDate);

        List<PostResponseDto> responses = postDtoConverter.convertPostsToResponseDtos(
                posts,
                command.memberId(),
                result.matchedWords(),
                null
        );

        return PageHelper.paginate(responses, command.pageable());
    }

    // 태그만 있음
    private Page<PostResponseDto> getPostsWithTagFilterOnly(GetPostsCommand command) {
        Page<Post> postPage = postRepository.findPostsByTagFilter(
                command.pageable(),
                Post.Status.ACTIVE,
                command.includedTags(),
                !command.includedTags().isEmpty(),
                command.excludedTags(),
                !command.excludedTags().isEmpty()
        );

        Map<Long, List<String>> matchedTags = command.includedTags().isEmpty()
                ? Map.of()
                : getMatchedTagsForPosts(
                postPage.stream().map(Post::getId).toList(),
                command.includedTags()
        );

        return postDtoConverter.convertPostsToResponseDtos(
                postPage,
                command.memberId(),
                null,
                matchedTags
        );
    }

    // 둘 다 있음
    private Page<PostResponseDto> getPostsWithBothFilters(GetPostsCommand command) {
        // DB에서 태그 필터링 (ID만)
        Set<Long> tagFilteredIds = filterPostIdsByTagsInDb(
                command.includedTags(),
                command.excludedTags()
        );

        if (tagFilteredIds.isEmpty()) return Page.empty(command.pageable());

        // 형태소 검색 (태그로 줄어진 범위 내)
        MorphemeSearchResult morphemeResult = searchByMorphemes(
                command.searchInput(),
                command.searchType(),
                tagFilteredIds
        );

        if (morphemeResult.isEmpty()) return Page.empty(command.pageable());

        // 게시물 조회 및 정렬
        List<Post> posts = postRepository.findAllByIdWithDetails(morphemeResult.postScoreMap().keySet());

        Comparator<Post> byScoreAndDate = Comparator
                .comparingDouble((Post p) -> morphemeResult.postScoreMap().getOrDefault(p.getId(), 0.0))
                .reversed()
                .thenComparing(Post::getCreatedAt, Comparator.reverseOrder());

        posts.sort(byScoreAndDate);

        // 매칭된 태그 정보 조회 (최종 결과에 대해서만)
        Map<Long, List<String>> matchedTags = command.includedTags().isEmpty()
                ? Map.of()
                : getMatchedTagsForPosts(
                new ArrayList<>(morphemeResult.postScoreMap().keySet()),
                command.includedTags()
        );

        List<PostResponseDto> responses = postDtoConverter.convertPostsToResponseDtos(
                posts,
                command.memberId(),
                morphemeResult.matchedWords(),
                matchedTags
        );

        return PageHelper.paginate(responses, command.pageable());
    }

    // 태그 필터링
    private Set<Long> filterPostIdsByTagsInDb(
            List<String> includedTags,
            List<String> excludedTags
    ) {
        List<Long> postIds = postRepository.findPostIdsByTagFilter(
                Post.Status.ACTIVE,
                includedTags,
                !includedTags.isEmpty(),
                excludedTags,
                !excludedTags.isEmpty()
        );

        return new HashSet<>(postIds);
    }

    // 형태소 검색
    private MorphemeSearchResult searchByMorphemes(
            String searchInput,
            SearchType searchType,
            Set<Long> candidatePostIds
    ) {
        List<String> searchMorphemes = KomoranHelper.getMeaningfulMorphemes(searchInput);
        List<String> dbMorphemes = excuseService.getMorphemes(searchType);

        Map<String, Double> morphemeScoreMap =
                dbMorphemes.stream()
                        .flatMap(dbMorpheme ->
                                searchMorphemes.stream()
                                        .map(searchMorpheme -> Map.entry(
                                                dbMorpheme,
                                                WordHelper.calculateWordSimilarity(searchMorpheme, dbMorpheme)
                                        ))
                        )
                        .filter(e -> e.getValue() >= MIN_SIMILARITY)
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                Math::max
                        ));

        if (morphemeScoreMap.isEmpty()) return MorphemeSearchResult.empty();

        List<String> morphemes = new ArrayList<>(morphemeScoreMap.keySet());

        List<Object[]> rows = candidatePostIds != null
                ? queryMorphemesFiltered(searchType, morphemes, candidatePostIds)
                : queryMorphemes(searchType, morphemes);

        Map<Long, MorphemeData> result = rows.stream()
                .collect(Collectors.groupingBy(
                        row -> ((Number) row[0]).longValue(),
                        Collectors.collectingAndThen(
                                Collectors.mapping(row -> (String) row[1], Collectors.toList()),
                                words -> new MorphemeData(
                                        words,
                                        words.stream().mapToDouble(morphemeScoreMap::get).sum()
                                )
                        )
                ));

        Map<Long, List<String>> matchedWords = result.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().words()));

        Map<Long, Double> postScoreMap = result.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().score()));

        return new MorphemeSearchResult(postScoreMap, matchedWords);
    }

    // 매칭된 태그 조회
    private Map<Long, List<String>> getMatchedTagsForPosts(List<Long> postIds, List<String> includedTags) {
        if (postIds.isEmpty() || includedTags.isEmpty()) {
            return Map.of();
        }

        List<Object[]> results = postRepository.findPostIdAndTagsInnerJoin(postIds, Post.Status.ACTIVE);

        return results.stream()
                .filter(row -> includedTags.contains((String) row[1]))
                .collect(Collectors.groupingBy(
                        row -> ((Number) row[0]).longValue(),
                        Collectors.mapping(
                                row -> (String) row[1],
                                Collectors.toList()
                        )
                ));
    }

    // 형태소 조회
    private List<Object[]> queryMorphemes(SearchType searchType, List<String> morphemes) {
        return switch (searchType) {
            case SITUATION -> excuseRepository.findPostIdsWithSituationMorphemes(morphemes);
            case EXCUSE -> excuseRepository.findPostIdsWithExcuseMorphemes(morphemes);
            case SITUATION_AND_EXCUSE -> excuseRepository.findPostIdsWithAllMorphemes(morphemes);
            default -> new ArrayList<>();
        };
    }

    private List<Object[]> queryMorphemesFiltered(SearchType searchType, List<String> morphemes, Set<Long> postIds) {
        return switch (searchType) {
            case SITUATION -> excuseRepository.findPostIdsWithSituationMorphemesFiltered(morphemes, postIds);
            case EXCUSE -> excuseRepository.findPostIdsWithExcuseMorphemesFiltered(morphemes, postIds);
            case SITUATION_AND_EXCUSE -> excuseRepository.findPostIdsWithAllMorphemesFiltered(morphemes, postIds);
            default -> new ArrayList<>();
        };
    }


    // 검색어 카운트 증가
    public void addSearchCount(String searchInput) {
        String todayStr = LocalDate.now().toString();
        RedisKey key = new RedisKey(RedisKey.Prefix.SEARCH, todayStr);

        redisService.putMemberToSortedSet(key, searchInput, 1, SEARCH_KEYWORD_EXPIRE_DAYS);
    }

    public List<HotSearchKeywordDto> getHotSearchKeywords() {
        String todayStr = LocalDate.now().toString();
        RedisKey key = new RedisKey(RedisKey.Prefix.SEARCH, todayStr);
        Map<String, Double> todayKeywords = redisService.getAllOfSortedSetEntriesAsMap(key, false);

        RedisKey lastKey = new RedisKey(RedisKey.Prefix.SEARCH, RECENT_SEARCHED_KEYWORDS_KEY);
        Map<String, Double> lastKeywords = redisService.getAllOfSortedSetEntriesAsMap(lastKey, false);

        Map<String, Double> merged = new HashMap<>(lastKeywords);
        todayKeywords.forEach((keyword, count) ->
                merged.merge(keyword, count, Double::sum)
        );

        Map<String, Integer> lastRanks = new HashMap<>();
        int rank = 1;
        for (String keyword : lastKeywords.keySet()) {
            lastRanks.put(keyword, rank++);
        }

        AtomicInteger currentRank = new AtomicInteger(1);
        return merged.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(e -> {
                    Integer lastRank = lastRanks.get(e.getKey());
                    Integer rankChange = lastRank == null ? null : lastRank - currentRank.getAndIncrement();

                    return new HotSearchKeywordDto(e.getKey(), e.getValue().intValue(), rankChange);
                })
                .limit(SEARCH_KEYWORD_MAX_SIZE)
                .toList();
    }

    // 특정 게시물을 포함한 페이지 조회
    public Page<PostResponseDto> getPageIncludesHighlightedPost(PostHighlightCommand command) {
        if (!postRepository.existsByIdAndStatus(command.postId(), Post.Status.ACTIVE))
            throw new BusinessLogicException(ExceptionType.POST_NOT_FOUND);

        int pageNumber = postRepository.findPageNumberByPostId(command.postId(), PostSearchRequestDto.DEFAULT_SIZE);
        Pageable pageable = PageRequest.of(pageNumber, PostSearchRequestDto.DEFAULT_SIZE);

        return getPosts(
                new GetPostsCommand(
                        pageable,
                        null,
                        command.memberId(),
                        null,
                        Collections.emptyList(),
                        Collections.emptyList()
                )
        );
    }
}
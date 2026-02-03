package net.whgkswo.excuse_dict.entities.posts.post_core.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_dict.auth.service.AuthService;
import net.whgkswo.excuse_dict.entities.excuses.dto.ExcuseRequestDto;
import net.whgkswo.excuse_dict.entities.posts.post_core.dto.*;
import net.whgkswo.excuse_dict.entities.posts.post_core.entity.Post;
import net.whgkswo.excuse_dict.entities.posts.post_core.service.GetPostsCommand;
import net.whgkswo.excuse_dict.entities.posts.post_core.service.PostService;
import net.whgkswo.excuse_dict.entities.vote.dto.VoteRequestDto;
import net.whgkswo.excuse_dict.general.dto.DeleteCommand;
import net.whgkswo.excuse_dict.general.responses.Response;
import net.whgkswo.excuse_dict.general.responses.dtos.PageSearchResponseDto;
import net.whgkswo.excuse_dict.general.responses.dtos.SimpleBooleanDto;
import net.whgkswo.excuse_dict.general.responses.page.PageInfo;
import net.whgkswo.excuse_dict.ranking.scheduler.RankingScheduler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping(PostController.BASE_URL)
@RequiredArgsConstructor
public class PostController {
    private final AuthService authService;
    private final PostService postService;

    public static final String BASE_URL = "/api/v1/posts";
    public static final String BASE_URL_ANY = "/api/*/posts";

    // 메인화면용 게시물 조회
    @GetMapping("/overview")
    public ResponseEntity<?> handleGetPostsOverview(@Nullable Authentication authentication){

        PageRequest pageRequest = PageRequest.of(0, 5);

        // 최근 게시물 5개
        Page<PostResponseDto> recentPosts = postService.getPosts(new GetPostsCommand(pageRequest, null, null, null, Collections.emptyList(), Collections.emptyList()));
        // 주간 TOP 게시물 5개
        Page<WeeklyTopPostResponseDto> weeklyTopPosts = postService.getWeeklyTopPosts(pageRequest, null);
        // 명예의 전당 게시물 5개
        Page<PostResponseDto> hallOfFamePosts = postService.getHallOfFamePosts(pageRequest, null);

        return ResponseEntity.ok(
                Response.of(new PostOverviewResponseDto(recentPosts, weeklyTopPosts, hallOfFamePosts))
        );
    }

    // 게시물 등록
    @PostMapping
    public ResponseEntity<?> handlePostRequest(@Valid @RequestBody ExcuseRequestDto dto,
                                               Authentication authentication){

        long memberId = authService.getMemberIdFromAuthentication(authentication);

        Post post = postService.createPost(memberId, dto.situation(), dto.excuse(), dto.tags());

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(post.getId()) // id 다음에 이걸 넣었기 때문에 명시 안해도 순서대로 매칭
                .toUri();

        return ResponseEntity.created(uri).build();
    }

    // 게시물 조회 (다수)
    @PostMapping("/search") // 검색조건 복잡해서...
    public ResponseEntity<?> handleGetPosts(@RequestBody PostSearchRequestDto dto,
                                            @Nullable Authentication authentication){

        Long memberId = null;
        if(authService.isValidUser(authentication)) memberId = authService.getMemberIdFromAuthentication(authentication);

        Pageable pageable = PageRequest.of(dto.pageOrDefault(), dto.sizeOrDefault());
        Page<PostResponseDto> posts = postService.getPosts(
                new GetPostsCommand(
                    pageable,
                    dto.searchInput(),
                    memberId,
                    dto.searchType(),
                    dto.includedTagsOrEmpty(),
                    dto.excludedTagsOrEmpty()
                ));
        PageInfo pageInfo = PageInfo.from(posts);

        return ResponseEntity.ok(
                Response.of(new PageSearchResponseDto<>(posts, pageInfo))
        );
    }

    // 게시물 id를 받아서 해당 게시물을 포함한 페이지 반환
    @GetMapping("/{postId}/page")
    public ResponseEntity<?> handleGetHighlightPost(@PathVariable long postId,
                                                    @Nullable Authentication authentication){

        Long memberId = null;
        if(authService.isValidUser(authentication)) memberId = authService.getMemberIdFromAuthentication(authentication);

        Page<PostResponseDto> posts = postService.getPageIncludesHighlightedPost(new PostHighlightCommand(postId, memberId));

        return ResponseEntity.ok(
                Response.of(new PageSearchResponseDto<>(posts, PageInfo.from(posts)))
        );
    }

    // 명예의 전당 게시글 조회
    @GetMapping("/hall-of-fame")
    public ResponseEntity<?> handleGetHallOfFame(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = RankingScheduler.HALL_OF_FAME_SIZE_STR) int size,
                                                 @Nullable Authentication authentication){

        Long memberId = null;
        if(authService.isValidUser(authentication)) memberId = authService.getMemberIdFromAuthentication(authentication);

        Pageable pageable = PageRequest.of(page, size);
        Page<PostResponseDto> posts = postService.getHallOfFamePosts(pageable, memberId);
        PageInfo pageInfo = PageInfo.from(posts);

        return ResponseEntity.ok(
                Response.of(new PageSearchResponseDto<>(posts, pageInfo))
        );
    }

    // 주간 Top 게시물 조회
    @GetMapping("/weekly-top")
    public ResponseEntity<?> handleGetWeeklyTop(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = RankingScheduler.WEEKLY_TOP_SIZE_STR) int size,
                                                @Nullable Authentication authentication){
        Long memberId = null;
        if(authService.isValidUser(authentication)) memberId = authService.getMemberIdFromAuthentication(authentication);

        Pageable pageable = PageRequest.of(page, size);
        Page<WeeklyTopPostResponseDto> posts = postService.getWeeklyTopPosts(pageable, memberId);
        PageInfo pageInfo = PageInfo.from(posts);

        return ResponseEntity.ok(
                Response.of(new PageSearchResponseDto<>(posts, pageInfo))
        );
    }

    // 게시물 추천/비추천
    @PostMapping("/{postId}/votes")
    public ResponseEntity<?> handleVoteRequest(@PathVariable long postId,
                                               @RequestBody @Valid VoteRequestDto dto,
                                               Authentication authentication){

        long memberId = authService.getMemberIdFromAuthentication(authentication);

        boolean created = postService.voteToPost(new VoteCommand(postId, memberId, dto.voteType()));

        return ResponseEntity.ok(
                Response.of(new SimpleBooleanDto(created))
        );
    }

    // 게시물 수정
    @PatchMapping("/{postId}")
    public ResponseEntity<?> handleUpdateRequest(@PathVariable long postId,
                                                 @RequestBody @Valid ExcuseRequestDto dto,
                                                 Authentication authentication
                                                 ){
        long memberId = authService.getMemberIdFromAuthentication(authentication);

        postService.updatePost(memberId, postId, dto.toUpdateCommand());

        return ResponseEntity.noContent().build();
    }

    // 게시물 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<?> handleDeleteRequest(@PathVariable long postId,
                                                 Authentication authentication){
        long memberId = authService.getMemberIdFromAuthentication(authentication);

        postService.deletePost(new DeleteCommand(postId, memberId));

        return ResponseEntity.noContent().build();
    }

    // 인기 검색어 조회
    @GetMapping("/hot-keywords")
    public ResponseEntity<?> handleGetHotKeywords(){
        List<HotSearchKeywordDto> hotKeywords = postService.getHotSearchKeywords();

        return ResponseEntity.ok(
                Response.ofList(hotKeywords)
        );
    }
}

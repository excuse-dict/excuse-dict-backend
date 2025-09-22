package net.whgkswo.excuse_bundle.entities.posts.core.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.auth.service.AuthService;
import net.whgkswo.excuse_bundle.entities.excuses.dto.ExcuseRequestDto;
import net.whgkswo.excuse_bundle.entities.posts.comments.service.CommentService;
import net.whgkswo.excuse_bundle.entities.posts.core.dto.PostResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.core.dto.PostSummaryResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.core.dto.VoteCommand;
import net.whgkswo.excuse_bundle.entities.posts.core.dto.WeeklyTopPostResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.core.entity.Post;
import net.whgkswo.excuse_bundle.entities.posts.core.service.GetPostsCommand;
import net.whgkswo.excuse_bundle.entities.posts.core.service.PostService;
import net.whgkswo.excuse_bundle.entities.vote.dto.VoteRequestDto;
import net.whgkswo.excuse_bundle.general.dto.DeleteCommand;
import net.whgkswo.excuse_bundle.general.responses.Response;
import net.whgkswo.excuse_bundle.general.responses.dtos.PageSearchResponseDto;
import net.whgkswo.excuse_bundle.general.responses.dtos.SimpleBooleanDto;
import net.whgkswo.excuse_bundle.general.responses.page.PageInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping(PostController.BASE_PATH)
@RequiredArgsConstructor
public class PostController {
    private final AuthService authService;
    private final PostService postService;

    public static final String BASE_PATH = "/api/v1/posts";
    public static final String BASE_PATH_ANY = "/api/*/posts";

    // 게시물 등록
    @PostMapping
    public ResponseEntity<?> handlePostRequest(@Valid @RequestBody ExcuseRequestDto dto,
                                               Authentication authentication){

        long memberId = authService.getMemberIdFromAuthentication(authentication);

        Post post = postService.createPost(memberId, dto.getSituation(), dto.getExcuse(), dto.getTags());

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(post.getId()) // id 다음에 이걸 넣었기 때문에 명시 안해도 순서대로 매칭
                .toUri();

        return ResponseEntity.created(uri).build();
    }

    // 게시물 조회 (다수)
    @GetMapping
    public ResponseEntity<?> handleGetPosts(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(required = false) String searchInput,
                                            @Nullable Authentication authentication){

        Long memberId = null;
        if(authService.isValidUser(authentication)) memberId = authService.getMemberIdFromAuthentication(authentication);

        Pageable pageable = PageRequest.of(page, size);
        Page<PostResponseDto> posts = postService.getPosts(new GetPostsCommand(pageable, searchInput, memberId));
        PageInfo pageInfo = PageInfo.from(posts);

        return ResponseEntity.ok(
                Response.of(new PageSearchResponseDto<>(posts, pageInfo))
        );
    }

    // 명예의 전당 게시글 조회
    @GetMapping("/hall-of-fame")
    public ResponseEntity<?> handleGetHallOfFame(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size,
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
                                                @RequestParam(defaultValue = "10") int size,
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
}

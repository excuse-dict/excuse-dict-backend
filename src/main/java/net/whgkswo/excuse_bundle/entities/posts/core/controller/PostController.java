package net.whgkswo.excuse_bundle.entities.posts.core.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.auth.service.AuthService;
import net.whgkswo.excuse_bundle.entities.excuses.dto.ExcuseRequestDto;
import net.whgkswo.excuse_bundle.entities.posts.comments.dto.*;
import net.whgkswo.excuse_bundle.entities.posts.comments.reply.entity.Reply;
import net.whgkswo.excuse_bundle.entities.posts.comments.service.CommentService;
import net.whgkswo.excuse_bundle.entities.posts.core.dto.PostResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.core.dto.VoteCommand;
import net.whgkswo.excuse_bundle.entities.posts.core.entity.Post;
import net.whgkswo.excuse_bundle.entities.posts.core.service.GetPostsCommand;
import net.whgkswo.excuse_bundle.entities.posts.core.service.PostService;
import net.whgkswo.excuse_bundle.entities.vote.dto.VoteRequestDto;
import net.whgkswo.excuse_bundle.responses.Response;
import net.whgkswo.excuse_bundle.responses.dtos.PageSearchResponseDto;
import net.whgkswo.excuse_bundle.responses.dtos.SimpleBooleanDto;
import net.whgkswo.excuse_bundle.responses.page.PageInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping(PostController.BASE_PATH)
@RequiredArgsConstructor
public class PostController {
    private final AuthService authService;
    private final PostService postService;
    private final CommentService commentService;

    public static final String BASE_PATH = "/api/v1/posts";
    public static final String BASE_PATH_ANY = "/api/*/posts";

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
    @GetMapping
    public ResponseEntity<?> handleGetPosts(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(required = false) String searchInput,
                                            @Nullable Authentication authentication){

        Long memberId = null;
        if(authentication != null) memberId = authService.getMemberIdFromAuthentication(authentication);

        Pageable pageable = PageRequest.of(page, size);
        Page<PostResponseDto> posts = postService.getPosts(new GetPostsCommand(pageable, searchInput, memberId));
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

    // 댓글 작성
    @PostMapping("/{postId}/comments")
    public ResponseEntity<?> handleAddCommentRequest(@PathVariable long postId,
                                                     @RequestBody @Valid CommentRequestDto dto,
                                                     Authentication authentication){
        long memberId = authService.getMemberIdFromAuthentication(authentication);

        commentService.createComment(new CreateCommentCommand(postId, memberId, dto.comment()));

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .build()
                .toUri();

        return ResponseEntity.created(uri).build();
    }

    // 댓글 조회
    @GetMapping("/{postId}/comments")
    public ResponseEntity<?> handleGetComments(@PathVariable long postId,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "5") int size,
                                               @Nullable Authentication authentication
                                               ){

        Optional<Long> memberId = authService.getOptionalMemberIdFromAuthentication(authentication);

        Pageable pageable = PageRequest.of(page, size);
        Page<CommentResponseDto> comments = commentService.getComments(new GetCommentsCommand(postId, memberId.orElse(null), pageable));
        PageInfo pageInfo = PageInfo.from(comments);

        return ResponseEntity.ok(
                Response.of(new PageSearchResponseDto<>(comments, pageInfo))
        );
    }

    // 댓글 추천 / 비추천
    @PostMapping("/comments/{commentId}/votes")
    public ResponseEntity<?> handleCommentVoteRequest(@PathVariable long commentId,
                                                      @RequestBody @Valid VoteRequestDto dto,
                                                      Authentication authentication){
        long memberId = authService.getMemberIdFromAuthentication(authentication);

        boolean created = commentService.voteToComment(new VoteCommand(commentId, memberId, dto.voteType()));

        return ResponseEntity.ok(
                Response.of(new SimpleBooleanDto(created))
        );
    }

    // 대댓글 작성
    @PostMapping("/comments/{commentId}/replies")
    public ResponseEntity<?> handlePostReplyRequest(@PathVariable long commentId,
                                                    @RequestBody @Valid CommentRequestDto dto,
                                                    Authentication authentication){
        long memberId = authService.getMemberIdFromAuthentication(authentication);
        commentService.createReply(new CreateCommentCommand(commentId, memberId, dto.comment()));

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .build()
                .toUri();

        return ResponseEntity.created(uri).build();
    }

    // 대댓글 조회
    @GetMapping("/comments/{commentId}/replies")
    public ResponseEntity<?> handleGetRepliesRequest(@PathVariable long commentId,
                                                     @RequestParam int page,
                                                     @RequestParam int size){

        Pageable pageable = PageRequest.of(page, size);

        Page<ReplyResponseDto> replies = commentService.getReplies(new GetRepliesCommand(commentId, pageable));

        return ResponseEntity.ok(
                Response.of(new PageSearchResponseDto<>(replies, PageInfo.from(replies)))
        );
    }

    // 대댓글 추천/비추천
    @PostMapping("/comments/replies/{replyId}/votes")
    public ResponseEntity<?> handleVoteReplies(@PathVariable long replyId,
                                               @RequestBody @Valid VoteRequestDto dto,
                                               Authentication authentication){

        long memberId = authService.getMemberIdFromAuthentication(authentication);

        boolean created = commentService.voteToReplies(new VoteCommand(replyId, memberId, dto.voteType()));

        return ResponseEntity.ok(
                Response.of(new SimpleBooleanDto(created))
        );
    }
}

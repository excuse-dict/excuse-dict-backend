package net.whgkswo.excuse_bundle.entities.posts.comments.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.auth.service.AuthService;
import net.whgkswo.excuse_bundle.entities.posts.comments.dto.CommentRequestDto;
import net.whgkswo.excuse_bundle.entities.posts.comments.dto.CommentResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.comments.dto.CreateOrUpdateCommentCommand;
import net.whgkswo.excuse_bundle.entities.posts.comments.dto.GetCommentsCommand;
import net.whgkswo.excuse_bundle.entities.posts.comments.service.CommentService;
import net.whgkswo.excuse_bundle.entities.posts.core.controller.PostController;
import net.whgkswo.excuse_bundle.entities.posts.core.dto.VoteCommand;
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
import java.util.Optional;

@RestController
@RequestMapping(PostController.BASE_URL)
@RequiredArgsConstructor
public class CommentController {

    private final AuthService authService;
    private final CommentService commentService;

    // 댓글 작성
    @PostMapping("/{postId}/comments")
    public ResponseEntity<?> handleAddCommentRequest(@PathVariable long postId,
                                                     @RequestBody @Valid CommentRequestDto dto,
                                                     Authentication authentication){
        long memberId = authService.getMemberIdFromAuthentication(authentication);

        commentService.createComment(new CreateOrUpdateCommentCommand(postId, memberId, dto.comment()));

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

    // 댓글 수정
    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<?> handleUpdateComment(@PathVariable long commentId,
                                                 @RequestBody @Valid CommentRequestDto dto,
                                                 Authentication authentication){

        long memberId = authService.getMemberIdFromAuthentication(authentication);

        commentService.updateComment(new CreateOrUpdateCommentCommand(commentId, memberId, dto.comment()));

        return ResponseEntity.noContent().build();
    }

    // 댓글 삭제
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> handleDeleteComment(@PathVariable long commentId,
                                                 Authentication authentication){

        long memberId = authService.getMemberIdFromAuthentication(authentication);

        commentService.deleteComment(new DeleteCommand(commentId, memberId));

        return ResponseEntity.noContent().build();
    }
}

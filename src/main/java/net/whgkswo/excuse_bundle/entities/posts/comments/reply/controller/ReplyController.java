package net.whgkswo.excuse_bundle.entities.posts.comments.reply.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.auth.service.AuthService;
import net.whgkswo.excuse_bundle.entities.posts.comments.dto.CommentRequestDto;
import net.whgkswo.excuse_bundle.entities.posts.comments.dto.CreateOrUpdateCommentCommand;
import net.whgkswo.excuse_bundle.entities.posts.comments.dto.GetRepliesCommand;
import net.whgkswo.excuse_bundle.entities.posts.comments.dto.ReplyResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.comments.reply.service.ReplyService;
import net.whgkswo.excuse_bundle.entities.posts.comments.service.CommentService;
import net.whgkswo.excuse_bundle.entities.posts.core.controller.PostController;
import net.whgkswo.excuse_bundle.entities.posts.core.dto.VoteCommand;
import net.whgkswo.excuse_bundle.entities.vote.dto.VoteRequestDto;
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
@RequestMapping(PostController.BASE_PATH + "/comments")
@RequiredArgsConstructor
public class ReplyController {

    private final AuthService authService;
    private final CommentService commentService;
    private final ReplyService replyService;

    // 대댓글 작성
    @PostMapping("/{commentId}/replies")
    public ResponseEntity<?> handlePostReplyRequest(@PathVariable long commentId,
                                                    @RequestBody @Valid CommentRequestDto dto,
                                                    Authentication authentication){
        long memberId = authService.getMemberIdFromAuthentication(authentication);
        int repliesCount = replyService.createReply(new CreateOrUpdateCommentCommand(commentId, memberId, dto.comment()));

        // 댓글의 현재 답글수를 리턴
        return ResponseEntity.ok(
                Response.simpleNumber(repliesCount)
        );
    }

    // 대댓글 조회
    @GetMapping("/{commentId}/replies")
    public ResponseEntity<?> handleGetRepliesRequest(@PathVariable long commentId,
                                                     @RequestParam int page,
                                                     @RequestParam int size,
                                                     @Nullable Authentication authentication){

        Optional<Long> memberId = authService.getOptionalMemberIdFromAuthentication(authentication);

        Pageable pageable = PageRequest.of(page, size);

        Page<ReplyResponseDto> replies = replyService.getReplies(new GetRepliesCommand(commentId, memberId.orElse(null), pageable));

        return ResponseEntity.ok(
                Response.of(new PageSearchResponseDto<>(replies, PageInfo.from(replies)))
        );
    }

    // 대댓글 추천/비추천
    @PostMapping("/replies/{replyId}/votes")
    public ResponseEntity<?> handleVoteReplies(@PathVariable long replyId,
                                               @RequestBody @Valid VoteRequestDto dto,
                                               Authentication authentication){

        long memberId = authService.getMemberIdFromAuthentication(authentication);

        boolean created = replyService.voteToReplies(new VoteCommand(replyId, memberId, dto.voteType()));

        return ResponseEntity.ok(
                Response.of(new SimpleBooleanDto(created))
        );
    }
}

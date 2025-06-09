package net.whgkswo.excuse_bundle.entities.posts.core.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.auth.service.AuthService;
import net.whgkswo.excuse_bundle.entities.excuses.Excuse;
import net.whgkswo.excuse_bundle.entities.excuses.dto.ExcuseRequestDto;
import net.whgkswo.excuse_bundle.entities.excuses.service.ExcuseService;
import net.whgkswo.excuse_bundle.entities.members.core.entitiy.Member;
import net.whgkswo.excuse_bundle.entities.members.core.service.MemberService;
import net.whgkswo.excuse_bundle.entities.posts.core.dto.PostResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.core.entity.Post;
import net.whgkswo.excuse_bundle.entities.posts.core.service.GetPostCommand;
import net.whgkswo.excuse_bundle.entities.posts.core.service.PostService;
import net.whgkswo.excuse_bundle.responses.Response;
import net.whgkswo.excuse_bundle.responses.dtos.PageSearchResponseDto;
import net.whgkswo.excuse_bundle.responses.page.PageInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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

    @GetMapping
    public ResponseEntity<?> getPostRequest(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(required = false) String searchInput){
        Pageable pageable = PageRequest.of(page, size);
        Page<PostResponseDto> posts = postService.getPosts(new GetPostCommand(pageable, searchInput));
        PageInfo pageInfo = new PageInfo(page, posts.getTotalPages(), posts.getTotalElements(), posts.hasNext());

        return ResponseEntity.ok(
                Response.of(new PageSearchResponseDto<>(posts, pageInfo))
        );
    }
}

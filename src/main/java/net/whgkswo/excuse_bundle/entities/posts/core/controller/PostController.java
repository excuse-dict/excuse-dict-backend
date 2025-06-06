package net.whgkswo.excuse_bundle.entities.posts.core.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.auth.service.AuthService;
import net.whgkswo.excuse_bundle.entities.excuses.Excuse;
import net.whgkswo.excuse_bundle.entities.excuses.dto.ExcuseRequestDto;
import net.whgkswo.excuse_bundle.entities.excuses.service.ExcuseService;
import net.whgkswo.excuse_bundle.entities.members.core.entitiy.Member;
import net.whgkswo.excuse_bundle.entities.members.core.service.MemberService;
import net.whgkswo.excuse_bundle.entities.posts.core.entity.Post;
import net.whgkswo.excuse_bundle.entities.posts.core.service.PostService;
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
}

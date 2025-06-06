package net.whgkswo.excuse_bundle.entities.posts.tags.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.entities.posts.tags.dtos.TagRequestDto;
import net.whgkswo.excuse_bundle.entities.posts.tags.dtos.TagResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.tags.entities.Tag;
import net.whgkswo.excuse_bundle.entities.posts.tags.services.TagService;
import net.whgkswo.excuse_bundle.responses.Response;
import net.whgkswo.excuse_bundle.responses.page.PageInfo;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(TagController.BASE_PATH)
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    public static final String BASE_PATH = "/api/v1/posts/tags";
    public static final String BASE_PATH_ANY = "/api/*/posts/tags";

    // 쿼리 파라미터에 검색조건 배열을 담아 보내느냐, Rest를 포기하고 Post로 받느냐...
    @PostMapping
    public ResponseEntity<?> handleTagRequest(@Valid @RequestBody TagRequestDto dto){

        Page<Tag> tags = tagService.searchTags(dto.categories(), dto.searchValue(), dto.pageOrDefault(), dto.sizeOrDefault());
        PageInfo pageInfo = new PageInfo(dto.pageOrDefault(), tags.getTotalPages(), tags.getTotalElements(), tags.hasNext());

         return ResponseEntity.ok(
                 Response.of(new TagResponseDto(tags, pageInfo))
         );
    }
}

package net.whgkswo.excuse_dict.entities.posts.tags.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_dict.entities.posts.tags.commands.SearchTagCommand;
import net.whgkswo.excuse_dict.entities.posts.tags.dto.TagSearchRequestDto;
import net.whgkswo.excuse_dict.general.responses.dtos.PageSearchResponseDto;
import net.whgkswo.excuse_dict.entities.posts.tags.entity.Tag;
import net.whgkswo.excuse_dict.entities.posts.tags.service.TagService;
import net.whgkswo.excuse_dict.general.responses.Response;
import net.whgkswo.excuse_dict.general.responses.page.PageInfo;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(TagController.BASE_URL)
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    public static final String BASE_URL = "/api/v1/posts/tags";
    public static final String BASE_URL_ANY = "/api/*/posts/tags";

    // 쿼리 파라미터에 검색조건 배열을 담아 보내느냐, Rest를 포기하고 Post로 받느냐...
    @PostMapping
    public ResponseEntity<?> handleTagRequest(@Valid @RequestBody TagSearchRequestDto dto){

        Page<Tag> tags = tagService.searchTags(new SearchTagCommand(dto.categories(), dto.searchValue(), dto.pageOrDefault(), dto.sizeOrDefault()));
        PageInfo pageInfo = PageInfo.from(tags);

         return ResponseEntity.ok(
                 Response.of(new PageSearchResponseDto<>(tags, pageInfo))
         );
    }
}

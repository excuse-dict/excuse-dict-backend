package net.whgkswo.excuse_bundle.entities.posts.tags.controllers;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.entities.posts.tags.dtos.TagResponseDto;
import net.whgkswo.excuse_bundle.entities.posts.tags.entities.Tag;
import net.whgkswo.excuse_bundle.entities.posts.tags.services.TagService;
import net.whgkswo.excuse_bundle.responses.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(TagController.BASE_PATH)
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    public static final String BASE_PATH = "/api/v1/posts/tags";
    public static final String BASE_PATH_ANY = "/api/*/posts/tags";

    @GetMapping
    public ResponseEntity<?> handleTagRequest(@RequestParam @Nullable List<Tag.Type> filterTypes,
                                              @RequestParam @Nullable String searchValue
                                              ){

        List<Tag> tags = tagService.searchTags(filterTypes, searchValue);

         return ResponseEntity.ok(
                 Response.of(new TagResponseDto(tags))
         );
    }
}

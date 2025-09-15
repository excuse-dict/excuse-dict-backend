package net.whgkswo.excuse_bundle.entities.excuses.controller;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.cooldown.Cooldown;
import net.whgkswo.excuse_bundle.entities.excuses.dto.GenerateExcuseDto;
import net.whgkswo.excuse_bundle.gemini.prompt.PromptBuilder;
import net.whgkswo.excuse_bundle.gemini.service.GeminiService;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(ExcuseController.BASE_URL)
@RequiredArgsConstructor
public class ExcuseController {

    public static final String BASE_URL = "/api/v1/excuses";
    public static final String BASE_URL_ANY = "/api/*/excuses";

    private final GeminiService geminiService;
    private final PromptBuilder promptBuilder;

    @PostMapping("/generate")
    @Cooldown(memberSeconds = 5, guestSeconds = 60)
    public Mono<String> generateExcuse(@RequestBody @Valid GenerateExcuseDto dto,
                                       @Nullable Authentication authentication){

        String prompt = promptBuilder.buildExcusePrompt(dto.situation());

        return geminiService.generateText(prompt);
    }
}

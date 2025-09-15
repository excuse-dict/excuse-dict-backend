package net.whgkswo.excuse_bundle.entities.excuses.controller;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.gemini.prompt.PromptBuilder;
import net.whgkswo.excuse_bundle.gemini.service.GeminiService;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(ExcuseController.BASE_URL)
@RequiredArgsConstructor
public class ExcuseController {

    public static final String BASE_URL = "/api/v1/excuses";
    public static final String BASE_URL_ANY = "/api/*/excuses";

    private final GeminiService geminiService;
    private final PromptBuilder promptBuilder;

    @GetMapping("/generate")
    public Mono<String> generateExcuse(@RequestParam @Length(min = 5, max = 100, message = "상황은 5~100자로 입력해주세요.") String situation){

        String prompt = promptBuilder.buildExcusePrompt(situation);

        return geminiService.generateText(prompt);
    }
}

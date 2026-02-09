package net.whgkswo.excuse_dict.entities.excuses.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.whgkswo.excuse_dict.auth.recaptcha.RecaptchaService;
import net.whgkswo.excuse_dict.entities.excuses.Excuse;
import net.whgkswo.excuse_dict.entities.excuses.dto.UpdateExcuseCommand;
import net.whgkswo.excuse_dict.entities.excuses.repository.ExcuseRepository;
import net.whgkswo.excuse_dict.entities.posts.tags.entity.Tag;
import net.whgkswo.excuse_dict.entities.posts.tags.service.TagService;
import net.whgkswo.excuse_dict.exceptions.BusinessLogicException;
import net.whgkswo.excuse_dict.exceptions.ExceptionType;
import net.whgkswo.excuse_dict.gemini.dto.GenerateExcuseResponseDto;
import net.whgkswo.excuse_dict.gemini.prompt.PromptBuilder;
import net.whgkswo.excuse_dict.gemini.service.GeminiService;
import net.whgkswo.excuse_dict.komoran.KomoranHelper;
import net.whgkswo.excuse_dict.search.SearchType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcuseService {
    private final TagService tagService;
    private final GeminiService geminiService;
    private final PromptBuilder promptBuilder;
    private final RecaptchaService recaptchaService;
    private final KomoranHelper komoranHelper;
    private final ExcuseRepository excuseRepository;

    // 핑계 등록
    public Excuse createExcuse(String situation, String excuseStr, Set<String> tagKeys){
        Excuse excuse = new Excuse();

        excuse.setSituation(situation);
        excuse.setExcuse(excuseStr);

        Set<Tag> tags = tagKeys.stream()
                        .map(tagService::tagKeyToTag)
                        .collect(Collectors.toSet());

        excuse.setTags(tags);

        // 형태소 저장
        List<String> situationMorphemes = komoranHelper.getMeaningfulMorphemes(situation);
        excuse.setSituationMorphemes(new HashSet<>(situationMorphemes));

        List<String> excuseMorphemes = komoranHelper.getMeaningfulMorphemes(excuseStr);
        excuse.setExcuseMorphemes(new HashSet<>(excuseMorphemes));

        // post 등록하며 함께 등록되기 때문에 저장 없이 반환
        return excuse;
    }

    // 핑계 수정
    public void updateExcuse(Excuse excuse, UpdateExcuseCommand command){
        command.situation().ifPresent(excuse::setSituation);
        command.excuseStr().ifPresent(excuse::setExcuse);
        command.tagKeys().ifPresent(tagKeys -> {
                excuse.setTags(tagKeys.stream()
                        .map(tagService::tagKeyToTag)
                        .collect(Collectors.toSet())
                );
            }
        );
    }

    // AI로 핑계 생성
    public Mono<GenerateExcuseResponseDto> generateExcuse(String situation, String recaptchaToken){
        // 리캡챠 토큰 검증
        recaptchaService.verifyRecaptcha(recaptchaToken);

        String prompt = promptBuilder.buildExcusePrompt(situation, 5);

        return geminiService.generateText(prompt, GenerateExcuseResponseDto.class)
                .flatMap(answer -> {
                    if(answer.getData().size() <= 1) return Mono.error(new BusinessLogicException(ExceptionType.IMPROPER_SITUATION));
                    return Mono.just(answer);
                });
    }

    // AI로 핑계 생성 (동기)
    public GenerateExcuseResponseDto generateExcuseInSynchronous(String situation, String recaptchaToken){
        // 리캡챠 토큰 검증
        recaptchaService.verifyRecaptcha(recaptchaToken);

        String prompt = promptBuilder.buildExcusePrompt(situation, 5);

        GenerateExcuseResponseDto answer = geminiService.generateText(prompt, GenerateExcuseResponseDto.class).block(Duration.ofSeconds(30));

        if(answer == null || answer.getData().size() <= 1) throw new BusinessLogicException(ExceptionType.IMPROPER_SITUATION);

        return answer;
    }

    public List<String> getMorphemes(SearchType searchType){
        if(searchType == null) return Collections.emptyList();

        return switch (searchType){
            case SITUATION -> excuseRepository.findAllSituationMorphemes();
            case EXCUSE -> excuseRepository.findAllExcuseMorphemes();
            case SITUATION_AND_EXCUSE -> excuseRepository.findAllMorphemes();
            default -> Collections.emptyList();
        };
    }

    // 형태소 필드 추가 전 데이터 마이그레이션
    @Async
    @Transactional
    public void migrateMorphemes() {

        List<Excuse> excuses = excuseRepository.findAll();
        int total = excuses.size();

        log.info("=== 마이그레이션 시작: {}건 ===", total);

        int processed = 0;
        for (Excuse excuse : excuses) {

            if (!excuse.getSituationMorphemes().isEmpty()
                    && !excuse.getExcuseMorphemes().isEmpty()) continue;

            List<String> situationMorphemes = komoranHelper.getMeaningfulMorphemes(excuse.getSituation());
            excuse.setSituationMorphemes(new HashSet<>(situationMorphemes));

            List<String> excuseMorphemes = komoranHelper.getMeaningfulMorphemes(excuse.getExcuse());
            excuse.setExcuseMorphemes(new HashSet<>(excuseMorphemes));

            processed++;

            // 50개마다 로그
            if (processed % 50 == 0) {
                log.info(">>> 진행: {}/{} ({}%)", processed, total, processed * 100 / total);
            }
        }

        log.info("=== 완료: {}건 ===", processed);
    }
}

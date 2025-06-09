package net.whgkswo.excuse_bundle.komoran;

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import kr.co.shineware.nlp.komoran.model.Token;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KomoranService {
    private final Komoran komoran = new Komoran(DEFAULT_MODEL.LIGHT);

    // 불용어 목록 (의미없는 단어들)
    private static final Set<String> STOP_WORDS = Set.of(
            "것", "수", "때", "곳", "데", "등", "점", "경우", "때문",
            "정도", "생각", "마음", "기분", "느낌", "상태", "상황",
            "하다", "되다", "있다", "없다", "같다", "이다"
    );

    // 사용자 입력을 분석해 의미있는 형태소 추출
    public List<String> getMeaningfulMorphemes(String userInput) {
        try {
            KomoranResult result = komoran.analyze(userInput);

            return result.getTokenList().stream()
                    .filter(token -> MeaningfulPosTag.isMeaningful(token.getPos()))  // 유용한 품사만
                    .map(Token::getMorph)  // 형태소만 추출
                    .filter(morph -> morph.length() >= 2)  // 2글자 이상 (조사 등 제외)
                    .filter(morph -> !isStopWord(morph))   // 불용어 제거
                    .distinct()  // 중복 제거
                    .collect(Collectors.toList());

        } catch (Exception e) {
            // 분석 실패 시 띄어쓰기 방식으로 대체
            return Arrays.stream(userInput.toLowerCase().split("\\s+"))
                    .filter(word -> word.length() >= 2)
                    .collect(Collectors.toList());
        }
    }

    // 불용어 체크
    private boolean isStopWord(String word) {
        return STOP_WORDS.contains(word);
    }
}
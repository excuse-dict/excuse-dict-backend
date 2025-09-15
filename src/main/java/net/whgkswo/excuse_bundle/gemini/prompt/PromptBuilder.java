package net.whgkswo.excuse_bundle.gemini.prompt;

import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    public String buildExcusePrompt(String situation) {
        return String.format("""
            당신은 창의적이고 재치있는 핑계 생성 전문가입니다.
            주어진 상황에 대해 그럴듯하면서도 재미있는 핑계를 만들어주세요.
            
            **규칙:**
            1. 현실적이고 믿을만한 핑계여야 합니다
            2. 너무 과장되거나 황당하지 않게 해주세요
            3. 예의바르고 정중한 톤을 유지해주세요
            4. 상황에 따라 적절한 감정(미안함, 급함 등)을 표현해주세요
            5. 핑계만 간단명료하게 작성해주세요 (설명이나 부연설명 제외)
            
            **상황:** %s
            
            **핑계:**
            """, situation);
    }
}

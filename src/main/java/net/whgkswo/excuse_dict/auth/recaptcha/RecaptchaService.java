package net.whgkswo.excuse_dict.auth.recaptcha;

import net.whgkswo.excuse_dict.exceptions.BusinessLogicException;
import net.whgkswo.excuse_dict.exceptions.ExceptionType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class RecaptchaService {
    @Value("${recaptcha.secret_key}")
    private String secretKey;

    private static final String RECAPTCHA_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    public void verifyRecaptcha(String recaptchaToken) {
        try {
            // 요청 송신 주체
            RestTemplate restTemplate = new RestTemplate();

            // 요청 파라미터 구성
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("secret", secretKey); // 백엔드 시크릿 키
            params.add("response", recaptchaToken); // 프론트에서 넘겨받은 토큰

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // HTTP 요청 생성
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            // 구글 API 호출 및 응답 수신
            RecaptchaResponseDto response = restTemplate.postForObject(
                    RECAPTCHA_VERIFY_URL, request, RecaptchaResponseDto.class);

            // 응답 못 받음
            if(response == null) throw new BusinessLogicException(ExceptionType.RECAPTCHA_VERIFY_FAILED);

            // 검증 실패
            if(!response.success()) throw new BusinessLogicException(ExceptionType.RECAPTCHA_TOKEN_INVALID);

        } catch (BusinessLogicException e){
            throw e;
        } catch (Exception e) {
            throw new BusinessLogicException(ExceptionType.RECAPTCHA_VERIFY_FAILED);
        }
    }
}

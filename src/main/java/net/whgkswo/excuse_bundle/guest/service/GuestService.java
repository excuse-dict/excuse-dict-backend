package net.whgkswo.excuse_bundle.guest.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.auth.jwt.token.tokenizer.JwtTokenizer;
import net.whgkswo.excuse_bundle.exceptions.BusinessLogicException;
import net.whgkswo.excuse_bundle.exceptions.ExceptionType;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GuestService {

    private final JwtTokenizer jwtTokenizer;

    // 비로그인 클라이언트 식별용 게스트 토큰 발급
    public String generateGuestToken(String existingToken) {

        if(existingToken != null) {
            getUuidFromGuestToken(existingToken); // 토큰 유효성 검증용
            return existingToken;
        };

        Map<String, Object> claims = new HashMap<>();
        claims.put("uuid", UUID.randomUUID().toString());   // 랜덤 uuid로 클라이언트 식별

        String subject = "guest";
        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());

        // 만료시간 없음 (영구 토큰)
        return jwtTokenizer.generateToken(claims, subject, null, base64EncodedSecretKey);
    }

    public String getUuidFromGuestToken(String token) {
        try {
            String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());
            Claims claims = jwtTokenizer.getClaims(token, base64EncodedSecretKey).getBody();

            return claims.get("uuid", String.class);
        } catch (JwtException e) {
            throw new BusinessLogicException(ExceptionType.GUEST_TOKEN_INVALID);
        }
    }
}

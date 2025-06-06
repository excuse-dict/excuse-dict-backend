package net.whgkswo.excuse_bundle.auth.jwt.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.auth.jwt.token.scheme.TokenPrefix;
import net.whgkswo.excuse_bundle.auth.jwt.token.tokenizer.JwtTokenizer;
import net.whgkswo.excuse_bundle.entities.members.core.entitiy.Member;
import net.whgkswo.excuse_bundle.entities.members.core.service.MemberService;
import net.whgkswo.excuse_bundle.exceptions.BusinessLogicException;
import net.whgkswo.excuse_bundle.exceptions.ExceptionType;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtTokenizer jwtTokenizer;
    private final MemberService memberService;

    // 액세스 토큰 발행
    public String generateAccessToken(Member member, TokenPrefix prefix){
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", member.getEmail());
        claims.put("memberId", member.getId());
        claims.put("roles", member.getRoles());

        String subject = member.getEmail();
        Date expiration = jwtTokenizer.getTokenExpiration(jwtTokenizer.getAccessTokenExpirationMinutes());

        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());

        String accessToken = jwtTokenizer.generateAccessToken(claims, subject, expiration, base64EncodedSecretKey);

        return prefix.getValue() + accessToken;
    }

    // 리프레시 토큰 발행
    public String generateRefreshToken(Member member){
        String subject = member.getEmail();
        Date expiration = jwtTokenizer.getTokenExpiration(jwtTokenizer.getRefreshTokenExpirationMinutes());
        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());

        String refreshToken = jwtTokenizer.generateRefreshToken(subject, expiration, base64EncodedSecretKey);

        return refreshToken;
    }

    // 액세스 토큰 재발급
    public String refreshAccessToken(String refreshToken){
        try{
            // 리프레시 토큰 검증
            Member member = validateRefreshTokenAndGetMember(refreshToken);

            // 액세스 토큰 재발급
            return generateAccessToken(member, TokenPrefix.BEARER);

        }catch (ExpiredJwtException e){
            throw new BusinessLogicException(ExceptionType.REFRESH_TOKEN_EXPIRED);
        }catch (Exception e){
            throw new BusinessLogicException(ExceptionType.REFRESH_TOKEN_INVALID);
        }
    }

    // 리프레시 토큰 검증 (유효하면 member 반환)
    private Member validateRefreshTokenAndGetMember(String refreshToken){
        // Claims 추출
        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());
        Claims claims = jwtTokenizer.getClaims(refreshToken, base64EncodedSecretKey).getBody();

        // 사용자 정보 조회
        String email = claims.getSubject();
        return memberService.findByEmail(email);
    }
}

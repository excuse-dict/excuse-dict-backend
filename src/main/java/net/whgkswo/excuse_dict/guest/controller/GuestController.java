package net.whgkswo.excuse_dict.guest.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_dict.guest.service.GuestService;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(GuestController.BASE_URL)
@RequiredArgsConstructor
public class GuestController {

    private final GuestService guestService;

    public static final String BASE_URL = "/api/v1/guests";
    public static final String BASE_URL_ANY = "/api/*/guests";

    // 비로그인 클라이언트 식별용 게스트 토큰 발급
    @PostMapping("/tokens")
    public ResponseEntity<?> handlePostGuestToken(HttpServletRequest request,
                                                  HttpServletResponse response,
                                                  @CookieValue(value = "guestToken", required = false) String existingToken){

        // 게스트 토큰 발급
        String guestToken = guestService.generateGuestToken(existingToken);

        // 쿠키에 저장
        ResponseCookie guestCookie = ResponseCookie.from("guestToken", guestToken)
                .domain(".exdict.site")
                .httpOnly(true)
                .path("/")
                .maxAge(-1)
                .sameSite("None")
                .secure(true)
                .build();

        response.addHeader("Set-Cookie", guestCookie.toString());

        // 클라이언트가 쿠키를 사용할 수 있는 환경인지 검증하기 위한 값
        ResponseCookie testCookie = ResponseCookie.from("cookieTest", "ok")
                .domain(".exdict.site")
                .httpOnly(false) // 클라이언트에서 접근 가능하게
                .path("/")
                .maxAge(30) // 30초 후 만료
                .sameSite("None")
                .secure(true)
                .build();

        response.addHeader("Set-Cookie", testCookie.toString());

        return ResponseEntity.noContent().build();
    }
}

package net.whgkswo.excuse_bundle.guest.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.general.responses.Response;
import net.whgkswo.excuse_bundle.guest.service.GuestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(GuestController.BASE_URL)
@RequiredArgsConstructor
public class GuestController {

    private GuestService guestService;

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
        Cookie cookie = new Cookie("guestToken", guestToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(-1);
        response.addCookie(cookie);

        return ResponseEntity.ok(
                Response.simpleString(guestToken)
        );
    }
}

package net.whgkswo.excuse_bundle.exceptions;

import net.whgkswo.excuse_bundle.auth.verify.VerificationCode;

public record ExceptionType(int status, String message){

    public static ExceptionType of(int status, String message){
        return new ExceptionType(status, message);
    }

    // 고정 메시지들
    public static final ExceptionType DUPLICATED_EMAIL = ExceptionType.of(400, "이미 가입하신 이메일입니다.");
    public static final ExceptionType WRONG_VERIFICATION_CODE_LAST = ExceptionType.of(400, "인증 코드가 틀립니다. 모든 시도 횟수를 소진하였습니다. 코드를 재발급해 주세요");
    public static final ExceptionType VERIFICATION_CODE_EXPIRED = ExceptionType.of(400, "인증 코드가 발급되지 않았거나 만료되었습니다.");
    public static final ExceptionType WRONG_CHARACTER_IN_NICKNAME = ExceptionType.of(400, "닉네임에 사용할 수 없는 문자가 있습니다.");
    public static final ExceptionType EMAIL_NOT_VERIFIED = ExceptionType.of(400, "이메일 인증이 완료되지 않았거나 인증 정보가 만료되었습니다.");
    public static final ExceptionType INVALID_RECAPTCHA_TOKEN = ExceptionType.of(400, "잘못된 reCAPTCHA 토큰입니다. 페이지를 새로고침하거나 잠시 후에 시도해 주세요");
    public static final ExceptionType MEMBER_NOT_FOUND = ExceptionType.of(404, "회원을 찾을 수 없습니다.");
    public static final ExceptionType REDIS_CONNECTION_LOST = ExceptionType.of(500, "Redis 서버 연결 불가");
    public static final ExceptionType FAILED_TO_SEND_MAIL = ExceptionType.of(500, "메일 전송 실패!");
    public static final ExceptionType SERIALIZATION_FAILED = ExceptionType.of(500, "데이터 직렬화에 실패하였습니다.");
    public static final ExceptionType DESERIALIZATION_FAILED = ExceptionType.of(500, "데이터 역직렬화에 실패하였습니다.");
    public static final ExceptionType RECAPTCHA_VERIFY_FAILED = ExceptionType.of(500, "reCAPTCHA 검증에 실패했습니다.");

    // 이 밑으로 동적 메시지들
    public static ExceptionType wrongVerificationCode(VerificationCode code){
        String message = String.format("인증 코드가 틀립니다. 다시 확인해 주세요. (남은 횟수: %d회)", code.getRemainingAttempts());
        return ExceptionType.of(400, message);
    }
}

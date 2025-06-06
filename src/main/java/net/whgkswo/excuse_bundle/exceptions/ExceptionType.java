package net.whgkswo.excuse_bundle.exceptions;

import net.whgkswo.excuse_bundle.auth.verify.VerificationCode;

public record ExceptionType(int status, String code, String message){

    private static ExceptionType of(int status, String code, String message){
        return new ExceptionType(status, code, message);
    }

    public static ExceptionType fromException(int status, Exception e){
        return ExceptionType.of(status, e.getClass().getSimpleName(), e.getMessage());
    }

    // 고정 메시지들
    public static final ExceptionType DUPLICATED_EMAIL = ExceptionType.of(400, "DUPLICATED_EMAIL", "이미 가입하신 이메일입니다.");
    public static final ExceptionType WRONG_VERIFICATION_CODE_LAST = ExceptionType.of(400, "WRONG_VERIFICATION_CODE_LAST","인증 코드가 틀립니다. 모든 시도 횟수를 소진하였습니다. 코드를 재발급해 주세요");
    public static final ExceptionType VERIFICATION_CODE_EXPIRED = ExceptionType.of(400, "VERIFICATION_CODE_EXPIRED","인증 코드가 발급되지 않았거나 만료되었습니다.");
    public static final ExceptionType WRONG_CHARACTER_IN_NICKNAME = ExceptionType.of(400, "WRONG_CHARACTER_IN_NICKNAME","닉네임에 사용할 수 없는 문자가 있습니다.");
    public static final ExceptionType EMAIL_NOT_VERIFIED = ExceptionType.of(400, "EMAIL_NOT_VERIFIED","이메일 인증이 완료되지 않았거나 인증 정보가 만료되었습니다.");
    public static final ExceptionType RECAPTCHA_TOKEN_INVALID = ExceptionType.of(400, "RECAPTCHA_TOKEN_INVALID","잘못된 reCAPTCHA 토큰입니다. 페이지를 새로고침하거나 잠시 후에 시도해 주세요");
    public static final ExceptionType JSON_FORMAT_INVALID = ExceptionType.of(400, "JSON_FORMAT_INVALID", "잘못된 형식의 JSON입니다.");
    public static final ExceptionType AUTHENTICATION_FAILED = ExceptionType.of(401, "AUTHENTICATION_FAILED", "인증에 실패하였습니다.");
    public static final ExceptionType ACCESS_TOKEN_INVALID = ExceptionType.of(401, "ACCESS_TOKEN_INVALID", "액세스 토큰이 유효하지 않습니다.");
    public static final ExceptionType ACCESS_TOKEN_EXPIRED = ExceptionType.of(401, "ACCESS_TOKEN_EXPIRED", "액세스 토큰이 만료되었습니다. 다시 발급해주세요.");
    public static final ExceptionType REFRESH_TOKEN_INVALID = ExceptionType.of(401, "REFRESH_TOKEN_INVALID", "리프레시 토큰이 유효하지 않습니다.");
    public static final ExceptionType REFRESH_TOKEN_EXPIRED = ExceptionType.of(401, "REFRESH_TOKEN_EXPIRED", "리프레시 토큰이 만료되었습니다. 다시 발급해주세요.");
    public static final ExceptionType MEMBER_NOT_FOUND = ExceptionType.of(404, "MEMBER_NOT_FOUND","회원을 찾을 수 없습니다.");
    public static final ExceptionType MEMBER_NOT_FOUND_BY_EMAIL = ExceptionType.of(404, "MEMBER_NOT_FOUND_BY_EMAIL","해당 이메일로 가입된 회원을 찾을 수 없습니다.");
    public static final ExceptionType REDIS_CONNECTION_LOST = ExceptionType.of(500, "REDIS_CONNECTION_LOST","Redis 서버 연결 불가");
    public static final ExceptionType FAILED_TO_SEND_MAIL = ExceptionType.of(500, "FAILED_TO_SEND_MAIL","메일 전송 실패!");
    public static final ExceptionType SERIALIZATION_FAILED = ExceptionType.of(500, "SERIALIZATION_FAILED","데이터 직렬화에 실패하였습니다.");
    public static final ExceptionType DESERIALIZATION_FAILED = ExceptionType.of(500, "DESERIALIZATION_FAILED","데이터 역직렬화에 실패하였습니다.");
    public static final ExceptionType RECAPTCHA_VERIFY_FAILED = ExceptionType.of(500, "RECAPTCHA_VERIFY_FAILED","reCAPTCHA 검증에 실패했습니다.");

    // ㅡㅡㅡㅡㅡ 이 밑으로 동적 메시지들 ㅡㅡㅡㅡㅡ

    public static ExceptionType wrongVerificationCode(VerificationCode code){
        String message = String.format("인증 코드가 틀립니다. 다시 확인해 주세요. (남은 횟수: %d회)", code.getRemainingAttempts());
        return ExceptionType.of(400, "WRONG_VERIFICATION_CODE", message);
    }

    public static ExceptionType tooManyVerificationCodeRequest(long timeToWait){
        return ExceptionType.of(429, "TOO_MANY_VERIFICATION_CODE_REQUEST", String.format("연달아 코드를 발급하실 수 없습니다. %d초 후 다시 시도해주세요.", timeToWait));
    }

    public static ExceptionType nicknameLengthInvalid(int minLength, int maxLength){
        return ExceptionType.of(400, "NICKNAME_LENGTH_VALID", String.format("닉네임은 %d~%d자 사이어야 합니다.", minLength, maxLength));
    }

    public static ExceptionType jsonFieldInvalid(String fieldName, String rejectedValue){
        return ExceptionType.of(400, "JSON_FIELD_INVALID", String.format("%s 필드의 값이 유효하지 않습니다: %s", fieldName, rejectedValue));
    }

    public static ExceptionType dtoValidationFailed(String validationMessage){
        return ExceptionType.of(400, "DTO_VALIDATION_FAILED", validationMessage);
    }

    public static ExceptionType tagNotFound(String tagKey){
        return ExceptionType.of(404, "TAG_NOT_FOUND", tagKey + "에 해당하는 태그가 없습니다.");
    }
}

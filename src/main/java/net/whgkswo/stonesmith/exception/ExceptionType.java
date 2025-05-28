package net.whgkswo.stonesmith.exception;

public record ExceptionType(int status, String message){

    public static ExceptionType of(int status, String message){
        return new ExceptionType(status, message);
    }

    public static final ExceptionType DUPLICATED_EMAIL = ExceptionType.of(400, "이미 가입하신 이메일입니다.");
    public static final ExceptionType WRONG_VERIFICATION_CODE = ExceptionType.of(400, "인증 코드가 틀립니다. 다시 확인해 주세요");
    public static final ExceptionType VERIFICATION_CODE_EXPIRED = ExceptionType.of(400, "인증 코드가 발급되지 않았거나 만료되었습니다.");
    public static final ExceptionType WRONG_CHARACTER_IN_NICKNAME = ExceptionType.of(400, "닉네임에 사용할 수 없는 문자가 있습니다.");
    public static final ExceptionType EMAIL_NOT_VERIFIED = ExceptionType.of(400, "이메일 인증이 완료되지 않았거나 인증 정보가 만료되었습니다.");
    public static final ExceptionType MEMBER_NOT_FOUND = ExceptionType.of(404, "회원을 찾을 수 없습니다.");
    public static final ExceptionType REDIS_CONNECTION_LOST = ExceptionType.of(500, "Redis 서버 연결 불가");
    public static final ExceptionType FAILED_TO_SEND_MAIL = ExceptionType.of(500, "메일 전송 실패!");
}

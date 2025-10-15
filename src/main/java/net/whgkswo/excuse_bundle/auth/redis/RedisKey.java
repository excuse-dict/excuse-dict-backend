package net.whgkswo.excuse_bundle.auth.redis;

public record RedisKey(Prefix prefix, String identifier) {

    // 직렬화
    @Override
    public String toString(){
        return prefix.name().toLowerCase() + ":" + identifier.toLowerCase();
    }

    public enum Prefix {
        GENERATOR_COOLDOWN, // 핑계 생성기 API 호출 쿨다운
        HALL_OF_FAME, // 명예의 전당 게시글
        VERIFICATION_BLOCK, // 인증 임시 차단됨
        VERIFICATION_CODE_FOR_REGISTRATION, // 회원가입용 인증코드
        VERIFICATION_CODE_TO_RESET_PASSWORD, // 비밀번호 재설정용 인증코드
        VERIFICATION_COMPLETE_REGISTRATION, // 회원가입 메일 인증여부
        VERIFICATION_COMPLETE_RESET_PASSWORD, // 비밀번호 재설정 메일 인증여부
        PASSWORD_RESET_TOKEN, // 비밀번호 재설정 인증 후 토큰
        WEEKLY_TOP, // 주간 Top 게시글
    }
}

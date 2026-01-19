package net.whgkswo.excuse_dict.entities.members.email.dto;

public record EmailVerificationStateDto(
        String code,
        int failedAttempts
) {

    public static final int MAX_VERIFICATION_ATTEMPTS = 5;

    public int getRemainingAttempts(){
        return MAX_VERIFICATION_ATTEMPTS - failedAttempts;
    }

    public EmailVerificationStateDto plusFailedAttempt(){
        return new EmailVerificationStateDto(code, failedAttempts + 1);
    }
}

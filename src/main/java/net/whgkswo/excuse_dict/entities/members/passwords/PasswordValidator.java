package net.whgkswo.excuse_dict.entities.members.passwords;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 128;
    private static final String ALLOWED_SPECIAL_CHARS = "!@#$%&*";

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        // 초기화
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isEmpty()) {
            addViolation(context, "비밀번호를 입력해주세요.");
            return false;
        }

        boolean isValid = true;
        context.disableDefaultConstraintViolation();

        // 길이
        if (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            addViolation(context, String.format("비밀번호는 %d자 이상 %d자 이하여야 합니다.",
                    MIN_PASSWORD_LENGTH, MAX_PASSWORD_LENGTH));
            isValid = false;
        }

        // 영문 소문자
        if (!password.matches(".*[a-z].*")) {
            addViolation(context, "영문 소문자를 포함해야 합니다.");
            isValid = false;
        }

        // 영문 대문자
        if (!password.matches(".*[A-Z].*")) {
            addViolation(context, "영문 대문자를 포함해야 합니다.");
            isValid = false;
        }

        // 숫자
        if (!password.matches(".*\\d.*")) {
            addViolation(context, "숫자를 포함해야 합니다.");
            isValid = false;
        }

        // 특수문자
        String specialCharPattern = ".*[" + Pattern.quote(ALLOWED_SPECIAL_CHARS) + "].*";
        if (!password.matches(specialCharPattern)) {
            addViolation(context, "특수문자를 포함해야 합니다.");
            isValid = false;
        }

        // 허용되지 않은 문자
        String allowedPattern = "^[a-zA-Z0-9" + Pattern.quote(ALLOWED_SPECIAL_CHARS) + "]*$";
        if (!password.matches(allowedPattern)) {
            addViolation(context, "허용되지 않은 문자가 포함되어 있습니다.");
            isValid = false;
        }

        return isValid;
    }

    private void addViolation(ConstraintValidatorContext context, String message) {
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
}

package net.whgkswo.excuse_bundle.auth.verify;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerificationCode {

    private String code;
    private int remainingAttempts;

    public static final int MAX_ATTEMPTS = 5;

    public VerificationCode(String code){
        this.code = code;
        remainingAttempts = MAX_ATTEMPTS;
    }

    public void deductAttempts(){
        remainingAttempts--;
    }
}

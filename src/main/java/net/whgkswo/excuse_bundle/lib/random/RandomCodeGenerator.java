package net.whgkswo.excuse_bundle.lib.random;

import net.whgkswo.excuse_bundle.auth.verify.VerificationCode;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class RandomCodeGenerator {

    // 랜덤 코드 생성
    public String generateRandomCode(int length){
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder();

        for(int i = 0; i < length; i++){
            int index = random.nextInt(characters.length());
            code.append(characters.charAt(index));
        }
        return code.toString();
    }
}

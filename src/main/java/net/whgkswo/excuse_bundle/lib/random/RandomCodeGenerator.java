package net.whgkswo.excuse_bundle.lib.random;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class RandomCodeGenerator {

    private final Random random = new Random();

    // 랜덤 코드 생성
    public String generateRandomCode(int length){
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        StringBuilder code = new StringBuilder();

        for(int i = 0; i < length; i++){
            int index = random.nextInt(characters.length());
            code.append(characters.charAt(index));
        }
        return code.toString();
    }
}

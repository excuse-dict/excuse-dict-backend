package net.whgkswo.excuse_dict.random;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomHelper {

    // 각 값에 대한 가중치를 입력받아 랜덤 반환
    public static <T> T getWeightedRandomValue(Map<T, Integer> weights){
        // static random은 멀티스레딩 성능 저하 발생
        // new Random()은 객체 생성 오버헤드 발생
        Random random = ThreadLocalRandom.current(); // 각 스레드마다 독립된 Random객체 사용

        // 전체 확률은 모든 가중치의 합 (100일 필요 없음)
        int totalWeight = weights.values().stream().mapToInt(Integer::intValue).sum();

        int randomValue = random.nextInt(totalWeight);

        int cumulative = 0;
        for(Map.Entry<T, Integer> entry : weights.entrySet()){
            cumulative += entry.getValue();
            if(randomValue < cumulative) return entry.getKey();
        }

        throw new IllegalStateException("타입스크립트 never 같은건데 여기까지 올 일 없음. 컴파일러 return 만족용");
    }
}

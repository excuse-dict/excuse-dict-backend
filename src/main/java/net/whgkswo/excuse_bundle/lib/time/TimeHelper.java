package net.whgkswo.excuse_bundle.lib.time;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class TimeHelper {

    public Duration getTimeDifference(LocalDateTime timeA, LocalDateTime timeB){
        return Duration.between(timeA, timeB);
    }

    public Duration getTimeAgo(LocalDateTime time){
        return getTimeDifference(time, LocalDateTime.now());
    }

    public long getMinutesAgo(LocalDateTime time){
        return getTimeAgo(time).toMinutes();
    }

    public static String formatSeconds(int seconds){
        if(seconds < 60) return seconds + "초";

        if(seconds < 3600) {
            return String.format("%d분 %d초", seconds / 60, seconds % 60);
        }

        return String.format("%d시간 %d분 %d초", seconds / 3600, (seconds % 3600) / 60, seconds % 60);
    }
}

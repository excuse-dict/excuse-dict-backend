package net.whgkswo.excuse_bundle.lib;

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
}

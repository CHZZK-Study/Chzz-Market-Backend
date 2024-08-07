package org.chzz.market.common.util;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimeUtil {
    /**
     * 주어진 종료 시간까지 남은 시간을 초 단위로 계산합니다.
     * 현재 시간보다 종료 시간이 이전인 경우 0을 반환합니다.
     *
     * @param endDateTime 계산할 종료 시간
     * @return 종료 시간까지 남은 초 단위 시간, 현재 시간이 종료 시간보다 이후인 경우 0
     */
    public static Long calculateSecondsUntilEnd(LocalDateTime endDateTime) {
        long seconds = Duration.between(LocalDateTime.now(), endDateTime).getSeconds();
        return seconds < 0 ? 0L : seconds;
    }
}

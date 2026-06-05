package ru.abondin.hreasy.platform.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.config.HrEasyCommonProps;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Limit requests in time window
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimiter {

    private final HrEasyCommonProps props;

    private final ConcurrentHashMap<Integer, ConcurrentLinkedQueue<OffsetDateTime>> requestTimestampsRateLimiterCache = new ConcurrentHashMap<>();

    /**
     * Do not allow send more than 5 support request in one hour
     *
     * @param employeeId
     * @param now
     * @return
     */
    public Mono<Boolean> checkSupportRequestRateLimit(Integer employeeId, OffsetDateTime now) {
        return Mono.defer(() -> {
            var timestamps = requestTimestampsRateLimiterCache.computeIfAbsent(employeeId, k -> new ConcurrentLinkedQueue<>());
            synchronized (timestamps) {
                // Remove timestamps older than 1 hour
                while (!timestamps.isEmpty() && ChronoUnit.HOURS.between(timestamps.peek(), now) >= 1) {
                    timestamps.poll();
                }

                if (timestamps.size() >= props.getMaxSupportRequestsInHour()) {
                    return Mono.error(new BusinessError("errors.support.request.rate_limit_exceeded", Integer.toString(props.getMaxSupportRequestsInHour())));
                }

                timestamps.add(now);
            }

            return Mono.just(true);
        });
    }


    public void reset() {
        this.requestTimestampsRateLimiterCache.clear();
    }
}

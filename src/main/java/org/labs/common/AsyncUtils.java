package org.labs.common;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class AsyncUtils {

    public static void waitMillis(long maxMillis) {
        if (maxMillis <= 0) {
            return;
        }
        try {
            long delay = ThreadLocalRandom.current().nextLong(maxMillis + 1);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            log.warn("Поток {} был прерван", Thread.currentThread().getName());
            Thread.currentThread().interrupt();
        }
    }
}

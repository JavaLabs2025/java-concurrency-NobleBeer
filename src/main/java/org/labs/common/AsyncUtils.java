package org.labs.common;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public class AsyncUtils {

    public static void waitMillis(long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.warn("Поток {} был прерван", Thread.currentThread().getName());
            Thread.currentThread().interrupt();
        }
    }

    public static void stopThreads(Thread[] threads) {
        Arrays.stream(threads)
                .filter(t -> !t.isInterrupted())
                .forEach(Thread::interrupt);

        waitMillis(100);
    }
}

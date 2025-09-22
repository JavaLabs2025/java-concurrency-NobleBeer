package org.labs.serverequest.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;

@Getter
@RequiredArgsConstructor
public class ServeRequest implements Comparable<ServeRequest> {
    private final int developerId;
    private final long timestamp = System.nanoTime();
    private final CompletableFuture<Boolean> served = new CompletableFuture<>();

    @Override
    public int compareTo(ServeRequest other) {
        return Long.compare(this.timestamp, other.timestamp);
    }
}

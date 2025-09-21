package org.labs.serverequest.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;

@Getter
@RequiredArgsConstructor
public class ServeRequest {
    private final int developerId;
    private final CompletableFuture<Boolean> served = new CompletableFuture<>();
}

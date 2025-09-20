package org.labs.serverequest.model;

import java.util.concurrent.CompletableFuture;

public record ServeRequest(int developerId, CompletableFuture<Boolean> served) {
}

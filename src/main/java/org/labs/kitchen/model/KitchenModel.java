package org.labs.kitchen.model;

import lombok.RequiredArgsConstructor;
import org.labs.serverequest.model.ServeRequest;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public class KitchenModel {

    private final AtomicInteger remainingDishCount;
    private final BlockingQueue<ServeRequest> requestQueue = new PriorityBlockingQueue<>();

    public void submitRequest(ServeRequest req) throws InterruptedException {
        requestQueue.put(req);
    }

    public ServeRequest takeRequest() throws InterruptedException {
        return requestQueue.take();
    }

    public boolean takeDishIfAvailable() {
        int currentRemainingDishCount;
        do {
            currentRemainingDishCount = remainingDishCount.get();
            if (currentRemainingDishCount <= 0) return false;
        } while (!remainingDishCount.compareAndSet(currentRemainingDishCount, currentRemainingDishCount - 1));
        return true;
    }

    public int getRemainingDishCount() {
        return remainingDishCount.get();
    }

    public boolean isDepleted() {
        return remainingDishCount.get() <= 0;
    }

    public boolean isRequestQueueEmpty() {
        return requestQueue.isEmpty();
    }

}

package org.labs.kitchen.model;

import org.labs.serverequest.model.ServeRequest;
import org.labs.waiter.model.WaiterModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class KitchenModel {

    private final AtomicInteger remainingDishCount;
    private final BlockingQueue<ServeRequest> requestQueue = new LinkedBlockingQueue<>();
    private final List<Thread> waiterThreads = new ArrayList<>();

    public KitchenModel(int initialDishes, int waiterCount) {
        this.remainingDishCount = new AtomicInteger(initialDishes);

        IntStream.range(0, waiterCount)
                .mapToObj(number -> new Thread(new WaiterModel(number, this), "waiter-" + (number + 1)))
                .peek(Thread::start)
                .forEach(waiterThreads::add);
    }

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

    public void stopWaiters() {
        for (Thread waiterThread : waiterThreads) waiterThread.interrupt();
    }
}

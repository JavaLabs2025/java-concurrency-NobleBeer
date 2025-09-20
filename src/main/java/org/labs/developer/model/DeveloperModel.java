package org.labs.developer.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.labs.common.MathUtils;
import org.labs.fork.model.ForkModel;
import org.labs.kitchen.model.KitchenModel;
import org.labs.serverequest.model.ServeRequest;
import org.labs.state.model.StateModel;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Getter
@RequiredArgsConstructor
public class DeveloperModel implements Runnable {

    private final int id;
    private final ForkModel leftFork;
    private final ForkModel rightFork;
    private final StateModel state;
    private final KitchenModel kitchen;

    public final AtomicBoolean isStopped = new AtomicBoolean();

    public AtomicInteger eatCount = new AtomicInteger();

    @Override
    public void run() {
        try {
            while (!isStopped.get()) {
                think();
                boolean served = callWaiter();
                if (!served) break;

                state.takeForks(id, leftFork, rightFork);
                eat();
                state.putForks(id, leftFork, rightFork);
            }
        } catch (InterruptedException ignored) {
            log.warn("Поток {} был прерван", Thread.currentThread().getName());
        }
    }

    public void stop() {
        isStopped.set(true);
    }

    private void think() throws InterruptedException {
        log.debug("Время: {} ms. Разработчик начал обсуждать преподавателей", System.currentTimeMillis());
        Thread.sleep(MathUtils.getRandomInt());
    }

    private void eat() throws InterruptedException {
        log.debug("Время: {} ms. Разработчик начал есть", System.currentTimeMillis());
        Thread.sleep(MathUtils.getRandomInt());
        eatCount.incrementAndGet();
    }

    private boolean callWaiter() throws InterruptedException {
        CompletableFuture<Boolean> servedFuture = new CompletableFuture<>();
        var serveRequest = new ServeRequest(id, servedFuture);
        kitchen.submitRequest(serveRequest);

        boolean served;
        try {
            served = servedFuture.get();
        } catch (ExecutionException e) {
            log.warn("Разработчик {} не смог вызвать официанта", id, e);
            return false;
        }

        if (!served) {
            log.info("Разработчик {} не может получить новую порцию. Прием пищи прекращен", id);
            isStopped.set(true);
            return false;
        }
        return true;
    }
}

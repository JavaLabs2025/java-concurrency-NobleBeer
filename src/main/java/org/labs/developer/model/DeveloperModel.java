package org.labs.developer.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.labs.common.MathUtils;
import org.labs.spoon.model.SpoonModel;
import org.labs.kitchen.model.KitchenModel;
import org.labs.serverequest.model.ServeRequest;
import org.labs.state.model.StateModel;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Getter
@RequiredArgsConstructor
public class DeveloperModel implements Runnable {

    private final int id;
    private final SpoonModel leftSpoon;
    private final SpoonModel rightSpoon;
    private final StateModel state;
    private final KitchenModel kitchen;

    public final AtomicBoolean isStopped = new AtomicBoolean();

    public AtomicInteger eatCount = new AtomicInteger();

    @Override
    public void run() {
        try {
            while (!isStopped.get()) {
                think();
                var served = placeOrder();
                if (!served) {
                    isStopped.set(true);
                    break;
                }

                state.takeSpoons(id, leftSpoon, rightSpoon);
                eat();
                state.putSpoons(id, leftSpoon, rightSpoon);
            }
        } catch (InterruptedException ignored) {
            log.warn("Поток {} был прерван", Thread.currentThread().getName());
        }
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

    private boolean placeOrder() throws InterruptedException {
        try {
            var serveRequest = new ServeRequest(id);
            kitchen.submitRequest(serveRequest);
            var isServed = serveRequest.getServed().get();
            if (!isServed) {
                log.info("Разработчик {} не может получить новую порцию. Его обед завершен", id + 1);
                return false;
            }
        } catch (ExecutionException e) {
            log.warn("Разработчик {} не смог вызвать официанта", id + 1, e);
            return false;
        }
        return true;
    }
}

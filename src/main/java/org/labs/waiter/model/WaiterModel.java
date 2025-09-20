package org.labs.waiter.model;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.labs.kitchen.model.KitchenModel;

@Slf4j
@RequiredArgsConstructor
public class WaiterModel implements Runnable {

    private final int id;
    private final KitchenModel kitchen;

    @Override
    public void run() {
        try {
            while (true) {
                var serveRequest = kitchen.takeRequest();
                if (serveRequest == null) {
                    continue;
                }

                var isServed = kitchen.takeDishIfAvailable();
                log.debug("Официант {} начал обслуживание разработчика {} -> {}", id + 1, serveRequest.developerId() + 1, isServed);
                serveRequest.served().complete(isServed);

                if (!isServed && kitchen.isDepleted() && kitchen.isRequestQueueEmpty()) {
                    log.info("Официант {} разнес все блюда, заказы закончились", id + 1);
                    break;
                }
            }
        } catch (InterruptedException e) {
            log.warn("Поток {} был прерван", Thread.currentThread().getName());
            Thread.currentThread().interrupt();
        }
    }
}

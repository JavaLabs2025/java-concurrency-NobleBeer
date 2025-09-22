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
            while (!(kitchen.isDepleted() && kitchen.isRequestQueueEmpty())) {
                var serveRequest = kitchen.takeRequest();
                if (serveRequest == null) {
                    continue;
                }

                var isServed = kitchen.takeDishIfAvailable();
                log.debug("Официант {} начал обслуживание разработчика {} -> {}",
                        id + 1, serveRequest.getDeveloperId() + 1, isServed);

                serveRequest.getServed().complete(isServed);
            }

            log.info("Официант {} разнес все блюда, заказы закончились", id + 1);
        } catch (InterruptedException e) {
            log.warn("Поток {} был прерван", Thread.currentThread().getName());
            Thread.currentThread().interrupt();
        }
    }
}

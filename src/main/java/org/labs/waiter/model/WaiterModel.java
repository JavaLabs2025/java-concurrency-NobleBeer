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

                var served = kitchen.serveDish();
                log.debug("Официант {} начал обслуживание разработчика {} -> {}", id + 1, serveRequest.developerId(), served);
                serveRequest.served().complete(served);

                if (!served && kitchen.isDepleted() && kitchen.isRequestQueueEmpty()) {
                    log.info("Официант {} разнес все блюда или заказы закончились", id + 1);
                    break;
                }
            }
        } catch (InterruptedException e) {
            log.warn("Поток {} был прерван", Thread.currentThread().getName());
            Thread.currentThread().interrupt();
        }
    }
}

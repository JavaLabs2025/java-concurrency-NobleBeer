package org.labs;

import lombok.extern.slf4j.Slf4j;
import org.labs.common.AsyncUtils;
import org.labs.config.Config;
import org.labs.developer.model.DeveloperModel;
import org.labs.spoon.model.SpoonModel;
import org.labs.kitchen.model.KitchenModel;
import org.labs.state.model.StateModel;
import org.labs.waiter.model.WaiterModel;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Slf4j
public class DinnerProcessingService {

    private static final int TARGET_RESOURCE_COUNT = Config.DEVELOPER_COUNT;
    private static final int DISH_COUNT = Config.DISH_COUNT;
    private static final int WAITER_COUNT = Config.WAITER_COUNT;

    private final ExecutorService developerPool = Executors.newWorkStealingPool(TARGET_RESOURCE_COUNT);
    private final ExecutorService waiterPool = Executors.newWorkStealingPool(WAITER_COUNT);

    public DeveloperModel[] runDinner() {
        var startDate = System.currentTimeMillis();
        log.info("Время {} мс. Обед начался", startDate);

        var kitchen = new KitchenModel(new AtomicInteger(DISH_COUNT));
        var spoons = createSpoons();
        var developers = createDevelopers(spoons, kitchen);
        var waiters = createWaiters(kitchen);

        startDeveloperTasks(developers);
        startWaiterTasks(waiters);

        monitorDinner(kitchen);

        stopDinner();

        log.info("Время выполнения: {} мс. Обед завершен", System.currentTimeMillis() - startDate);

        return developers;
    }

    private SpoonModel[] createSpoons() {
        return IntStream.range(0, TARGET_RESOURCE_COUNT)
                .mapToObj(SpoonModel::new)
                .toArray(SpoonModel[]::new);
    }

    private DeveloperModel[] createDevelopers(SpoonModel[] spoons, KitchenModel kitchen) {
        var developers = new DeveloperModel[TARGET_RESOURCE_COUNT];
        var state = new StateModel(developers);

        IntStream.range(0, developers.length).forEach(number -> {
            var leftSpoon = spoons[number];
            var rightSpoon = spoons[(number + 1) % spoons.length];
            developers[number] = new DeveloperModel(number, leftSpoon, rightSpoon, state, kitchen);
        });
        return developers;
    }

    private WaiterModel[] createWaiters(KitchenModel kitchen) {
        var waiters = new WaiterModel[WAITER_COUNT];
        IntStream.range(0, WAITER_COUNT).forEach(number -> waiters[number] = new WaiterModel(number, kitchen));
        return waiters;
    }

    private void startDeveloperTasks(DeveloperModel[] developers) {
        Arrays.stream(developers).forEach(developerPool::submit);
    }

    private void startWaiterTasks(WaiterModel[] waiters) {
        Arrays.stream(waiters).forEach(waiterPool::submit);
    }

    private void monitorDinner(KitchenModel kitchen) {
        while (kitchen.getRemainingDishCount() > 0) {
            AsyncUtils.waitMillis(200);
            log.debug("Оставшееся количество блюд на кухне {}", kitchen.getRemainingDishCount());
        }
    }

    private void stopDinner() {
        AsyncUtils.waitMillis(Config.DINNER_DURATION_IN_MS);
        shutdownAndAwaitTermination(developerPool);
        shutdownAndAwaitTermination(waiterPool);
    }

    private void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

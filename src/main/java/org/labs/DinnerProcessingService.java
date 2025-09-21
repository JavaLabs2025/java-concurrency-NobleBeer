package org.labs;

import lombok.extern.slf4j.Slf4j;
import org.labs.common.AsyncUtils;
import org.labs.config.Config;
import org.labs.developer.model.DeveloperModel;
import org.labs.fork.model.ForkModel;
import org.labs.kitchen.model.KitchenModel;
import org.labs.state.model.StateModel;
import org.labs.waiter.model.WaiterModel;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Slf4j
public class DinnerProcessingService {

    private static final int TARGET_RESOURCE_COUNT = Config.DEVELOPER_COUNT;
    private static final Thread[] DEVELOPER_THREADS = new Thread[TARGET_RESOURCE_COUNT];
    private static final int DISH_COUNT = Config.DISH_COUNT;
    private static final int WAITER_COUNT = Config.WAITER_COUNT;
    private static final Thread[] WAITER_THREADS = new Thread[WAITER_COUNT];

    public DeveloperModel[] runDinner() {
        var startDate = System.currentTimeMillis();
        log.info("Время {} мс. Обед начался", startDate);

        var kitchen = new KitchenModel(new AtomicInteger(DISH_COUNT));
        var forks = createForks();
        var developers = createDevelopers(forks, kitchen);
        var waiters = createWaiters(kitchen);

        startDeveloperThreads(developers);
        startWaiterThreads(waiters);

        monitorDinner(kitchen);

        stopDinner();

        log.info("Время выполнения: {} мс. Обед завершен", System.currentTimeMillis() - startDate);

        return developers;
    }

    private ForkModel[] createForks() {
        return IntStream.range(0, TARGET_RESOURCE_COUNT)
                .mapToObj(ForkModel::new)
                .toArray(ForkModel[]::new);
    }

    private DeveloperModel[] createDevelopers(ForkModel[] forks, KitchenModel kitchen) {
        var developers = new DeveloperModel[TARGET_RESOURCE_COUNT];
        var state = new StateModel(developers);

        IntStream.range(0, developers.length).forEach(number -> {
            var leftFork = forks[number];
            var rightFork = forks[(number + 1) % forks.length];
            developers[number] = new DeveloperModel(number, leftFork, rightFork, state, kitchen);
        });
        return developers;
    }

    private WaiterModel[] createWaiters(KitchenModel kitchen) {
        var waiters = new WaiterModel[WAITER_COUNT];
        IntStream.range(0, WAITER_COUNT).forEach(number -> {
            waiters[number] = new WaiterModel(number, kitchen);
        });
        return waiters;
    }

    private void startWaiterThreads(WaiterModel[] waiters) {
        IntStream.range(0, waiters.length).forEach(number -> {
            WAITER_THREADS[number] = new Thread(waiters[number], "waiter-" + (number + 1));
            WAITER_THREADS[number].start();
        });
    }

    private void startDeveloperThreads(DeveloperModel[] developers) {
        IntStream.range(0, developers.length).forEach(number -> {
            DEVELOPER_THREADS[number] = new Thread(developers[number], "developer-" + (number + 1));
            DEVELOPER_THREADS[number].start();
        });
    }

    private void monitorDinner(KitchenModel kitchen) {
        while (kitchen.getRemainingDishCount() > 0) {
            AsyncUtils.waitMillis(200);
            log.debug("Оставшееся количество блюд на кухне {}", kitchen.getRemainingDishCount());
        }
    }

    private void stopDinner() {
        AsyncUtils.waitMillis(Config.DINNER_DURATION_IN_MS);
        AsyncUtils.stopThreads(WAITER_THREADS);
        AsyncUtils.stopThreads(DEVELOPER_THREADS);
    }
}

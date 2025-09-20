package org.labs;

import lombok.extern.slf4j.Slf4j;
import org.labs.common.AsyncUtils;
import org.labs.common.MathUtils;
import org.labs.config.Config;
import org.labs.developer.model.DeveloperModel;
import org.labs.fork.model.ForkModel;
import org.labs.kitchen.model.KitchenModel;
import org.labs.state.model.StateModel;

import java.util.Arrays;
import java.util.stream.IntStream;

@Slf4j
public class Main {

    private static final int TARGET_RESOURCE_COUNT = Config.DEVELOPER_COUNT;
    private static final Thread[] THREADS = new Thread[TARGET_RESOURCE_COUNT];
    private static final int DISH_COUNT = Config.DISH_COUNT;
    private static final int WAITER_COUNT = Config.WAITER_COUNT;

    public static void main(String[] args) {
        log.info("Время {} mc. Обед начался", System.currentTimeMillis());

        var forks = IntStream.range(0, TARGET_RESOURCE_COUNT)
                .mapToObj(ForkModel::new)
                .toArray(ForkModel[]::new);

        var developers = new DeveloperModel[TARGET_RESOURCE_COUNT];
        var state = new StateModel(developers);
        var kitchen = new KitchenModel(DISH_COUNT, WAITER_COUNT);

        IntStream.range(0, TARGET_RESOURCE_COUNT).forEach(number -> {
            var leftFork = forks[number];
            var rightFork = forks[(number + 1) % forks.length];

            developers[number] = new DeveloperModel(number, leftFork, rightFork, state, kitchen);

            THREADS[number] = new Thread(developers[number], "developer-" + (number + 1));
            THREADS[number].start();
        });

        while (kitchen.getRemainingDishCount() > 0) {
            AsyncUtils.waitMillis(200);
            log.debug("Оставшееся количество блюд на кухне {}", kitchen.getRemainingDishCount());
        }

        AsyncUtils.waitMillis(Config.DINNER_DURATION_IN_MS);
        stopDevelopers(developers);
        kitchen.stopWaiters();
        AsyncUtils.stopThreads(THREADS);

        log.info("Время {} mc. Обед завершен", System.currentTimeMillis());
        printStates(developers);
    }

    private static void stopDevelopers(DeveloperModel[] developers) {
        Arrays.stream(developers)
                .filter(d -> !d.getIsStopped().get())
                .forEach(DeveloperModel::stop);

        AsyncUtils.waitMillis(200);
    }

    private static void printStates(DeveloperModel[] developers) {
        int totalCount = Arrays.stream(developers)
                .mapToInt(developer -> developer.getEatCount().intValue())
                .sum();

        if (totalCount > 0) {
            log.info("Итоговое состояние:");
            log.info("Всего съедено: {}", totalCount);

            IntStream.range(0, developers.length)
                    .forEachOrdered(i -> {
                        double value = 100.0 * developers[i].getEatCount().intValue() / totalCount;
                        log.info("Разработчик {} съел {}% блюд", i + 1, MathUtils.roundTo2Digits(value));
                    });
        }
    }
}
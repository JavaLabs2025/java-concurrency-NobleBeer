package org.labs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.labs.common.MathUtils;
import org.labs.developer.model.DeveloperModel;

import java.util.Arrays;
import java.util.stream.IntStream;

@Slf4j
@RequiredArgsConstructor
public class Main {

    private static final DinnerProcessingService dinnerProcessingService = new DinnerProcessingService();

    public static void main(String[] args) {
        var developers = dinnerProcessingService.runDinner();
        printStates(developers);
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
                        var value = 100.0 * developers[i].getEatCount().intValue() / totalCount;
                        log.info("Разработчик {} съел {}% блюд", i + 1, MathUtils.roundTo2Digits(value));
                    });
        }
    }
}
package org.labs.common;

import org.labs.config.Config;

public class MathUtils {

    public static double roundTo2Digits(double value) {
        return Math.round(value * 100) / 100.0;
    }

    public static int getRandomInt() {
        return (int) (Math.random() * Config.MAX_WAIT_MS);
    }
}

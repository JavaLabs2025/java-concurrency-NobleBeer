package org.labs.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Config {
    public static int DEVELOPER_COUNT = 7;
    public static int MAX_WAIT_MS = 1;
    public static int DINNER_DURATION_IN_MS = 10;
    public static int DISH_COUNT = 1000000;
    public static int WAITER_COUNT = 2;
}

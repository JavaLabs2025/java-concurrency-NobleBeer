package org.labs.state.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public enum State {
    HUNGRY,
    EATING,
    DISCUSS_TEACHERS
}

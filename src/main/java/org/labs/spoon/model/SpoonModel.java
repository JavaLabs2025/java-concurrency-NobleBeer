package org.labs.spoon.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


@Getter
@RequiredArgsConstructor
public class SpoonModel {
    private final int id;
    @Setter
    private boolean isAvailable = true;
}

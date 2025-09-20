package org.labs.fork.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


@Getter
@RequiredArgsConstructor
public class ForkModel {
    private final int id;
    @Setter
    private boolean isAvailable = true;
}

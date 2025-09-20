package org.labs.fork.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@RequiredArgsConstructor
public class ForkModel {
    private final int id;
    private boolean isAvailable = true;
}

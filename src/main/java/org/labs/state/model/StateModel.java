package org.labs.state.model;

import lombok.extern.slf4j.Slf4j;
import org.labs.developer.model.DeveloperModel;
import org.labs.fork.model.ForkModel;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class StateModel {

    private final int developerCount;
    private final DeveloperModel[] developers;
    private final State[] state;
    private final Lock lock;
    private final Condition[] conditions;

    public StateModel(DeveloperModel[] developers) {
        this.developers = developers;
        this.developerCount = developers.length;
        lock = new ReentrantLock();
        state = new State[developerCount];
        conditions = new Condition[developerCount];
        IntStream.range(0, developerCount).forEach(i -> {
            state[i] = State.DISCUSS_TEACHERS;
            conditions[i] = lock.newCondition();
        });
    }

    public void takeForks(int id, ForkModel leftFork, ForkModel rightFork) {
        lock.lock();
        try {
            updateDeveloperState(id, State.HUNGRY);

            while (!developers[id].getIsStopped().get() && (!leftFork.isAvailable() || !rightFork.isAvailable())) {
                conditions[id].await();
            }

            leftFork.setAvailable(false);
            rightFork.setAvailable(false);

            updateDeveloperState(id, State.EATING);

            printState();
        } catch (InterruptedException e) {
            log.warn("Поток {} был прерван", Thread.currentThread().getName());
        } finally {
            lock.unlock();
        }
    }


    public void putForks(int developerId, ForkModel leftFork, ForkModel rightFork) {
        lock.lock();
        try {
            updateDeveloperState(developerId, State.DISCUSS_TEACHERS);

            leftFork.setAvailable(true);
            rightFork.setAvailable(true);

            conditions[(developerId + 1) % developerCount].signalAll();
            conditions[(developerId + developerCount - 1) % developerCount].signalAll();

            printState();
        } finally {
            lock.unlock();
        }
    }

    private void updateDeveloperState(int id, State state) {
        this.state[id] = state;
    }

    private void printState() {
        String result = IntStream.range(0, developerCount)
                .mapToObj(i -> switch (state[i]) {
                    case DISCUSS_TEACHERS -> "обсуждает преподавателей";
                    case HUNGRY   -> "голоден";
                    case EATING   -> "ест";
                })
                .collect(Collectors.joining(" ", "Сводка по занятости разработчиков: ", ""));

        log.debug("{}", result);
    }
}

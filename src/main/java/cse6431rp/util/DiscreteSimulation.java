package cse6431rp.util;

/**
 * Created by alex on 4/23/15.
 */
public class DiscreteSimulation extends Scheduler {
    private boolean running;
    protected int currentTime;

    public DiscreteSimulation() {
        newNamedCondition("time");
    }

    @Override
    protected void newProcess(final Runnable runnable) {
        super.newProcess(new Runnable() {
            @Override
            public void run() {
                await("time", new StatePredicate() {
                    @Override
                    public boolean test() {
                        return running;
                    }
                });

                runnable.run();

                await("time", new StatePredicate() {
                    @Override
                    public boolean test() {
                        return !running;
                    }
                });
            }
        });
    }

    @Override
    protected void stop() {
        lock.lock();
        try {
            running = false;
            super.stop();
        } finally {
            lock.unlock();
        }
    }

    protected void sleep(int delay) {
        sleepUntil(currentTime + delay);
    }

    protected void sleepUntil(final int time) {
        lock.lock();
        try {
            await("time", new StatePredicate() {
                @Override
                public boolean test() {
                    return currentTime == time;
                }
            });
        } finally {
            lock.unlock();
        }
    }

    protected void start(int time) {
        lock.lock();

        try {
            currentTime = time;
            running = true;
            signalAll("time");
        } finally {
            lock.unlock();
        }
    }

    protected void sync() {
        waitForDeadlock();
    }

    protected void step() {
        lock.lock();

        try {
            sync();
            currentTime += 1;
            signalAll("time");
            sync();
        } finally {
            lock.unlock();
        }
    }
}

package cse6431rp.util;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class Scheduler {
    protected ReentrantLock lock;
    private Condition reschedule;
    private HashMap<Long, Process> processes;
    private HashMap<String, Condition> namedConditions;

    public Scheduler() {
        lock = new ReentrantLock();
        reschedule = lock.newCondition();
        processes = new HashMap<>();
        namedConditions = new HashMap<>();
    }

    /**
     * Checks whether the current state is deadlocked.
     * @return
     */
    private boolean isDeadlocked() {
        for (Process process : processes.values()) {
            if (process.waitPredicate == null) {
                return false;
            }

            if (process.waitPredicate.test()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Waits until the current state is deadlocked.
     */
    protected void waitForDeadlock() {
        lock.lock();

        try {
            while (!isDeadlocked()) {
                reschedule.await();

//                System.out.println("isDeadlocked = " + isDeadlocked());
//                for (Process process : processes.values()) {
//                    if (process.waitPredicate == null) {
//                        System.out.println("process#" + process.thread.getId() + " is not waiting");
//                    } else {
//                        System.out.println("process#" + process.thread.getId() + " waits on " + process.waitGroup + " (" + process.waitPredicate.test() + ")");
//                    }
//                }
            }
        } catch (InterruptedException e) {
            Unchecked.unchecked(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Spawns a new thread controlled by the scheduler.
     * @param runnable
     */
    protected void newProcess(final Runnable runnable) {
        lock.lock();

        try {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Unchecked.<InterruptedException>checked();
                        runnable.run();
                    } catch (InterruptedException ignored) { }
                }
            });


            processes.put(thread.getId(), new Process(thread));
            thread.start();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Creates a new named conditional variable.
     * @param name
     */
    protected void newNamedCondition(String name) {
        namedConditions.put(name, lock.newCondition());
    }

    /**
     * Stops all threads from executing.
     */
    protected void stop() {
        for (Process p : processes.values()) {
            p.thread.interrupt();
        }

        processes.clear();
        reschedule.signal();
    }

    /**
     * Waits on the named condition variable.
     * @param conditionName
     * @param predicate
     */
    protected void await(final String conditionName, final StatePredicate predicate) {
        lock.lock();

        try {
            if (!predicate.test()) {
                final Process self = processes.get(Thread.currentThread().getId());

                self.waitPredicate = predicate;
                self.waitGroup = conditionName;

                reschedule.signal();

                while (!predicate.test()) {
                    namedConditions.get(conditionName).await();
                }

                self.waitPredicate = null;
                self.waitGroup = null;
            }
        } catch (InterruptedException e) {
            Unchecked.unchecked(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Signals the named condition variable waking up one waiter.
     * @param conditionName
     */
    protected void signal(final String conditionName) {
        lock.lock();

        try {
            namedConditions.get(conditionName).signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Signals the named condition variable waking up multiple waiters.
     * @param conditionName
     */
    protected void signalAll(final String conditionName) {
        lock.lock();

        try {
            namedConditions.get(conditionName).signalAll();
        } finally {
            lock.unlock();
        }
    }

    private static class Process {
        public Thread thread;
        public StatePredicate waitPredicate;
        public String waitGroup;

        public Process(Thread thread) {
            this.thread = thread;
        }
    }
}

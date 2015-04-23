package cse6431rp;

import cse6431rp.util.DiscreteSimulation;
import cse6431rp.util.StatePredicate;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

/**
 * Created by alex on 4/23/15.
 */
class RestaurantSimulation extends DiscreteSimulation {
    private final Config config;

    private int availableTables;
    private Queue<Order> incomingOrders;
    private boolean availableMachines[];
    private int totalDinersServed;

    private int enterRestaurant() {
        lock.lock();
        try {
            await("tables", new StatePredicate() {
                @Override
                public boolean test() {
                    return availableTables > 0;
                }
            });

            int table = availableTables;
            availableTables--;
            return table;
        } finally {
            lock.unlock();
        }
    }

    private void orderFood(Order order) {
        lock.lock();
        try {
            incomingOrders.add(order);
            signal("orders-in");
        } finally {
            lock.unlock();
        }
    }

    private void waitForFood(final Order order) {
        lock.lock();
        try {
            await("orders-out", new StatePredicate() {
                @Override
                public boolean test() {
                    return order.completed();
                }
            });
        } finally {
            lock.unlock();
        }
    }

    private void leaveRestaurant() {
        lock.lock();
        try {
            assert availableTables < config.tableCount;
            availableTables++;
            totalDinersServed++;
            signal("tables");
        } finally {
            lock.unlock();
        }
    }

    private void dinerProcess(int id) {
        lock.lock();
        try {
            sleepUntil(config.diners[id].arrivalTime);

            int table = enterRestaurant();
            report("Diner#" + id + " was seated at the table#" + table + " at " + currentTime);

            Order myOrder = new Order(id, config.diners[id].order);
            orderFood(myOrder);

            waitForFood(myOrder);
            report("Diner#" + id + " started eating at " + currentTime);

            sleep(30);

            leaveRestaurant();
            report("Diner#" + id + " left at " + currentTime);
        } finally {
            lock.unlock();
        }
    }

    private Order waitForOrder() {
        lock.lock();
        try {
            await("orders-in", new StatePredicate() {
                @Override
                public boolean test() {
                    return incomingOrders.size() > 0;
                }
            });

            Order result = incomingOrders.poll();
            if (incomingOrders.size() > 0) signal("orders-in");
            return result;
        } finally {
            lock.unlock();
        }
    }

    private void cookFood(int id, final Order order) {
        lock.lock();
        try {
            while (!order.completed()) {
                await("machines", new StatePredicate() {
                    @Override
                    public boolean test() {
                        for (int i = 0; i < config.foodTypes; i++) {
                            if (order.required[i] > 0 && availableMachines[i]) return true;
                        }
                        return false;
                    }
                });

                int machine = -1;
                for (int i = 0; i < config.foodTypes; i++) {
                    if (order.required[i] > 0 && availableMachines[i]) {
                        machine = i;
                    }
                }
                assert machine != -1;

                report("Cook#" + id + " started cooking food#" + machine + " for diner#" + order.diner + " at " + currentTime);
                availableMachines[machine] = false;
                sleep(config.cookTime[machine]);

                report("Cook#" + id + " finished cooking food#" + machine + " for diner#" + order.diner + " at " + currentTime);
                order.required[machine]--;
                availableMachines[machine] = true;
                signalAll("machines");
            }

            signalAll("orders-out");
        } finally {
            lock.unlock();
        }
    }

    private void cookProcess(int id) {
        lock.lock();
        try {
            while (true) {
                Order order = waitForOrder();
                report("Cook#" + id + " started cooking for diner#" + order.diner + " at " + currentTime);
                cookFood(id, order);
                report("Cook#" + id + " finished cooking for diner#" + order.diner + " at " + currentTime);
            }
        } finally {
            lock.unlock();
        }
    }

    public RestaurantSimulation(Config config) {
        super();

        this.config = config;

        newNamedCondition("tables");
        newNamedCondition("orders-in");
        newNamedCondition("orders-out");
        newNamedCondition("machines");

        availableTables = config.tableCount;
        totalDinersServed = 0;
        availableMachines = new boolean[config.foodTypes];
        Arrays.fill(availableMachines, true);
        incomingOrders = new ArrayDeque<>();

        for (int i = 0; i < config.dinerCount; i++) {
            final int id = i;
            newProcess(new Runnable() {
                @Override
                public void run() {
                    dinerProcess(id);
                }
            });
        }

        for (int i = 0; i < config.cookCount; i++) {
            final int id = i;
            newProcess(new Runnable() {
                @Override
                public void run() {
                    cookProcess(id);
                }
            });
        }
    }

    public void run() {
        start(config.minArrivalTime);
        while (totalDinersServed < config.dinerCount) step();
        stop();
    }

    private void report(String s) {
        System.out.println(s);
    }

    private class Order {
        public int diner;
        public int required[];

        public boolean completed() {
            boolean result = true;
            for (int r : required) {
                if (r > 0) {
                    result = false;
                    break;
                }
            }
            return result;
        }

        public Order(int diner, int required[]) {
            this.diner = diner;
            this.required = required.clone();
        }
    }
}

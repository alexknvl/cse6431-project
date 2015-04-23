# CSE 6431 Restaurant Project

## Scheduling
I decided to use a slightly unconventional design for my solution. All of my threads are controlled by one `Scheduler` (and its subclass `DiscreteSimulation`) that takes care of synchronization, deadlocks and progression. 

Whenever current time is updated, this causes a number of other state updates (diners come in, compete for the tables and order food; cooks take orders and cook food using machines). At some point these update fully propagate through the system and threads deadlock; they won't be able to proceed without another time update.

`Scheduler` keeps a predicate function for each waiting thread that tells it whether this thread is supposed to wake up. Once all predicates evaluate to false, threads are deadlocked. `waitForDeadlock` inside the `Scheduler` class allows its caller to wait until all scheduled threads are deadlocked.

In addition, `Scheduler` keeps a list of named condition variables. Whenever a thread wants to wait for some event, it has to provide both a predicate that tells the scheduler when this thread is supposed to wake up and a condition variable name on which it should wait.

`DiscreteSimulation` inherits from `Scheduler` and augments its behaviour to support discrete time simulation. It provides a function called `step` which waits until all threads are deadlocked (`sync`), then updates the time and signals all threads waiting on a named condition variable "time" to wake up.

## `RestaurantSimulation`
This is a class containing the actual logic. It spawns threads for cooks (`cookProcess`) and diners (`dinerProcess`) and then runs the whole system using `step` from `DiscreteSimulation`.

## Main & Config
The `Config` class is a very simple configuration file parser. `Main` assumes that the program is run with one argument, a path to its configuration file.

## Disclaimer
I looked up how to subvert checked exceptions in Java on the internet (@see `Unchecked` class). This was not in any way related to the problem at hand and probably not a good coding style, but checked exceptions are too annoying in my personal opinion.
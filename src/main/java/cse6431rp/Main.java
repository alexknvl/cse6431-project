package cse6431rp;

public final class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("No config file provided.");
            System.exit(-1);
        }

        Config config = Config.readFrom(args[0]);

        if (config == null) {
            System.err.println("Invalid config file.");
            System.exit(-1);
        }

        RestaurantSimulation simulation = new RestaurantSimulation(config);
        simulation.run();
    }
}
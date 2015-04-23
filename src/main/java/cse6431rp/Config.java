package cse6431rp;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.Scanner;

/**
 * Created by alex on 4/23/15.
 */
class Config {
    public static class Diner {
        public final int id;
        public final int arrivalTime;
        public final int order[];

        public Diner(int id, int arrivalTime, int order[]) {
            this.id = id;
            this.arrivalTime = arrivalTime;
            this.order = order;
        }
    }

    public final int minArrivalTime;
    public final int maxArrivalTime;
    public final int foodTypes;
    public final int cookTime[];
    public final int minFoodOrder[];
    public final int maxFoodOrder[];
    public final int dinerCount;
    public final int tableCount;
    public final int cookCount;
    public final Diner diners[];

    public Config(Scanner scanner) {
        minArrivalTime = 0;
        maxArrivalTime = 120;

        foodTypes = 3;
        minFoodOrder = new int[]{1, 0, 0};
        maxFoodOrder = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE, 1};
        cookTime = new int[]{5, 3, 1};

        dinerCount = scanner.nextInt();
        tableCount = scanner.nextInt();
        cookCount = scanner.nextInt();

        assert 1 <= tableCount;
        assert 1 <= cookCount;

        diners = new Diner[dinerCount];

        for (int i = 0; i < dinerCount; i++) {
            final int arrivalTime = scanner.nextInt();
            assert minArrivalTime <= arrivalTime && arrivalTime <= maxArrivalTime;

            final int order[] = new int[foodTypes];
            for (int j = 0; j < foodTypes; j++) {
                order[j] = scanner.nextInt();

                assert minFoodOrder[j] <= order[j] && order[j] <= maxFoodOrder[j];
            }

            diners[i] = new Diner(i, arrivalTime, order);
        }
    }

    public static Config readFrom(String path) {
        Scanner scanner = null;
        try {
            scanner = new Scanner(new BufferedInputStream(
                    new FileInputStream(path)));
            return new Config(scanner);
        } catch (Exception re) {
            return null;
        } finally {
            if (scanner != null) scanner.close();
        }
    }
}

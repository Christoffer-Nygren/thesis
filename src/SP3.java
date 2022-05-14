import java.security.SecureRandom;
import java.util.stream.IntStream;

public class SP3 {
    private static final int[] l = {11000,22000,33000, 44000, 55000, 66000, 77000, 88000, 99000, 110000};
    private static final int[][] x = {{367, 211, 211, 211}, {421, 579, 579, 421}, {828, 828, 672, 672},
            {923, 1077, 923, 1077}, {1327, 1327, 1173, 1173}, {1577, 1423, 1423, 1577}, { 1673, 1827, 1827, 1673},
            {1923, 2077, 1923, 2077}, {2327, 2173, 2173, 2327}, {2577, 2423, 2423, 2577}};
    private static final int[][] y = {{0, 10000, 0}, {0, 20000, 0}, {0, 30000, 0}, {40000, 0, 0}, {0, 50000, 0},
            {0, 60000, 0}, {0, 70000, 0}, {0, 80000, 0}, {90000, 0, 0}, {59645, 40355, 0}};
    private static final double d = 2.4;
    private static final int generations = 50000;
    private static final int poolSize = 100;
    private static final int selectionSize = 8;
    private static final int mutationRate = 8;
    private static final SecureRandom rn = new SecureRandom(new byte[]{0, 0, 1, 1, 2, 2, 3, 3});

    public static void main(String[] args) {
        int currentGen = 0;
        Chromosome3[] pool;
        Chromosome3[] topOnes = new Chromosome3[generations];
        for (int i = 0; i < l.length; i++) {
            pool = generatePool(i);
            topOnes[0] = bestGenerationalFitness(pool);
            System.out.println(printC(topOnes[0]));
            while (currentGen < generations) {
                pool = progressiveGeneration(pool);
                currentGen++;
            }
        }

    }

    private static Chromosome3[] progressiveGeneration(Chromosome3[] pool) {
        Chromosome3[] newPool = new Chromosome3[pool.length];
        for (int i = 0; i < newPool.length; i++) {
            Chromosome3[] parents = pickParents(pool);
            Chromosome3 child = crossoverParents(parents);
            while(!child.checkConstraint()) {
                parents = pickParents(pool);
                child = crossoverParents(parents);
            }
        }
        return newPool;
    }

    private static Chromosome3 crossoverParents(Chromosome3[] parents) {

    }

    private static Chromosome3[] pickParents(Chromosome3[] pool) {
        Chromosome3[] tournament = new Chromosome3[selectionSize];
        Chromosome3[] winners = new Chromosome3[2];
        for (int i = 0; i < tournament.length; i++) {
            tournament[i] = pool[rn.nextInt(pool.length)];
        }
        for (Chromosome3 c : tournament) {
            if (winners[0] == null) winners[0] = c;
            else if (c.fitnessFunction() < winners[0].fitnessFunction()) {
                winners[1] = winners[0];
                winners[0] = c;
            }
        }
        return winners;
    }

    private static String printC(Chromosome3 c) {
        StringBuilder C = new StringBuilder();
        for (int i = 0; i < c.getC().length; i++) {
            C.append("Array Part ").append(i).append(": ");
            for (int j = 0; j < c.getC()[i].length; j++) {
                C.append(c.getC()[i][j]).append(", ");
            }
        }
        return C.toString();
    }


    private static Chromosome3 bestGenerationalFitness(Chromosome3[] pool) {
        Chromosome3 best = null;
        for (Chromosome3 c: pool) {
            if (best == null) best = c;
            else if (best.fitnessFunction() > c.fitnessFunction()) best = c;
        }
        return best;
    }

    private static Chromosome3[] generatePool(int n) {
        Chromosome3[] pool = new Chromosome3[poolSize];
        for (int i = 0; i < pool.length; i++) {
            Chromosome3 c = new Chromosome3(d, l[n], x[n], y[n]);
            c.setC(fixC(c, n));
            while(!c.checkConstraint()) c.setC(fixC(c, n));
            pool[i] = c;
            System.out.println("Bop");
        }
        return pool;
    }

    private static int[][] fixC(Chromosome3 c, int n) {
        int[][] C = new int[3][4];
        for (int j = 0; j < 3; j++) {
            for (int m = 0; m < 4 ; m++) {
                if (y[n][j] == 0) C[j][m] = 0;
                else C[j][m] = rn.nextInt(y[n][j]);

            }
        }
        return C;
    }
}

class Chromosome3 {
    private int[][] C = new int[4][4];
    private final double d;
    private final int l;
    private final int[] x;
    private final int[] y;

    Chromosome3(double d, int l, int[] x, int[] y) {
        this.d = d;
        this.l = l;
        this.x = x;
        this.y = y;
    }

    public void setC (int[][] C) {
        this.C = C;
    }

    public int[][] getC () {
        return this.C;
    }

    public boolean checkConstraint(boolean first) {
        if (first) {
            if (sum(C) != l - sum(x)) return false;
        } else {
            if (sum(C) != l - sum(x)) modifySum();
        }
        if (sum(C) != sum(y)) return false;
        else return sum(C) >= 0;
    }

    private void modifySum() {
    }

    private int sum(int[] C) {
        int z = 0;
        for (int c : C) {
            z += c;
        }
        return z;
    }

    private int sum(int[][] C) {
        int z = 0;
        for (int[] c : C) {
            for (int cc : c) {
                z += cc;
            }
        }
        if (z == 10000)  {
            System.out.println("Sum" + z);
            for (int[] c : C) {
                for (int cc : c) {
                    System.out.println(cc);
                }
            }
        }

        return z;
    }

    public double fitnessFunction() {
        int Z = 0;
        for (int i = 0; i < 3; i++) {
            for (int c : C[i]) {
                Z += c * d;
            }
        }
        return Z;
    }

}

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;

public class SP3 {
    private static final int[] l = {11000,22000,33000, 44000, 55000, 66000, 77000, 88000, 99000, 110000};
    private static final int[][] x = {{367, 211, 211, 211}, {421, 579, 579, 421}, {828, 828, 672, 672},
            {923, 1077, 923, 1077}, {1327, 1327, 1173, 1173}, {1577, 1423, 1423, 1577}, { 1673, 1827, 1827, 1673},
            {1923, 2077, 1923, 2077}, {2327, 2173, 2173, 2327}, {2577, 2423, 2423, 2577}};
    private static final int[][] y = {{0, 10000, 0}, {0, 20000, 0}, {0, 30000, 0}, {40000, 0, 0}, {0, 50000, 0},
            {0, 60000, 0}, {0, 70000, 0}, {0, 80000, 0}, {90000, 0, 0}, {59645, 40355, 0}};
    private static final double d = 0.0005;
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
            pool = generatePool(d,  l[i],  x[i],  y[i]);
            topOnes[currentGen] = bestGenerationalFitness(pool);
            System.out.println("Generation: " + currentGen + " " + printC(topOnes[currentGen]) + " Fitness: " + topOnes[currentGen].fitnessFunction());
            currentGen++;
            while (currentGen < generations) {
                pool = progressiveGeneration(pool, d,  l[i],  x[i],  y[i]);
                topOnes[currentGen] = bestGenerationalFitness(pool);
                System.out.println("Generation: " + currentGen + " " + printC(topOnes[currentGen]) + " Fitness: " + topOnes[currentGen].fitnessFunction());
                currentGen++;
            }
            currentGen = 0;
            Chromosome3 bestSolution = bestGenerationalFitness(topOnes);
            addToRegistry(bestSolution, i);
            topOnes = new Chromosome3[generations];
        }

    }

    public static Chromosome3 start(int[] x, int[] y, int l) {
        int generations = 5000;
        int currentGen = 0;
        Chromosome3[] pool;
        Chromosome3[] topOnes = new Chromosome3[generations];
        pool = generatePool(d,  l,  x,  y);
        topOnes[currentGen] = bestGenerationalFitness(pool);
        System.out.println("Generation: " + currentGen + " " + printC(topOnes[currentGen]) + " Fitness: " + topOnes[currentGen].fitnessFunction());
        currentGen++;
        while (currentGen < generations) {
            pool = progressiveGeneration(pool,d,l, x, y);
            topOnes[currentGen] = bestGenerationalFitness(pool);
            System.out.println("Generation: " + currentGen + " " + printC(topOnes[currentGen]) + " Fitness: " + topOnes[currentGen].fitnessFunction());
            currentGen++;
        }
        return bestGenerationalFitness(topOnes);

    }

    private static void addToRegistry(Chromosome3 c, int pos) {
        try {
            createFile();
            FileWriter myWriter = new FileWriter("registry3.txt", true);
            BufferedWriter bw = new BufferedWriter(myWriter);
            bw.write("Best for workload "+ Arrays.toString(y[pos]) + ", Fitness score " + c.fitnessFunction() +
                    "Positions C:" + Arrays.deepToString(c.getC()) + "\n");
            bw.newLine();
            bw.close();
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private static void createFile() {
        try {
            File myObj = new File("registry3.txt");
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private static Chromosome3[] progressiveGeneration(Chromosome3[] pool, double d, int l, int[] x, int[] y) {
        Chromosome3[] newPool = new Chromosome3[pool.length];
        for (int i = 0; i < newPool.length; i++) {
            Chromosome3[] parents = pickParents(pool);
            Chromosome3 child = crossoverParents(parents, d, l, x, y);
            while(!child.checkConstraint(false)) {
                parents = pickParents(pool);
                child = crossoverParents(parents,  d, l, x, y);
            }
            newPool[i] = child;
        }
        return newPool;
    }

    private static Chromosome3 crossoverParents(Chromosome3[] parents, double d, int l, int[] x, int[] y) {
        int splittingPoint = rn.nextInt(parents[0].getC()[0].length);
        int mutation = rn.nextInt(100);
        int[][] C = new int[3][4];
        for (int i = 0; i < 3; i++) {
            int[] temp = new int[4];
            for (int j = 0; j < 4; j++) {
                if (j < splittingPoint) temp[j] = parents[0].getC()[i][j];
                else  temp[j] = parents[1].getC()[i][j];
            }
            C[i] = temp;
        }
        Chromosome3 child = new Chromosome3(d, l, x, y, rn);
        child.setC(C);
        if (mutation <= mutationRate) child.mutate();
        return child;
    }

    private static Chromosome3[] pickParents(Chromosome3[] pool) {
        Chromosome3[] winners = new Chromosome3[2];
        for (int i = 0; i < selectionSize; i++) {
            if (winners[0] == null) winners[i] = pool[rn.nextInt(pool.length)];
            else if (winners[1] == null) winners[1] = pool[rn.nextInt(pool.length)];
            else {
                Chromosome3 c = pool[rn.nextInt(pool.length)];
                if (c.fitnessFunction() < winners[0].fitnessFunction()) winners[0] = c;
                else if (c.fitnessFunction() < winners[1].fitnessFunction()) winners[1] = c;
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

    private static Chromosome3[] generatePool(double d, int l, int[] x, int[] y) {
        Chromosome3[] pool = new Chromosome3[poolSize];
        for (int i = 0; i < pool.length; i++) {
            Chromosome3 c = new Chromosome3(d, l, x, y, rn);
            c.setC(fixC(y));
            while(!c.checkConstraint(true)) c.setC(fixC(y));
            pool[i] = c;
        }
        return pool;
    }

    private static int[][] fixC(int[] y) {
        int[][] C = new int[3][4];
        for (int j = 0; j < 3; j++) {
            for (int m = 0; m < 4 ; m++) {
                if (y[j] == 0) C[j][m] = 0;
                else C[j][m] = rn.nextInt(y[j]);

            }
        }
        return C;
    }
}

class Chromosome3 {
    private int[][] C = new int[4][4];
    private final double d;
    private final int l;
    private final SecureRandom rn;
    private final int[] x;
    private final int[] y;

    Chromosome3(double d, int l, int[] x, int[] y, SecureRandom rn) {
        this.d = d;
        this.l = l;
        this.x = x;
        this.y = y;
        this.rn = rn;
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
        int[][] c = getC();
        for (int i = 0; i < 3; i++) {
            if (sum(c[i]) != 0) adjustSum(c[i]);
        }

    }

    private void adjustSum(int[] c) {
        int loops = Math.abs(sum(y) - sum(c));
        boolean bigger = sum(y) - sum(c) > 0;
        int count = 0;
        int grow = 0;
        while (grow < loops) {
            if (count == 3) count = 0;
            if (bigger) {
                c[count] = c[count]++;
                grow++;
            }
            else {
                if (c[count] > 0 ){
                    c[count] = c[count]--;
                    grow++;
                }
            }
            count++;
        }
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

    public void mutate() {
        int pos = rn.nextInt(getC().length);
        while (sum(C[pos]) == 0) pos = rn.nextInt(getC().length);
        int toMutate = rn.nextInt(C[pos].length);
        C[pos][toMutate] = rn.nextInt(y[pos]);
        checkConstraint(false);
    }
}

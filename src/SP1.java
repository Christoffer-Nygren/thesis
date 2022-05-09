import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;

public class SP1 {
    private static final SecureRandom rn = new SecureRandom(new byte[]{0, 0, 1, 1, 2, 2, 3, 3});
    private static final int selectionSize = 10;
    public static void main(String[] args) {
        int generations = 50000;
        int currentGen = 0;
        int poolSize = 100;
        int variants = 4;
        int mutationRate = 10;
        ChromosomeX[] generationalTops = new ChromosomeX[generations];
        ChromosomeX generationalBest;
        int[] X = {1000,2000,3000,4000,5000,6000,7000,8000,9000,10000}; // Workloads
        double[] V = {280, 512, 758, 1005.5, 1253.7, 1502.8, 1752.17, 2001.55, 2251.22, 2501}; // Service Rates
        for (int i = 0; i < X.length; i++) {
            ChromosomeX[] pool = generatePool(poolSize, variants, X[i]);
            generationalBest = currentBest(pool, V[i]);
            System.out.println("Gen: " + currentGen + ", Current best fitness: " + generationalBest.fitnessFunction(V[i]));
            generationalTops[currentGen] = generationalBest;
            currentGen++;
            while (currentGen < generations) {
                pool = progressiveGenerations(pool, X[i], V[i], mutationRate);
                generationalBest = currentBest(pool, V[i]);
                System.out.println("Gen: " + currentGen + ", Current best fitness: " + generationalBest.fitnessFunction(V[i]));
                generationalTops[currentGen] = generationalBest;
                currentGen++;
            }
            ChromosomeX absoluteBest = currentBest(generationalTops, V[i]);
            addToRegistry(absoluteBest, X[i], V[i]);

            generationalTops = new ChromosomeX[generations];
            currentGen = 0;
        }
    }

    private static void addToRegistry(ChromosomeX c, int X, double V) {
        try {
            createFile();
            FileWriter myWriter = new FileWriter("registry.txt", true);
            BufferedWriter bw = new BufferedWriter(myWriter);
            bw.write("Best for workload "+ X + ", Fitness score " + c.fitnessFunction(V) + ", Positions:"
                    + c.getX(0) + ", " + c.getX(1) + ", " + c.getX(2) +", " + c.getX(3) + ", "
                    + ", Sum calculated: " +  c.sum(V) + ", Power: " + c.pFog() +  ", Delay:  "
                    + c.dFog(V) + " \n");
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
            File myObj = new File("registry.txt");
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

    private static ChromosomeX[] progressiveGenerations(ChromosomeX[] pool, int X, double V, int mutationRate ) {
        ChromosomeX[] newPop = new ChromosomeX[pool.length];
        for (int i = 0; i < newPop.length; i++) {
            ChromosomeX child = getChild(pool, V, mutationRate, X);
            if (child.countSum() > X) {
                child.modifySum((child.countSum() - X), false);
            } else if (child.countSum() < X) {
                child.modifySum((X - child.countSum()), true);
            }
            newPop[i] =  child;
        }
        return newPop;
    }

    private static ChromosomeX getChild(ChromosomeX[] pool, double V, int mutationRate, int X) {
        ChromosomeX[] parents = pickRandomParent(pool, V);
        int crossover = rn.nextInt(4);
        int mutation = rn.nextInt(100);
        int[] parentVar1 = {parents[0].getX(0),parents[0].getX(1),parents[0].getX(2),parents[0].getX(3)};
        int[] parentVar2 = {parents[1].getX(0),parents[1].getX(1),parents[1].getX(2),parents[1].getX(3)};
        int[] newChild = new int[parentVar1.length];
        ChromosomeX child = new ChromosomeX(rn);
        for (int i = 0; i < parentVar1.length; i++) {
            if (i < crossover) {
                newChild[i] = parentVar1[i];
            } else {
                newChild[i] = parentVar2[i];
            }
        }
        for (int i = 0; i < 4; i++) {
            child.setX(newChild[i], i);
        }
        if (mutation <= mutationRate) child.mutate(X);
        return child;
    }


    private static ChromosomeX[] pickRandomParent(ChromosomeX[] pool, double V) {
        ChromosomeX[] selection = new ChromosomeX[2];

        for (int i = 0; i < selectionSize; i++) {
            if (selection[0] == null) selection[i] = pool[rn.nextInt(pool.length)];
            else if (selection[1] == null) selection[i] = pool[rn.nextInt(pool.length)];
            else {
                ChromosomeX c = pool[rn.nextInt(pool.length)];
                if (c.fitnessFunction(V) < selection[0].fitnessFunction(V)) selection[0] = c;
                else if (c.fitnessFunction(V) < selection[1].fitnessFunction(V)) selection[1] = c;
            }
        }


        return selection;
    }

    private static ChromosomeX currentBest(ChromosomeX[] pool, double V) {
        ChromosomeX best = null;
        for (ChromosomeX c: pool) {
            if (best == null) best = c;
            else if (best.fitnessFunction(V) > c.fitnessFunction(V)) best = c;
        }
        assert best != null;
        System.out.print(" X: " + best.getX(0) + ", " + best.getX(1) + ", " + best.getX(2) + ", " + best.getX(3) +
                ", ");

        return best;
    }

    private static ChromosomeX[] generatePool(int poolSize , int variants, int X) {
        ChromosomeX[] chromosomePool = new ChromosomeX[poolSize];
        for (int i = 0; i < chromosomePool.length; i++) {
            ChromosomeX c = generateChromosome(variants, X);
            while (c.checkConstraint(X)) {
                c = generateChromosome(variants, X);
            }
            chromosomePool[i] = c;
        }
        return chromosomePool;
    }


    private static ChromosomeX generateChromosome(int variants, int X) {
        ChromosomeX chromosome = new ChromosomeX(rn);
        for (int j = 0; j < variants; j++) {
            chromosome.setX(rn.nextInt(X-1) + 1, j);
        }
        return chromosome;
    }
}

class ChromosomeX {
    private final int[] x = new int[4];
    private final double a = 0.0000011;
    private final double b = 0.0021;
    private final short c = 0;
    private final SecureRandom rn;

    public ChromosomeX(SecureRandom rn) {
        this.rn = rn;
    }

    public int getX(int pos) {
        return x[pos];
    }

    public void setX(int x, int pos) {
        this.x[pos] = x;
    }
    public boolean checkConstraint(int X) {
        return countSum() != X;
    }

    public int countSum() {
        int sum = 0;
        for (int num : x) {
            sum += num;
        }
        return sum;
    }

    public void modifySum(int loops, boolean add) {
        for (int i = 0; i < loops; i++) {
            int pos = rn.nextInt(x.length);
            if (add) setX(getX(pos) + 1, pos);
            else {
                while (getX(pos) <= 0){
                    pos = rn.nextInt(x.length);
                }
                setX(getX(pos) - 1, pos);
            }
        }
    }
    public double sum(Double V) {
        double Z = 0;
        for (int j : x) {
            Z += (a * j * j) + (b * j) + c + (1 / Math.abs(V - j));
        }
        return Z;
    }

    public double dFog(Double v) {
        double Z = 0;
        for (int j : x) {
            Z += (1 / Math.abs(v - j));
        }
        return Z;
    }

    public double pFog() {
        double Z = 0;
        for (int j : x) {
            Z += (a * j * j) + (b * j) + c;
        }
        return Z;
    }
    public void mutate(int X) {
        int pos = rn.nextInt(x.length);
        setX(rn.nextInt(1000), pos);
        if (checkConstraint(X)) {
            int loops = X - countSum();
            if (loops > 0) modifySum(loops, true);
            else if (loops < 0) modifySum(loops, false);
        }
    }

    public double fitnessFunction(double V) {
        return dFog(V) + pFog();
    }
}

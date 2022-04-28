import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;

public class SP2 {
    public static void main(String[] args) {
        int generations = 3000;
        int currentGen = 0;
        int poolSize = 30;
        int variants = 5;
        Chromosomes[] generationalTops = new Chromosomes[generations];
        Chromosomes generationalBest;
        int[] Y = {1000,2000,3000,4000,5000,6000,7000,8000,9000,10000}; // Workloads
        double[] V = {222, 412, 608, 806.5, 1004.5, 1203.4, 1402.8, 1602, 1801.5, 2001.23}; // Service Rates
        for (int i = 0; i < Y.length; i++) {
            Chromosomes[] pool = generatePool(poolSize, variants, Y[i]);
            generationalBest = currentBest(pool, V[i]);
            System.out.println("Gen: " + currentGen + ", Current best fitness: " + generationalBest.fitnessFunction(V[i]));
            generationalTops[currentGen] = generationalBest;
            currentGen++;
            while (currentGen < generations) {
                pool = progressiveGenerations(pool, Y[i], V[i]);
                generationalBest = currentBest(pool, V[i]);
                System.out.println("Gen: " + currentGen + ", Current best fitness: " + generationalBest.fitnessFunction(V[i]));
                generationalTops[currentGen] = generationalBest;
                currentGen++;
            }
            Chromosomes absoluteBest = currentBest(generationalTops, V[i]);
            addToRegistry(absoluteBest, Y[i], V[i]);

            currentGen = 0;
        }
    }

    private static void addToRegistry(Chromosomes c, int Y, double V) {
        try {
            createFile();
            FileWriter myWriter = new FileWriter("registry2.txt", true);
            BufferedWriter bw = new BufferedWriter(myWriter);
            bw.write("Best for workload "+ Y + ", Fitness score " + c.fitnessFunction(V) + ", Positions:" + c.getY(0) + ", "
                    + c.getY(1) + ", " + c.getY(2) +", " + c.getY(3) + ", " + c.getY(4) + "\n");
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

    private static Chromosomes[] progressiveGenerations(Chromosomes[] pool, int Y, double V ) {
        Chromosomes[] newPop = new Chromosomes[pool.length];
        for (int i = 0; i < newPop.length; i++) {
            Chromosomes child = getChild(pool, V);
            if (child.countSum() > Y) {
                child.modifySum((child.countSum() - Y), false);
            } else if (child.countSum() < Y) {
                child.modifySum((Y - child.countSum()), true);
            }
            newPop[i] =  child;
        }
        return newPop;
    }

    private static Chromosomes getChild(Chromosomes[] pool, double V) {
        Chromosomes parent1 = pickRandomParent(pool,V);
        Chromosomes parent2 = pickRandomParent(pool,V);
        SecureRandom rn = new SecureRandom();
        int crossover = rn.nextInt(5);
        int[] parentVar1 = {parent1.getY(0),parent1.getY(1),parent1.getY(2),parent1.getY(3),parent1.getY(4)};
        int[] parentVar2 = {parent2.getY(0),parent2.getY(1),parent2.getY(2),parent2.getY(3),parent2.getY(4)};
        int[] newChild = new int[parentVar1.length];
        Chromosomes child = new Chromosomes();
        for (int i = 0; i < parentVar1.length; i++) {
            if (i < crossover) {
                newChild[i] = parentVar1[i];
            } else {
                newChild[i] = parentVar2[i];
            }
        }
        for (int i = 0; i < 5; i++) {
            child.setY(newChild[i], i);
        }
        return child;
    }

    private static Chromosomes pickRandomParent(Chromosomes[] pool, double V) {
        double random = Math.random();
        double progress = 0.0;
        Chromosomes c = null;
        for (Chromosomes cx : pool) {
            progress += calculateProbability(cx, pool, V);
            if (progress >= random) {
                c = cx;
                break;
            }
        }
        return c;

    }


    private static double calculateProbability(Chromosomes c, Chromosomes[] population, double V){
        return c.fitnessFunction(V) / calculateTotalFitness(population, V);
    }

    private static double calculateTotalFitness(Chromosomes[] pop, double V) {
        double totalFit = 0.0;
        for (Chromosomes c : pop) {
            totalFit += c.fitnessFunction(V);
        }
        return totalFit;
    }

    private static Chromosomes currentBest(Chromosomes[] pool, double V) {
        Chromosomes best = null;
        for (Chromosomes c: pool) {
            if (best == null) best = c;
            else if (best.fitnessFunction(V) < c.fitnessFunction(V)) best = c;
        }
        System.out.print(" X: " + best.getY(0) + ", " + best.getY(1) + ", " + best.getY(2) + ", " + best.getY(3) +
                ", " + best.getY(4) + ", ");

        return best;
    }

    private static Chromosomes[] generatePool(int poolSize , int variants, int Y) {
        Chromosomes[] chromosomePool = new Chromosomes[poolSize];
        for (int i = 0; i < chromosomePool.length; i++) {
            Chromosomes c = generateChromosome(variants, Y);
            while (!c.checkConstraint(Y)) {
                c = generateChromosome(variants, Y);
            }

            chromosomePool[i] = c;
        }
        return chromosomePool;
    }


    private static Chromosomes generateChromosome(int variants, int Y) {
        double[] aVal = {3.206, 4.485, 2.370};
        int[] bVal = {68, 53, 70};
        double[] fMax = {3.4, 2.4, 4.0};
        int[] nMax = {30000, 60000, 25000};
        int fMin = 1;
        int p = 3;
        SecureRandom random = new SecureRandom();
        Chromosomes c = new Chromosomes();
        for (int j = 0; j < variants; j++) {
            c.setY(random.nextInt(Y-1) + 1, j);
        }
        while (!c.checkConstraint(Y)) {
            c = generateChromosome(variants, Y);
        }
        int rValue = random.nextInt(3);
        for (int j = 0; j < variants; j++) {
            c.setA(aVal[rValue], j);
            rValue = random.nextInt(3);
            c.setB(bVal[rValue], j);
            rValue = random.nextInt(3);
            c.setF(random.nextDouble(fMax[rValue]) + fMin, j);
            rValue = random.nextInt(3);
            c.setN(random.nextInt(nMax[rValue]) + fMin, j);
        }
        return c;
    }
}

class Chromosomes {
    private int[] y = new int[5];
    private int[] s = new int[5];
    private double[] A = new double[5];
    private int[] B = new int[5];
    private double[] f = new double[5];
    private int[] n = new int[5];

    public int getY(int pos) {
        return y[pos];
    }
    public void setY(int y, int pos) {
        this.y[pos] = y;
    }

    public void setsS(int s, int pos) {
        this.s[pos] = s;
    }
    public int getS(int pos) {
        return s[pos];
    }

    public void setA(double A, int pos) {
        this.A[pos] = A;
    }
    public double getA(int pos) {
        return A[pos];
    }

    public void setB(int B, int pos) {
        this.B[pos] = B;
    }
    public int getB(int pos) {
        return B[pos];
    }

    public void setF(double f, int pos) {
        this.f[pos] = f;
    }
    public double getF(int pos) {
        return f[pos];
    }
    public void setN(int n, int pos) {
        this.n[pos] = n;
    }
    public int getN(int pos) {
        return n[pos];
    }

    public boolean checkConstraint(int Y) {
        return countSum() == Y;
    }

    public int countSum() {
        int sum = 0;
        for (int num : y) {
            sum += num;
        }
        return sum;
    }

    public void modifySum(int loops, boolean add) {
        SecureRandom rn = new SecureRandom();
        for (int i = 0; i < loops; i++) {
            int pos = rn.nextInt(y.length);
            if (add) setY(getY(pos) + 1, pos);
            else {
                while (getY(pos) <= 0){
                    pos = rn.nextInt(y.length);
                }
                setY(getY(pos) - 1, pos);
            }
        }
    }

    public double fitnessFunction(double V) {
        double Z = 0;
        for (int j = 0; j < y.length; j++) {
            Z += (s[j]*n[j]*(A[j]*(Math.pow(f[j], 3)) + B[j]));
        }
        return 1 / Z;
    }
}

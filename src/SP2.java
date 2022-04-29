import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;

public class SP2 {
    public static void main(String[] args) {
        int generations = 3000;
        int currentGen = 0;
        int poolSize = 100;
        int variants = 5;
        Chromosomes[] generationalTops = new Chromosomes[generations];
        Chromosomes generationalBest;
        int[] Y = {1000,2000,3000,4000,5000,6000,7000,8000,9000,10000}; // Workloads
        for (int i = 0; i < Y.length; i++) {
            Chromosomes[] pool = generatePool(poolSize, variants, Y[i]);
            generationalBest = currentBest(pool);
            System.out.println("Gen: " + currentGen + ", Current best fitness: " + generationalBest.fitnessFunction());
            generationalTops[currentGen] = generationalBest;
            currentGen++;
            while (currentGen < generations) {
                pool = progressiveGenerations(pool, Y[i]);
                generationalBest = currentBest(pool);
                System.out.println("Gen: " + currentGen + ", Current best fitness: " + generationalBest.fitnessFunction());
                generationalTops[currentGen] = generationalBest;
                currentGen++;
            }
            Chromosomes absoluteBest = currentBest(generationalTops);
            addToRegistry(absoluteBest, Y[i]);

            currentGen = 0;
        }
    }

    private static void addToRegistry(Chromosomes c, int Y) {
        try {
            createFile();
            FileWriter myWriter = new FileWriter("registry2.txt", true);
            BufferedWriter bw = new BufferedWriter(myWriter);
            bw.write("Best for workload "+ Y + ", Fitness score " + c.fitnessFunction() + ", Positions:" + c.getA(0) + ", "
                    + c.getA(1) + ", " + c.getA(2) +", " + c.getA(3) + ", " + c.getA(4) + "\n");
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

    private static Chromosomes[] progressiveGenerations(Chromosomes[] pool, int Y) {
        Chromosomes[] newPop = new Chromosomes[pool.length];
        for (int i = 0; i < newPop.length; i++) {
            Chromosomes child = getChild(pool);
            if (child.countSum() > Y) {
                child.modifySum((child.countSum() - Y), false);
            } else if (child.countSum() < Y) {
                child.modifySum((Y - child.countSum()), true);
            }
            child.checkConstraint(Y, false);
            child.checkS();
            newPop[i] =  child;
        }
        return newPop;
    }

    private static Chromosomes getChild(Chromosomes[] pool) {
        Chromosomes parent1 = pickRandomParent(pool);
        Chromosomes parent2 = pickRandomParent(pool);
        Chromosomes child = new Chromosomes();
        int[] y = crossoverLoopInt(new int[]{parent1.getY(0), parent1.getY(1), parent1.getY(2), parent1.getY(3), parent1.getY(4)},
                new int[]{parent2.getY(0), parent2.getY(1), parent2.getY(2), parent2.getY(3), parent2.getY(4)});
        int[] s = crossoverLoopInt(new int[]{parent1.getS(0), parent1.getS(1), parent1.getS(2), parent1.getS(3), parent1.getS(4)},
                new int[]{parent2.getS(0), parent2.getS(1), parent2.getS(2), parent2.getS(3), parent2.getS(4)});
        double[] A = crossoverLoopDouble(new double[]{parent1.getA(0), parent1.getA(1), parent1.getA(2), parent1.getA(3), parent1.getA(4)},
                new double[]{parent2.getA(0), parent2.getA(1), parent2.getA(2), parent2.getA(3), parent2.getA(4)});
        int[] B = crossoverLoopInt(new int[]{parent1.getB(0), parent1.getB(1), parent1.getB(2), parent1.getB(3), parent1.getB(4)},
                new int[]{parent2.getB(0), parent2.getB(1), parent2.getB(2), parent2.getB(3), parent2.getB(4)});
        double[] f = crossoverLoopDouble(new double[]{parent1.getF(0), parent1.getF(1), parent1.getF(2), parent1.getF(3), parent1.getF(4)},
                new double[]{parent2.getF(0), parent2.getF(1), parent2.getF(2), parent2.getF(3), parent2.getF(4)});
        int[] n = crossoverLoopInt(new int[]{parent1.getN(0), parent1.getN(1), parent1.getN(2), parent1.getN(3), parent1.getN(4)},
                new int[]{parent2.getN(0), parent2.getN(1), parent2.getN(2), parent2.getN(3), parent2.getN(4)});
        for (int i = 0; i < 5; i++) {
            child.setY(y[i], i);
            child.setS(s[i], i);
            child.setA(A[i], i);
            child.setB(B[i], i);
            child.setF(f[i], i);
            child.setN(n[i], i);
        }

        return child;
    }
    private static int[] crossoverLoopInt(int[] a, int[] b) {
        SecureRandom rn = new SecureRandom();
        int crossover = rn.nextInt(5);
        int[] newChild = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            if (i < crossover) {
                newChild[i] = a[i];
            } else {
                newChild[i] = b[i];
            }
        }
        return newChild;

    }
    private static double[] crossoverLoopDouble(double[] a, double[] b) {
        SecureRandom rn = new SecureRandom();
        int crossover = rn.nextInt(5);
        double[] newChild = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            if (i < crossover) {
                newChild[i] = a[i];
            } else {
                newChild[i] = b[i];
            }
        }
        return newChild;
    }

    private static Chromosomes pickRandomParent(Chromosomes[] pool) {
        double random = Math.random();
        double progress = 0.0;
        Chromosomes c = null;
        for (Chromosomes cx : pool) {
            progress += calculateProbability(cx, pool);
            if (progress >= random) {
                c = cx;
                break;
            }
        }
        return c;

    }


    private static double calculateProbability(Chromosomes c, Chromosomes[] population){
        return c.fitnessFunction() / calculateTotalFitness(population);
    }

    private static double calculateTotalFitness(Chromosomes[] pop) {
        double totalFit = 0.0;
        for (Chromosomes c : pop) {
            totalFit += c.fitnessFunction();
        }
        return totalFit;
    }

    private static Chromosomes currentBest(Chromosomes[] pool) {
        Chromosomes best = null;
        for (Chromosomes c: pool) {
            if (best == null) best = c;
            else if (best.fitnessFunction() < c.fitnessFunction()) best = c;
        }
        System.out.print(" S: " + best.getB(0) + ", " + best.getB(1) + ", " + best.getB(2) + ", " + best.getB(3) +
                ", " + best.getB(4) + ", ");

        return best;
    }

    private static Chromosomes[] generatePool(int poolSize , int variants, int Y) {
        Chromosomes[] chromosomePool = new Chromosomes[poolSize];
        for (int i = 0; i < chromosomePool.length; i++) {
            Chromosomes c = generateChromosome(variants, Y);
            while (!c.checkConstraint(Y, true)) {
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
        SecureRandom random = new SecureRandom();
        Chromosomes c = new Chromosomes();
        for (int j = 0; j < variants; j++) {
            c.setY(random.nextInt(Y-1) + 1, j);
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
            rValue = random.nextInt(2);
            c.setS(rValue, j);

        }
        c.checkS();
        c.checkConstraint(Y, false);
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

    public void setS(int s, int pos) {
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

    public boolean checkConstraint(int Y, boolean first) {
        if (!first) fixSum();
        return countSum() == Y;
    }

    private void fixSum() {
        int sum = 0;
        int count = 0;
        for(int i = 0; i < y.length; i++) {
            if (s[i] == 0) {
                sum += y[i];
                setY(0, i);
            }
        }
        while (sum != 0) {
            if (count == 5) count = 0;
            if (s[count] != 0) {
                setY(getY(count) + 1, count);
            }
            sum--;
        }
        for(int i = 0; i < sum; i++) {
            if (s[i] != 0) {
                sum += y[i];
                setY(0, i);
            }
        }
    }

    public int countSum() {
        int sum = 0;
        for (int num : y) {
            sum += num;
        }
        return sum;
    }

    public void checkS() {
        boolean zero = true;
        for (int i: s) {
            if (i == 1) zero = false;
        }
        if (zero) {
            SecureRandom rn = new SecureRandom();
            int num =rn.nextInt(5);
            setS(1, num);
        }
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

    public double fitnessFunction() {
        double Z = 0;
        for (int j = 0; j < y.length; j++) {
            Z += (s[j]*n[j]*(A[j]*(Math.pow(f[j], 3)) + B[j]));
        }
        return 1 / (Z);
    }
}

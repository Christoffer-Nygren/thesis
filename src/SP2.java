import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;

public class SP2 {
    public static void main(String[] args) {
        int generations = 300;
        int currentGen = 0;
        int poolSize = 50;
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
            bw.write("Best for workload "+ Y + ", Fitness score " + c.fitnessFunction() +
                    ", Positions A:" + toString(c.getFullA()) + "\n" +
                    ", Positions B:" + toString(c.getFullB()) + "\n" +
                    ", Positions S:" + toString(c.getFullS())  + "\n" +
                    ", Positions F:" + toString(c.getFullF())  + "\n" +
                    ", Positions N:" + toString(c.getFullN())  + "\n" +
                    ", Positions Y:" + toString(c.getFullY())  + "\n");
            bw.newLine();
            bw.close();
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private static String toString(int[] arr) {
        StringBuilder string = new StringBuilder();
        for (int i: arr) string.append(i).append(", ");
        return String.valueOf(string);

    }
    private static String toString(double[] arr) {
        StringBuilder string = new StringBuilder();
        for (double i: arr) string.append(i).append(", ");
        return String.valueOf(string);
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
        int[] y = crossoverLoop(parent1.getFullY(), parent2.getFullY());
        int[] s = crossoverLoop(parent1.getFullS(), parent2.getFullS());
        double[] A = crossoverLoop(parent1.getFullA(), parent2.getFullA());
        int[] B = crossoverLoop(parent1.getFullB(), parent2.getFullB());
        double[] f = crossoverLoop(parent1.getFullF(), parent2.getFullF());
        int[] n = crossoverLoop(parent1.getFullN(), parent2.getFullN());
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
    private static int[] crossoverLoop(int[] a, int[] b) {
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
    private static double[] crossoverLoop(double[] a, double[] b) {
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
        Chromosomes c = null;
        for (Chromosomes cc: pool) {
            if (c == null) c = cc;
            else if (c.fitnessFunction() < c.fitnessFunction()) c = cc;
        }
        System.out.print( "Positions A:" + c.getA(0) + ", "
                + c.getA(1) + ", " + c.getA(2) +", " + c.getA(3) + ", " + c.getA(4) + "\n" + ", Positions B:" + c.getB(0) + ", "
                + c.getB(1) + ", " + c.getB(2) +", " + c.getB(3) + ", " + c.getB(4) + "\n" + ", Positions S:" + c.getS(0) + ", "
                + c.getS(1) + ", " + c.getS(2) +", " + c.getS(3) + ", " + c.getS(4) + "\n" + ", Positions F:" + c.getF(0) + ", "
                + c.getF(1) + ", " + c.getF(2) +", " + c.getF(3) + ", " + c.getF(4) + "\n" + ", Positions N:" + c.getN(0) + ", "
                + c.getN(1) + ", " + c.getN(2) +", " + c.getN(3) + ", " + c.getN(4) + "\n" + ", Positions Y:" + c.getY(0) + ", "
                + c.getY(1) + ", " + c.getY(2) +", " + c.getY(3) + ", " + c.getY(4) + "\n");

        return c;
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
    public int[] getFullY() {
        return y;
    }
    public void setY(int y, int pos) {
        this.y[pos] = y;
    }

    public void setS(int s, int pos) {
        this.s[pos] = s;
    }
    public int[] getFullS() {
        return s;
    }
    public int getS(int pos) {
        return s[pos];
    }

    public void setA(double A, int pos) {
        this.A[pos] = A;
    }
    public double[] getFullA() {
        return A;
    }
    public double getA(int pos) {
        return A[pos];
    }

    public void setB(int B, int pos) {
        this.B[pos] = B;
    }
    public int[] getFullB() {
        return n;
    }
    public int getB(int pos) {
        return B[pos];
    }

    public void setF(double f, int pos) {
        this.f[pos] = f;
    }
    public double[] getFullF() {
        return f;
    }
    public double getF(int pos) {
        return f[pos];
    }

    public void setN(int n, int pos) {
        this.n[pos] = n;
    }
    public int[] getFullN() {
        return n;
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

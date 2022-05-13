import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;

public class workingSP2 {
    private static final SecureRandom rn = new SecureRandom(new byte[]{0, 0, 1, 1, 2, 2, 3, 3});
    private static final int selectionSize = 5;
    private static final int mutationRate = 10;
    private static final int[] Y = {10000,20000,30000,40000,50000,60000,70000,80000,90000,100000}; // Workloads
    public static void main(String[] args) {
        int generations = 59;
        int currentGen = 0;
        int poolSize = 100;
        int variants = 3;
        ChromosomesS[] generationalTops = new ChromosomesS[generations];
        ChromosomesS generationalBest;
        for (int j : Y) {
            ChromosomesS[] pool = generatePool(poolSize, variants, j);
            generationalBest = currentBest(pool);
            System.out.println("Gen: " + currentGen + ", Current best fitness: " + generationalBest.fitnessFunction());
            generationalTops[currentGen] = generationalBest;
            currentGen++;
            while (currentGen < generations) {
                pool = progressiveGenerations(pool, j);
                generationalBest = currentBest(pool);
                System.out.println("Gen: " + currentGen + ", Current best fitness: " + generationalBest.fitnessFunction());
                generationalTops[currentGen] = generationalBest;
                currentGen++;
            }
            ChromosomesS absoluteBest = currentBest(generationalTops);
            addToRegistry(absoluteBest, j);

            currentGen = 0;
        }
    }

    private static void addToRegistry(ChromosomesS c, int Y) {
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
                    ", Positions Y:" + toString(c.getFullY())  + "\n" +
                    ", Power: " + c.pCloud() + ", Delay: " + c.dCloud());
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
            File myObj = new File("registry2.txt");
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

    private static ChromosomesS[] progressiveGenerations(ChromosomesS[] pool, int Y) {
        ChromosomesS[] newPop = new ChromosomesS[pool.length];
        for (int i = 0; i < newPop.length; i++) {
            ChromosomesS child = getChild(pool);
            if (child.countSum() > Y) {
                child.modifySum((child.countSum() - Y), false);
            } else if (child.countSum() < Y) {
                child.modifySum((Y - child.countSum()), true);
            }
            int mutation = rn.nextInt(100);
            if (mutation <= mutationRate) child.mutate(Y);
            child.checkConstraint(Y, false);
            newPop[i] =  child;
        }
        return newPop;
    }

    private static ChromosomesS getChild(ChromosomesS[] pool) {
        ChromosomesS[] parents = pickRandomParent(pool);
        ChromosomesS child = new ChromosomesS(rn);
        int[] y = crossoverLoop(parents[0].getFullY(), parents[1].getFullY());
        int[] s = crossoverLoop(parents[0].getFullS(), parents[1].getFullS());
        double[] f = crossoverLoop(parents[0].getFullF(), parents[1].getFullF());
        int[] n = crossoverLoop(parents[0].getFullN(), parents[1].getFullN());
        for (int i = 0; i < 3; i++) {
            child.setY(y[i], i);
            child.setS(s[i], i);
            child.setF(f[i], i);
            child.setN(n[i], i);
        }

        return child;
    }
    private static int[] crossoverLoop(int[] a, int[] b) {
        int crossover = rn.nextInt(a.length);
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
        int crossover = rn.nextInt(a.length);
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

    private static ChromosomesS[] pickRandomParent(ChromosomesS[] pool) {
        ChromosomesS[] selection = new ChromosomesS[2];

        for (int i = 0; i < selectionSize; i++) {
            if (selection[0] == null) selection[i] = pool[rn.nextInt(pool.length)];
            else if (selection[1] == null) selection[i] = pool[rn.nextInt(pool.length)];
            else {
                ChromosomesS c = pool[rn.nextInt(pool.length)];
                if (c.fitnessFunction() < selection[0].fitnessFunction()) selection[0] = c;
                else if (c.fitnessFunction() < selection[1].fitnessFunction()) selection[1] = c;
            }
        }


        return selection;
    }


    private static ChromosomesS currentBest(ChromosomesS[] pool) {
        ChromosomesS c = null;
        for (ChromosomesS cc: pool) {
            if (c == null) c = cc;
            else if (cc.fitnessFunction() < c.fitnessFunction()) c = cc;
        }
        assert c != null;
        System.out.print(
                ", Positions S:" + c.getS(0) + ", " + c.getS(1) + ", " + c.getS(2) + "\n"
                        + ", Positions F:" + c.getF(0) + ", " + c.getF(1) + ", " + c.getF(2) + "\n"
                        + ", Positions N:" + c.getN(0) + ", " + c.getN(1) + ", " + c.getN(2) + "\n"
                        + ", Positions Y:" + c.getY(0) + ", " + c.getY(1) + ", " + c.getY(2) + "\n");

        return c;
    }

    private static ChromosomesS[] generatePool(int poolSize , int variants, int Y) {
        ChromosomesS[] chromosomePool = new ChromosomesS[poolSize];
        for (int i = 0; i < chromosomePool.length; i++) {
            ChromosomesS c = generateChromosome(variants, Y);
            while (!c.checkConstraint(Y, true)) {
                c = generateChromosome(variants, Y);
            }
            chromosomePool[i] = c;
        }
        return chromosomePool;
    }


    private static ChromosomesS generateChromosome(int variants, int Y) {
        double[] fMax = {3.4, 2.4, 4.0};
        int[] nMax = {30000, 60000, 25000};
        int fMin = 1;
        ChromosomesS c = new ChromosomesS(rn);
        for (int j = 0; j < variants; j++) {
            c.setY(rn.nextInt(Y-1) + 1, j);
        }
        int rValue;
        for (int j = 0; j < variants; j++) {
            c.setF(rn.nextDouble(fMax[j]) + fMin, j);
            rValue = rn.nextInt(3);
            c.setN(rn.nextInt(nMax[rValue]) + fMin, j);
            rValue = rn.nextInt(2);
            c.setS(rValue, j);

        }
        c.checkConstraint(Y, false);
        return c;
    }

}

class ChromosomesS {
    private final int[] y = new int[3];
    private final int[] s = new int[3];
    private final double[] A = {3.206, 4.485, 2.370};
    private final int[] B = {68, 53, 70};
    private final double[] f = new double[3];
    private final int[] n = new int[3];
    private final int[] nMax = {30000, 60000, 25000};
    private final SecureRandom rn;

    ChromosomesS(SecureRandom rn) {
        this.rn = rn;
    }

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

    public double[] getFullA() {
        return A;
    }

    public int[] getFullB() {
        return B;
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
        checkS();
        if (!first) fixSum();
        checkSum(Y);
        checkN();
        //if (checkDelayCon())
        return countSum() == Y;
        //return false;
    }

    private void checkN() {
        for (int i = 0; i < n.length; i++) {
            if (n[i] == 0) {
                setN(rn.nextInt(nMax[i]) + 1, i);
                checkN();
            }
        }
    }

    public void checkSum(int Y) {
        if (countSum() < Y) adjustSum(Y - countSum(), true);
        else if (countSum() > Y) adjustSum(Y - countSum(), false);
    }

    private void adjustSum(int loops, boolean add) {
        int count = 0;
        int amount = Math.abs(loops);
        while (amount > 0) {
            if (count > 2) {
                count = 0;
            }
            if (add){
                if (s[count] != 0) {
                    setY(getY(count) + 1, count);
                    amount--;
                }
            } else {
                if (s[count] != 0) {
                    setY(getY(count) - 1, count);
                    amount--;
                }
            }
            count++;
        }
    }

    public double pCloud() {
        double Z = 0;
        for (int j = 0; j < y.length; j++) {
            Z += (s[j]*n[j]*(A[j]*(Math.pow(f[j], 3)) + B[j]));
        }
        return Z;
    }

    public double dCloud() {
        double Z = 0;
        for (int i = 0; i < s.length; i++) {
            double erlang = erlangC(i);
            double val = ((n[i] * f[i])-y[i]) + (1/f[i]);
            double delay = (s[i] * (erlang/val));
            Z += delay;
        }
        return 1.0;
    }

    public void mutate(int Y) {
        double[] fMax = {3.4, 2.4, 4.0};
        int[] nMax = {30000, 60000, 25000};
        int toMutate = rn.nextInt(4);
        int pos = rn.nextInt(3);
        if (toMutate == 0) {
            setY(rn.nextInt(Y), pos);
        } else if (toMutate == 1) {
            setS(rn.nextInt(1), pos);
        } else if (toMutate == 2) {
            setF(rn.nextDouble(fMax[pos]) + 1, pos);
        } else {
            setN(rn.nextInt(nMax[pos]), pos);
        }
        checkConstraint(Y, false);

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
            if (count == 3) count = 0;
            if (s[count] != 0) {
                setY(getY(count) + 1, count);
                sum--;
            }
            count++;
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
        int sum = 0;
        for (int i : s) sum += i;

        boolean zero = sum == 0 || sum == 1;
        if (zero) {
            int pos =rn.nextInt(3);
            setS(1, pos);
        }
    }

    public boolean checkDelayCon() {
        for (int i = 0; i < s.length; i++) {
            double erlang = erlangC(i);
            double val = ((n[i] * f[i])-y[i]) + (1/f[i]);
            double delay = (s[i] * (erlang/val));
            if ( (delay > 1 && s[i] != 0) || (delay < 0 && s[i] != 0)) return false;
        }
        return  true;
    }
    private long factorial(int n){
        long sum = 0;
        for (int i = 0; i < n; i++) {
            sum += (long) n * (n-i);
        }
        return sum;
    }
    private double erlangC(int pos){
        int N = n[pos];
        double A = (y[pos])/f[pos];
        long pow = (long) Math.pow(A, N);
        double v = (pow / factorial(N)) * (N / (N - A));
        double delay = (v / (Math.pow(A, 0) + v));
        return delay;
    }


    public void modifySum(int loops, boolean add) {
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
        return Z;
    }
}
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;

public class AllTogether {
    private static final int[] l = {11000,22000,33000, 44000, 55000, 66000, 77000, 88000, 99000, 110000};
    private static final double[] V = {280, 512, 758, 1005.5, 1253.7, 1502.8, 1752.17, 2001.55, 2251.22, 2501};
    private static final int poolSize = 100;
    private static final int generations = 5000;
    private static final SecureRandom rn = new SecureRandom(new byte[]{0, 0, 1, 1, 2, 2, 3, 3});
    public static void main(String[] args) {
        int currentGen = 0;
        Chromosome4[] pool;
        Chromosome4[] topOnes = new Chromosome4[generations];
        for (int i = 0; i < l.length; i++) {
            pool = generatePool(l[i], V[i]);
            topOnes[currentGen] = bestGenerationalFitness(pool, V[i]);
            System.out.println("Generation: " + currentGen + " " + printC(topOnes[currentGen], V[i], l[i]) + " Fitness: " + topOnes[currentGen].fitnessFunction(V[i]));
            currentGen++;
            while (currentGen < generations) {
                pool = progressiveGeneration(pool, V[i], l[i]);
                topOnes[currentGen] = bestGenerationalFitness(pool, V[i]);
                System.out.println("Generation: " + currentGen + " " + printC(topOnes[currentGen], V[i], l[i]) + " Fitness: " + topOnes[currentGen].fitnessFunction(V[i]));
                currentGen++;
            }
            currentGen = 0;
            Chromosome4 bestSolution = bestGenerationalFitness(topOnes, V[i]);
            addToRegistry(bestSolution, l[i], V[i]);
            topOnes = new Chromosome4[generations];
        }

    }

    private static Chromosome4[] progressiveGeneration(Chromosome4[] pool, double V, int l) {
        return new Chromosome4[0];
    }

    private static String printC(Chromosome4 c, double V, int l) {
        return ("Best for workload "+ l + ", Fitness score " + c.fitnessFunction(V) +
                "Division: X: " + Arrays.toString(c.getX().getFullX()) + ", Y: " + Arrays.toString(c.getY().getFullY()) +
                ", D: " + Arrays.deepToString(c.getD().getC()) + "\n");
    }

    private static Chromosome4 bestGenerationalFitness(Chromosome4[] pool, double V) {
        Chromosome4 c = null;
        for (Chromosome4 cc: pool) {
            if (c == null) c = cc;
            else if (cc.fitnessFunction(V) < c.fitnessFunction(V)) c = cc;
        }
        return c;
    }

    private static void addToRegistry(Chromosome4 c, int l, double V) {
        try {
            createFile();
            FileWriter myWriter = new FileWriter("registry4.txt", true);
            BufferedWriter bw = new BufferedWriter(myWriter);
            bw.write("Best for workload "+ l + ", Fitness score " + c.fitnessFunction(V) +
                    "Division: X: " + Arrays.toString(c.getX().getFullX()) + ", Y: " + Arrays.toString(c.getY().getFullY()) +
                    ", D: " + Arrays.deepToString(c.getD().getC()) + "\n");
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
            File myObj = new File("registry4.txt");
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

    private static Chromosome4[] generatePool(int l, double V) {
        Chromosome4[] pool = new Chromosome4[poolSize];
        for (int i = 0; i < pool.length; i++) {
            int y = rn.nextInt(l);
            int x = l - y;
            Chromosome1 sp1 = SP1.start(x, V);
            Chromosome2 sp2 = SP2.start(y);
            Chromosome3 sp3 = SP3.start(sp1.getFullX(), sp2.getFullY() , l);
            Chromosome4 c = new Chromosome4(sp1, sp2, sp3);
            while(!c.checkConstraint(V)) {
                y = rn.nextInt(l);
                x = l - y;
                sp1 = SP1.start(x, V);
                sp2 = SP2.start(y);
                sp3 = SP3.start(sp1.getFullX(), sp2.getFullY() , l);
                c = new Chromosome4(sp1, sp2, sp3);
            }
            pool[i] = c;
        }
        return pool;
    }
}

class Chromosome4 {
    private final Chromosome1 x;
    private final Chromosome2 y;
    private final Chromosome3 d;


    Chromosome4(Chromosome1 x, Chromosome2 y, Chromosome3 d) {
        this.x = x;
        this.y = y;
        this.d = d;
    }

    public Chromosome1 getX()  { return x;}
    public Chromosome2 getY()  { return y;}
    public Chromosome3 getD()  { return d;}


    public double fitnessFunction(double V) {
        return x.fitnessFunction(V) + y.fitnessFunction() + d.fitnessFunction();
    }

    public boolean checkConstraint(double V) {
        return !(x.dFog(V) + y.dCloud() + d.fitnessFunction() > 10);
    }
}

import java.security.SecureRandom;

public class GA2 {
    public static void main(String[] args) {
        int generations = 300;
        int currentGen = 0;
        int poolSize = 30;
        int variants = 5;
        int[] X = {1000,2000,3000,4000,5000,6000,7000,8000,9000,10000}; // Workloads
        double[] V = {222, 412, 608, 806.5, 1004.5, 1203.4, 1402.8, 1602, 1801.5, 2001.23}; // Service Rates
        for (int i = 0; i < X.length; i++) {
            ChromosomeX[] pool = generatePool(poolSize, variants, X[i]);
            System.out.println("Gen: " + currentGen + ", Current best fitness: " + currentBest(pool, V[i]).fitnessFunction(V[i]));
            currentGen++;
            while (currentGen < generations) {
                pool = progressiveGenerations(pool, X[i], V[i]);
                System.out.println("Gen: " + currentGen + ", Current best fitness: " + currentBest(pool, V[i]).fitnessFunction(V[i]));
                currentGen++;
            }
            currentGen = 0;
        }
    }

    private static ChromosomeX[] progressiveGenerations(ChromosomeX[] pool, int X, double V ) {
        ChromosomeX[] newPop = new ChromosomeX[pool.length];
        for (int i = 0; i < newPop.length; i++) {
            ChromosomeX child = getChild(pool, 1, X, V);
            while (!child.checkConstraint(X)) {
                child = getChild(pool, 1, X, V);
            }
            newPop[i] =  child;
        }
        return newPop;
    }

    private static ChromosomeX getChild(ChromosomeX[] pool, int mutationRate, int X, double V) {
        ChromosomeX parent1 = pickRandomParent(pool,V);
        ChromosomeX parent2 = pickRandomParent(pool,V);
        SecureRandom rn = new SecureRandom();
        int mutation = rn.nextInt(101);
        int crossover = rn.nextInt(5);
        int[] parentVar1 = {parent1.getX(0),parent1.getX(1),parent1.getX(2),parent1.getX(3),parent1.getX(4)};
        int[] parentVar2 = {parent2.getX(0),parent2.getX(1),parent2.getX(2),parent2.getX(3),parent2.getX(4)};
        int[] newChild = new int[parentVar1.length];
        ChromosomeX child = new ChromosomeX();
        for (int i = 0; i < parentVar1.length; i++) {
            if (i < crossover) {
                newChild[i] = parentVar1[i];
            } else {
                newChild[i] = parentVar2[i];
            }
        }
        for (int i = 0; i < 5; i++) {
            child.setX(newChild[i], i);
        }
        if (mutationRate <= mutation) {
            child.mutate(X);
        }
        return child;
    }

    private static ChromosomeX pickRandomParent(ChromosomeX[] pool, double V) {
        double random = Math.random();
        double progress = 0.0;
        ChromosomeX c = null;
        for (ChromosomeX cx : pool) {
            progress += calculateProbability(cx, pool, V);
            if (progress >= random) {
                c = cx;
                break;
            }
        }
        return c;

    }


    private static double calculateProbability(ChromosomeX c, ChromosomeX[] population, double V){
        return c.fitnessFunction(V) / calculateTotalFitness(population, V);
    }

    private static double calculateTotalFitness(ChromosomeX[] pop, double V) {
        double totalFit = 0.0;
        for (ChromosomeX c : pop) {
            totalFit += c.fitnessFunction(V);
        }
        return totalFit;
    }

    private static ChromosomeX currentBest(ChromosomeX[] pool, double V) {
        ChromosomeX best = null;
        for (ChromosomeX c: pool) {
            if (best == null) best = c;
            else if (best.fitnessFunction(V) < c.fitnessFunction(V)) best = c;
        }
        System.out.print(" X: " + best.getX(0) + ", " + best.getX(1) + ", " + best.getX(2) + ", " + best.getX(3) +
                ", " + best.getX(4) + ", ");

        return best;
    }

    private static ChromosomeX[] generatePool(int poolSize , int variants, int X) {
        ChromosomeX[] chromosomePool = new ChromosomeX[poolSize];
        for (int i = 0; i < chromosomePool.length; i++) {
            ChromosomeX c = generateChromosome(variants, X);
            while (!c.checkConstraint(X)) {
                c = generateChromosome(variants, X);
            }
            chromosomePool[i] = c;
        }
        return chromosomePool;
    }


    private static ChromosomeX generateChromosome(int variants, int X) {
        SecureRandom random = new SecureRandom();
        ChromosomeX chromosome = new ChromosomeX();
        for (int j = 0; j < variants; j++) {
            chromosome.setX(random.nextInt(X-1) + 1, j);
        }
        return chromosome;
    }
}

class ChromosomeX {
    private int[] x = new int[5];

    public int getX(int pos) {
        return x[pos];
    }

    public void setX(int x, int pos) {
        this.x[pos] = x;
    }
    public boolean checkConstraint(int X) {
        int sum = 0;
        for (int num : x) {
            sum += num;
        }
        return sum == X;
    }

    public double fitnessFunction(double V) {
        double Z = 0;
        double a = 0.0000011;
        double b = 0.0021;
        short c = 0;
        for (int j : x) {
            Z += (a * j * j) + (b * j) + c + (1 / (V - j));
        }
        return 1 / Z;
    }
    public void mutate(int X) {
        SecureRandom rn = new SecureRandom();
        int pos = rn.nextInt(5);
        int random = rn.nextInt(X);
        setX(random, pos);
    }
}

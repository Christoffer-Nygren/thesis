public class AllTogether {
    private static final int[] l = {11000,22000,33000, 44000, 55000, 66000, 77000, 88000, 99000, 110000};
    private static final int generations = 5000;
    public static void main(String[] args) {
        int currentGen = 0;
        Chromosome4[] pool;
        Chromosome4[] topOnes = new Chromosome4[generations];
        for (int i = 0; i < l.length; i++) {
            pool = generatePool(l[i]);
            topOnes[currentGen] = bestGenerationalFitness(pool);
            System.out.println("Generation: " + currentGen + " " + printC(topOnes[currentGen]) + " Fitness: " + topOnes[currentGen].fitnessFunction());
            currentGen++;
            while (currentGen < generations) {
                pool = progressiveGeneration(pool);
                topOnes[currentGen] = bestGenerationalFitness(pool);
                System.out.println("Generation: " + currentGen + " " + printC(topOnes[currentGen]) + " Fitness: " + topOnes[currentGen].fitnessFunction());
                currentGen++;
            }
            currentGen = 0;
            Chromosome4 bestSolution = bestGenerationalFitness(topOnes);
            addToRegistry(bestSolution, i);
            topOnes = new Chromosome4[generations];
        }

    }

    private static Chromosome4[] progressiveGeneration(Chromosome4[] pool) {
        return new Chromosome4[0];
    }

    private static String printC(Chromosome4 topOne) {
        return null;
    }

    private static Chromosome4 bestGenerationalFitness(Chromosome4[] topOnes) {
        return null;
    }

    private static void addToRegistry(Chromosome4 bestSolution, int i) {

    }

    private static Chromosome4[] generatePool(int i) {
        return null;
    }
}

class Chromosome4 {

    public int fitnessFunction() {
        return 0;
    }
}

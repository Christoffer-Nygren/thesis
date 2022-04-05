import java.security.SecureRandom;
import java.text.DecimalFormat;

public class main {
    public static void main(String[] args) {
        int generations = 10000;
        int populationSize = 30;
        Chromosome[] pool = generatePop(populationSize);
        Chromosome[] bestOnes = new Chromosome[generations];
        Chromosome best;
        Chromosome absoluteBest = null;
        int bestGen = 0;
        int currentGen = 0;
        while (currentGen < generations) {
            pool = generateProgressivePop(pool);
            best = displayBestSolution(pool);
            bestOnes[currentGen] = best;
            System.out.println("Generation: " + currentGen + ", Current best fitness: " + calculateFitness(best) +
                    ", Variables A,B,C: " + best.getA() + "," + best.getB() + "," + best.getC() +
                    ", Probability: " + best.getProbability());
            currentGen++;
        }
        for (int i = 0; i < bestOnes.length; i++) {
            if (absoluteBest == null) absoluteBest = bestOnes[i];
            else if (calculateFitness(bestOnes[i]) > calculateFitness(absoluteBest)) {
                absoluteBest = bestOnes[i];
                bestGen = i;
            }
        }

        System.out.println("The best performer is.... " + calculateFitness(absoluteBest) + " A,B,C: " + absoluteBest.getA() +", " + absoluteBest.getB() + ", " + absoluteBest.getC() + " Gen: " + bestGen);
    }

    private static Chromosome displayBestSolution(Chromosome[] population) {
        Chromosome best = null;
        for (Chromosome chrom : population) {
            if (best == null) {
                best = chrom;
            } else if (calculateFitness(chrom) > calculateFitness(best)) {
                best = chrom;
            }
        }
        return best;
    }

    private static Chromosome[] generateProgressivePop(Chromosome[] population){
        Chromosome[] newPop = new Chromosome[population.length];
        for (int i = 0; i < newPop.length; i++) {
            Chromosome child = makeChild(population, 1);
            newPop[i] =  child;
        }
        return calculateProbability(newPop);
    }

    private static Chromosome[] calculateProbability(Chromosome[] population){
        double totalFit = 0.0;
        for (Chromosome c : population) {
            totalFit += calculateFitness(c);
        }
        for (Chromosome c : population) {
            c.setProbability(totalFit, calculateFitness(c));
        }
        return population;
    }

    private static Chromosome[] generatePop(int size) {
        DecimalFormat df = new DecimalFormat("#.######");
        SecureRandom rn = new SecureRandom();
        Chromosome[] population = new Chromosome[size];
        for (int i = 0; i < size; i++) {
            Chromosome chrom = new Chromosome();
            chrom.setA(Double.parseDouble(df.format(rn.nextDouble(100) + 1)));
            chrom.setB(Double.parseDouble(df.format(rn.nextDouble(101))));
            chrom.setC(Double.parseDouble(df.format(rn.nextDouble(101))));
            population[i] = chrom;
        }

        population = calculateProbability(population);
        return population;
    }

    private static Chromosome pickRandomParent(Chromosome[] population) {
        double random = Math.random();
        double progress = 0.0;
        Chromosome chromo = null;
        for (Chromosome c : population) {
            progress += c.getProbability();
            if (progress >= random) {
                chromo = c;
                break;
            }
        }
        return chromo;
    }


    private static double calculateFitness(Chromosome c) {
        DecimalFormat df = new DecimalFormat("#.#######");
        int workAmount = 10;
        return Double.parseDouble(df.format(1 / ((c.getA() * Math.pow(workAmount, 2)) + (c.getB() * workAmount) + c.getC())));
    }

    private static Chromosome makeChild(Chromosome[] population, int mutationRate) {
        Chromosome parent1 = pickRandomParent(population);
        Chromosome parent2 = pickRandomParent(population);
        SecureRandom rn = new SecureRandom();
        int mutation = rn.nextInt(101);
        int crossover = rn.nextInt(3);
        double[] parentVariables1 = {parent1.getA(), parent1.getB(), parent1.getC()};
        double[] parentVariables2 = {parent2.getA(), parent2.getB(), parent2.getC()};
        double[] newChild = new double[parentVariables1.length];
        Chromosome child = new Chromosome();
        for (int i = 0; i < parentVariables1.length; i++) {
            if (i < crossover) {
                newChild[i] = parentVariables1[i];
            } else {
                newChild[i] = parentVariables2[i];
            }
        }
        child.setA(newChild[0]);
        child.setB(newChild[1]);
        child.setC(newChild[2]);
        if (mutationRate <= mutation) {
            child = mutateChild(child);
        }
        return child;
    }

    private static Chromosome mutateChild(Chromosome child) {
        DecimalFormat df = new DecimalFormat("#.###");
        SecureRandom rn = new SecureRandom();
        int random = rn.nextInt(3);
        double[] mutatedChild = {child.getA(), child.getB(), child.getC()};
        mutatedChild[random] = Double.parseDouble(df.format(rn.nextDouble(101)));
        child = new Chromosome();
        child.setA(mutatedChild[0]);
        child.setB(mutatedChild[1]);
        child.setC(mutatedChild[2]);
        return child;
    }


}

class Chromosome {
    private double a = 0;
    private double b = 0;
    private double c = 0;
    private double probability = 0;

    public double getProbability() {
        return probability;
    }


    public void setProbability(double totalFitness, double fitness) {
        probability = fitness / totalFitness;
    }

    public double getC() {
        return c;
    }

    public void setC(double c) {
        this.c = c;
    }

    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }

    public double getA() {
        return a;
    }

    public void setA(double a) {
        this.a = a;
    }
}

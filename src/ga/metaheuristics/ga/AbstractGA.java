package ga.metaheuristics.ga;

import java.util.ArrayList;
import java.util.Random;

import ga.problems.Evaluator;
import ga.solutions.Solution;

/**
 * Abstract class for metaheuristic GA (Genetic Algorithms). It consider the
 * maximization of the chromosome fitness.
 * 
 * @author ccavellucci, fusberti
 * @param <G>
 *            Generic type of the chromosome element (genotype).
 * @param <F>
 *            Generic type of the candidate to enter the solution (fenotype).
 */
public abstract class AbstractGA<G extends Number, F> {

	@SuppressWarnings("serial")
	public class Chromosome extends ArrayList<G> {
	}

	@SuppressWarnings("serial")
	public class Population extends ArrayList<Chromosome> {
	}

	/**
	 * flag that indicates whether the code should print more information on
	 * screen
	 */
	public static boolean verbose = true;
	
	public static boolean isTTTPlotExecution = false;
    
    public static Double targetCost = 0.0;

	/**
	 * a random number generator
	 */
	public static final Random rng = new Random(0);

	/**
	 * the objective function being optimized
	 */
	protected Evaluator<F> ObjFunction;

	/**
	 * maximum number of generations being executed
	 */
	protected int generations;

	/**
	 * the size of the population
	 */
	protected int popSize;

	/**
	 * the size of the chromosome
	 */
	protected int chromosomeSize;

	/**
	 * the probability of performing a mutation
	 */
	protected double mutationRate;

	/**
	 * the best solution cost
	 */
	protected Double bestCost;
	
	//fitRate - diferenca em % do melhor fitness
	public double fitRate = 0.98; 
	
	//sizeRate - % máxima de diferença entre os membros da população
	public double sizeRate = 0.005;
        
    public String contrucao = "padrao";

	/**
	 * the best solution
	 */
	protected Solution<F> bestSol;

	/**
	 * the best chromosome, according to its fitness evaluation
	 */
	protected Chromosome bestChromosome;

	/**
	 * Creates a new solution which is empty, i.e., does not contain any
	 * candidate solution element.
	 * 
	 * @return An empty solution.
	 */
	public abstract Solution<F> createEmptySol();

	/**
	 * A mapping from the genotype (domain) to the fenotype (image). In other
	 * words, it takes a chromosome as input and generates a corresponding
	 * solution.
	 * 
	 * @param chromosome
	 *            The genotype being considered for decoding.
	 * @return The corresponding fenotype (solution).
	 */
	protected abstract Solution<F> decode(Chromosome chromosome);

	/**
	 * Generates a random chromosome according to some probability distribution
	 * (usually uniform).
	 * 
	 * @return A random chromosome.
	 */
	protected abstract Chromosome generateRandomChromosome();

	/**
	 * Determines the fitness for a given chromosome. The fitness should be a
	 * function strongly correlated to the objective function under
	 * consideration.
	 * 
	 * @param chromosome
	 *            The genotype being considered for fitness evaluation.
	 * @return The fitness value for the input chromosome.
	 */
	protected abstract Double fitness(Chromosome chromosome);

	/**
	 * Mutates a given locus of the chromosome. This method should be preferably
	 * called with an expected frequency determined by the {@link #mutationRate}.
	 * 
	 * @param chromosome
	 *            The genotype being mutated.
	 * @param locus
	 *            The position in the genotype being mutated.
	 */
	protected abstract void mutateGene(Chromosome chromosome, Integer locus);

	/**
	 * The constructor for the GA class.
	 * 
	 * @param objFunction
	 *            The objective function being optimized.
	 * @param generations
	 *            Number of generations to be executed.
	 * @param popSize
	 *            Population size.
	 * @param mutationRate
	 *            The mutation rate.
	 */
	public AbstractGA(Evaluator<F> objFunction, Integer generations, Integer popSize, Double mutationRate) {
		
		//System.out.print("FitRate:"+ this.fitRate+"\t");
		//System.out.print("sizeRate:"+ this.sizeRate+"\t");
		this.ObjFunction = objFunction;
		this.generations = generations;
	
		int logL = 7 * (int)(Math.log(this.ObjFunction.getDomainSize())/ Math.log(2) );

		this.popSize = ((logL % 2 == 0)?logL:logL+1);
		//System.out.print("Populacao:"+ this.popSize+"\t");
		this.chromosomeSize = this.ObjFunction.getDomainSize();
		this.mutationRate = mutationRate;
	}

	/**
	 * The GA mainframe. It starts by initializing a population of chromosomes.
	 * It then enters a generational loop, in which each generation goes the
	 * following steps: parent selection, crossover, mutation, population update
	 * and best solution update.
	 * 
	 * @return The best feasible solution obtained throughout all iterations.
	 */
	public Solution<F> solve() {

		/* starts the initial population */
		Population population = Correct(initializePopulation());
		
		//System.out.println(population.toString());
		
		bestChromosome = getBestChromosome(population);
		bestSol = decode(bestChromosome);
		//System.out.println("(Gen. " + 0 + ") BestSol = " + bestSol);

		/*
		 * enters the main loop and repeats until a given number of generations
		 */
                
        long endTime = System.currentTimeMillis() + 120000;
                
        for (int g = 1; g <= generations && System.currentTimeMillis() < endTime; g++) {
            Population parents = selectParents(population);

            Population offsprings = Correct(crossover(parents));

            Population mutants = Correct(mutate(offsprings));

            Population newpopulation = selectPopulation(mutants);

            if (contrucao.equals("padrao")) {
                newpopulation = Correct(newpopulation);
            } else
                newpopulation = Correct(diversifyPopulation(newpopulation));

            population = newpopulation;

            bestChromosome = getBestChromosome(population);

            if (fitness(bestChromosome) > bestSol.cost) {
                bestSol = decode(bestChromosome);
                // if (verbose)
                // System.out.println("(Gen. " + g + ") BestSol = " + bestSol);
            }
            
            if(isTTTPlotExecution && targetCost <= bestSol.cost) {
                return bestSol;
            }

        }

		return bestSol;
	}

	/**
	 * Randomly generates an initial population to start the GA.
	 * 
	 * @return A population of chromosomes.
	 */
	protected Population initializePopulation() {

		Population population = new Population();

		while (population.size() < popSize) {
			population.add(generateRandomChromosome());
		}

		return population;

	}

	/**
	 * Given a population of chromosome, takes the best chromosome according to
	 * the fitness evaluation.
	 * 
	 * @param population
	 *            A population of chromosomes.
	 * @return The best chromosome among the population.
	 */
	protected Chromosome getBestChromosome(Population population) {

		double bestFitness = Double.NEGATIVE_INFINITY;
		Chromosome bestChromosome = null;
		for (Chromosome c : population) {
			double fitness = fitness(c);
			if (fitness > bestFitness) {
				bestFitness = fitness;
				bestChromosome = c;
			}
		}

		return bestChromosome;
	}

	/**
	 * Given a population of chromosome, takes the worst chromosome according to
	 * the fitness evaluation.
	 * 
	 * @param population
	 *            A population of chromosomes.
	 * @return The worst chromosome among the population.
	 */
	protected Chromosome getWorseChromosome(Population population) {

		double worseFitness = Double.POSITIVE_INFINITY;
		Chromosome worseChromosome = null;
		for (Chromosome c : population) {
			double fitness = fitness(c);
			if (fitness < worseFitness) {
				worseFitness = fitness;
				worseChromosome = c;
			}
		}

		return worseChromosome;
	}

	/**
	 * Selection of parents for crossover using the tournament method. Given a
	 * population of chromosomes, randomly takes two chromosomes and compare
	 * them by their fitness. The best one is selected as parent. Repeat until
	 * the number of selected parents is equal to {@link #popSize}.
	 * 
	 * @param population
	 *            The current population.
	 * @return The selected parents for performing crossover.
	 */
	protected Population selectParents(Population population) {

		Population parents = new Population();

		while (parents.size() < popSize) {
			int index1 = rng.nextInt(popSize);
			Chromosome parent1 = population.get(index1);
			int index2 = rng.nextInt(popSize);
			Chromosome parent2 = population.get(index2);
			if (fitness(parent1) > fitness(parent2)) {
				parents.add(parent1);
			} else {
				parents.add(parent2);
			}
		}

		return parents;

	}

	/**
	 * The crossover step takes the parents generated by {@link #selectParents}
	 * and recombine their genes to generate new chromosomes (offsprings). The
	 * method being used is the 2-point crossover, which randomly selects two
	 * locus for being the points of exchange (P1 and P2). For example:
	 * 
	 *                        P1            P2
	 *    Parent 1: X1 ... Xi | Xi+1 ... Xj | Xj+1 ... Xn
	 *    Parent 2: Y1 ... Yi | Yi+1 ... Yj | Yj+1 ... Yn
	 * 
	 * Offspring 1: X1 ... Xi | Yi+1 ... Yj | Xj+1 ... Xn
	 * Offspring 2: Y1 ... Yi | Xi+1 ... Xj | Yj+1 ... Yn
	 * 
	 * @param parents
	 *            The selected parents for crossover.
	 * @return The resulting offsprings.
	 */
	protected Population crossover(Population parents) {

		Population offsprings = new Population();

		for (int i = 0; i < popSize; i = i + 2) {

			Chromosome parent1 = parents.get(i);
			Chromosome parent2 = parents.get(i + 1);

			int crosspoint1 = rng.nextInt(chromosomeSize + 1);
			int crosspoint2 = crosspoint1 + rng.nextInt((chromosomeSize + 1) - crosspoint1);

			Chromosome offspring1 = new Chromosome();
			Chromosome offspring2 = new Chromosome();

			for (int j = 0; j < chromosomeSize; j++) {
				if (j >= crosspoint1 && j < crosspoint2) {
					offspring1.add(parent2.get(j));
					offspring2.add(parent1.get(j));
				} else {
					offspring1.add(parent1.get(j));
					offspring2.add(parent2.get(j));
				}
			}

			offsprings.add(offspring1);
			offsprings.add(offspring2);

		}

		return offsprings;

	}

	/**
	 * The mutation step takes the offsprings generated by {@link #crossover}
	 * and to each possible locus, perform a mutation with the expected
	 * frequency given by {@link #mutationRate}.
	 * 
	 * @param offsprings
	 *            The offsprings chromosomes generated by the
	 *            {@link #crossover}.
	 * @return The mutated offsprings.
	 */
	protected Population mutate(Population offsprings) {

		for (Chromosome c : offsprings) {
			for (int locus = 0; locus < chromosomeSize; locus++) {
				if (rng.nextDouble() < mutationRate) {
					mutateGene(c, locus);
				}
			}
		}

		return offsprings;
	}

	/**
	 * Updates the population that will be considered for the next GA
	 * generation. The method used for updating the population is the elitist,
	 * which simply takes the worse chromosome from the offsprings and replace
	 * it with the best chromosome from the previous generation.
	 * 
	 * @param offsprings
	 *            The offsprings generated by {@link #crossover}.
	 * @return The updated population for the next generation.
	 */
	protected Population selectPopulation(Population offsprings) {

		Chromosome worse = getWorseChromosome(offsprings);
		if (fitness(worse) < fitness(bestChromosome)) {
			offsprings.remove(worse);
			offsprings.add(bestChromosome);
		}

		return offsprings;
	}
	
	public Population Correct(Population p){
        for(int i = 0; i < p.size(); i++){
            Chromosome c = p.get(i);
            
            for(int j = 0; j < c.size() - 1; j++){
                if(c.get(j).intValue() == 1 && c.get(j + 1).intValue() == 1){                           
                    Integer num = 0, num2 = 1;              
                    c.set(j, (G) num);
                    
                    Double a = fitness(c);
                    
                    c.set(j, (G) num2);
                    c.set(j + 1, (G) num);
                    
                    Double b = fitness(c);
                    
                    if(a > b){
                        c.set(j, (G) num);
                        c.set(j + 1, (G) num2);
                    } else {
                        c.set(j, (G) num2);
                        c.set(j + 1, (G) num);
                    }
                }
            }
            
            p.set(i, c);
        }
                        
        return p;
    }
	// fazer pela variancia do fitness
	//se tiver alta, entao tem muita diversidade
	//se tiver baixa, entao tem pouca diversidade
	
	//olhar a taxa de mutacao por 1/l
	
	public Population diversifyPopulation(Population p){
		Random rnd = new Random();
		
		double fit = fitness(getBestChromosome(p));

		
		for(int i = 0; i < p.size(); i++){
			if(fitness(p.get(i)) / fit >= fitRate) continue;
			
			for(int j = i + 1; j < p.size(); j++){				
				int cont = 0;
				
				for(int k = 0; k < p.get(i).size(); k++){
					if(p.get(i).get(k).intValue() != p.get(j).get(k).intValue()){
						cont++;
						
						if(cont >= sizeRate * ObjFunction.getDomainSize())
							break;
					}
				}
				
				if(cont < sizeRate * ObjFunction.getDomainSize()){;
					mutateGene(p.get(i), rnd.nextInt(ObjFunction.getDomainSize()));
				}
			}
		}
		//System.out.println("Tivemos "+count+" gemeos de "+ popSize+" da populacao");
		return p;
	}

}

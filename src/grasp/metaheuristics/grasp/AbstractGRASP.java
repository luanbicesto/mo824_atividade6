/**
 *
 */
package grasp.metaheuristics.grasp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import grasp.problems.Evaluator;
import grasp.solutions.Solution;

/**
 * Abstract class for metaheuristic GRASP (Greedy Randomized Adaptive Search
 * Procedure). It consider a minimization problem.
 *
 * @author ccavellucci, fusberti
 * @param <E> Generic type of the element which composes the solution.
 */
public abstract class AbstractGRASP<E> {

    /**
     * flag that indicates whether the code should print more information on
     * screen
     */
    public static boolean verbose = false;

    /**
     * a random number generator
     */
    protected static Random rng = new Random(0);
    
    protected static boolean isTTTPlotExecution = false;
    
    protected static Double targetCost = 0.0;

    /**
     * the objective function being optimized
     */
    protected Evaluator<E> ObjFunction;

    /**
     * the GRASP greediness-randomness parameter
     */
    protected Double alpha;

    /**
     * the best solution cost
     */
    protected Double bestCost;

    /**
     * the incumbent solution cost
     */
    protected Double incumbentCost;

    /**
     * the best solution
     */
    protected Solution<E> bestSol;

    /**
     * the best solution
     */
    protected Solution<E> partialSol;
    
    /**
     * the incumbent solution
     */
    protected Solution<E> incumbentSol;

    /**
     * the number of iterations the GRASP main loop executes.
     */
    protected Integer iterations;

    /**
     * the Candidate List of elements to enter the solution.
     */
    protected ArrayList<E> CL;

    /**
     * the Restricted Candidate List of elements to enter the solution.
     */
    protected ArrayList<E> RCL;

    /**
     * Creates the Candidate List, which is an ArrayList of candidate elements
     * that can enter a solution.
     *
     * @return The Candidate List.
     */
    public abstract ArrayList<E> makeCL();

    /**
     * Creates the Restricted Candidate List, which is an ArrayList of the best
     * candidate elements that can enter a solution. The best candidates are
     * defined through a quality threshold, delimited by the GRASP
     * {@link #alpha} greedyness-randomness parameter.
     *
     * @return The Restricted Candidate List.
     */
    public abstract ArrayList<E> makeRCL();

    /**
     * Updates the Candidate List according to the incumbent solution
     * {@link #incumbentSol}. In other words, this method is responsible for
     * updating which elements are still viable to take part into the solution.
     */
    public abstract void updateCL();

    /**
     * Creates a new solution which is empty, i.e., does not contain any
     * element.
     *
     * @return An empty solution.
     */
    public abstract Solution<E> createEmptySol();

    /**
     * The GRASP local search phase is responsible for repeatedly applying a
     * neighborhood operation while the solution is getting improved, i.e.,
     * until a local optimum is attained.
     *
     * @return An local optimum solution.
     */
    public abstract Solution<E> localSearch();

    /**
     * Constructor for the AbstractGRASP class.
     *
     * @param objFunction The objective function being minimized.
     * @param alpha The GRASP greediness-randomness parameter (within the range
     * [0,1])
     * @param iterations The number of iterations which the GRASP will be
     * executed.
     */
    public AbstractGRASP(Evaluator<E> objFunction, Double alpha, Integer iterations) {
        this.ObjFunction = objFunction;
        this.alpha = alpha;
        this.iterations = iterations;
    }

    /**
     * The GRASP constructive heuristic, which is responsible for building a
     * feasible solution by selecting in a greedy-random fashion, candidate
     * elements to enter the solution.
     *
     * @return A feasible solution to the problem being minimized.
     */
    public Solution<E> constructiveHeuristic() {
        boolean bias = false;
        
        CL = makeCL();
        RCL = makeRCL();

        incumbentSol = createEmptySol();
        incumbentCost = Double.POSITIVE_INFINITY;

        /* Main loop, which repeats until the stopping criteria is reached. */
        while (!constructiveStopCriteria()) {
            double maxCost = Double.NEGATIVE_INFINITY, minCost = Double.POSITIVE_INFINITY;
            incumbentCost = ObjFunction.evaluate(incumbentSol);
            updateCL();

            /*
			 * Explore all candidate elements to enter the solution, saving the
			 * highest and lowest cost variation achieved by the candidates.
             */
            for (E c : CL) {
                Double deltaCost = ObjFunction.evaluateInsertionCost(c, incumbentSol);
                if (deltaCost < minCost) {
                    minCost = deltaCost;
                }
                if (deltaCost > maxCost) {
                    maxCost = deltaCost;
                }
            }

            /*
			 * Among all candidates, insert into the RCL those with the highest
			 * performance using parameter alpha as threshold.
             */
            Double countBias = 0.0; //Bias
            Double it = 0.0;
            
            for (E c : CL) {
                Double deltaCost = ObjFunction.evaluateInsertionCost(c, incumbentSol);

                if (deltaCost <= minCost + alpha * (maxCost - minCost)) {
                    RCL.add(c);

                    it = it + 1.0;
                    countBias += 1.0 / it;
                }
            }

            
            
            if (bias == true) {
                Collections.sort((List<Double>) RCL);
                Collections.reverse(RCL);
            }

            if (RCL.size() <= 0) {
                continue;
            }
            
            E inCand = RCL.get(0);

            /* Choose a candidate randomly from the RCL */
            if (bias == true) {
                Double rndIndex = rng.nextDouble(); //bias
                inCand = RCL.get(0); //bias

                it = 0.0;

                for (int i = 0; i < RCL.size(); i++) {// Bias
                    it = it + (1.0 / (RCL.size() - i)) / countBias;

                    if (rndIndex < it) {
                        inCand = RCL.get(i);
                        break;
                    }
                }
            } else {
                Integer rndIndex = rng.nextInt(RCL.size());
                inCand = RCL.get(rndIndex);
            }

            CL.remove(inCand);
            incumbentSol.add(inCand);

            ObjFunction.evaluate(incumbentSol);
            RCL.clear();

        }

        return incumbentSol;
    }

    /**
     * The GRASP mainframe. It consists of a loop, in which each iteration goes
     * through the constructive heuristic and local search. The best solution is
     * returned as result.
     *
     * @return The best feasible solution obtained throughout all iterations.
     */
    public Solution<E> solve() {
        long end = System.currentTimeMillis()+ 120000;

        bestSol = createEmptySol();
        partialSol = createEmptySol();
        
        for (int i = 0; System.currentTimeMillis() < end; i++) {
            constructiveHeuristic();
            
            if(partialSol.cost > incumbentSol.cost)
                partialSol = new Solution<E>(incumbentSol);
            
            localSearch();
            if (bestSol.cost > incumbentSol.cost) {
                bestSol = new Solution<E>(incumbentSol);
                if (verbose) {
                    System.out.println("(Iter. " + i + ") BestSol = " + bestSol);
                }
            }
            
            if(isTTTPlotExecution && targetCost >= bestSol.cost) {
                return bestSol;
            }
        }

        //System.out.println("BestConstructive = " + partialSol);
                
        return bestSol;
    }

    /**
     * A standard stopping criteria for the constructive heuristic is to repeat
     * until the incumbent solution improves by inserting a new candidate
     * element.
     *
     * @return true if the criteria is met.
     */
    public Boolean constructiveStopCriteria() {
        return (incumbentCost > incumbentSol.cost) ? false : true;
    }

}

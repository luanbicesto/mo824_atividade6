/**
 * 
 */
package tabusearchheuristic.metaheuristics.tabusearch;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Random;

import tabusearchheuristic.problems.Evaluator;
import tabusearchheuristic.solutions.Solution;

/**
 * Abstract class for metaheuristic Tabu Search. It consider a minimization problem.
 * 
 * @author ccavellucci, fusberti
 * @param <E>
 *            Generic type of the candidate to enter the solution.
 */
public abstract class AbstractTS<E> {

	/**
	 * flag that indicates whether the code should print more information on
	 * screen
	 */
	public static boolean verbose = false;
	
	protected static boolean isTTTPlotExecution = false;
    
    protected static Double targetCost = 0.0;

	/**
	 * a random number generator
	 */
	protected static Random rng = new Random(0);

	/**
	 * the objective function being optimized
	 */
	protected Evaluator<E> ObjFunction;

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
	 * the incumbent solution
	 */
	protected Solution<E> incumbentSol;

	/**
	 * the number of iterations the TS main loop executes.
	 */
	protected Integer iterations;
	
	/**
	 * the tabu tenure.
	 */
	protected Integer tenure;

	/**
	 * the Candidate List of elements to enter the solution.
	 */
	protected ArrayList<E> CL;

	/**
	 * the Restricted Candidate List of elements to enter the solution.
	 */
	protected ArrayList<E> RCL;
	
	/**
	 * the Tabu List of elements to enter the solution.
	 */
	protected ArrayDeque<E> TL;
	
	protected int numberOfIterationsToStartIntensification;
	
	protected int numberOfIterationsOfIntensification;
	
	protected enum STATUS {ACTIVE, DEACTIVE};
	
	protected STATUS statusIntensificationProcess;
	
	private int countIterationsStartIntensification;
	
	private int countIterationsOfIntensification;
	
	private static final int TOTAL_EXECUTION_TIME = 120;

	/**
	 * Creates the Candidate List, which is an ArrayList of candidate elements
	 * that can enter a solution.
	 * 
	 * @return The Candidate List.
	 */
	public abstract ArrayList<E> makeCL();

	/**
	 * Creates the Restricted Candidate List, which is an ArrayList of the best
	 * candidate elements that can enter a solution. 
	 * 
	 * @return The Restricted Candidate List.
	 */
	public abstract ArrayList<E> makeRCL();
	
	/**
	 * Creates the Tabu List, which is an ArrayDeque of the Tabu
	 * candidate elements. The number of iterations a candidate
	 * is considered tabu is given by the Tabu Tenure {@link #tenure}
	 * 
	 * @return The Tabu List.
	 */
	public abstract ArrayDeque<E> makeTL();

	/**
	 * Updates the Candidate List according to the incumbent solution
	 * {@link #incumbentSol}. In other words, this method is responsible for
	 * updating the costs of the candidate solution elements.
	 */
	public abstract void updateCL();

	/**
	 * Creates a new solution which is empty, i.e., does not contain any
	 * candidate solution element.
	 * 
	 * @return An empty solution.
	 */
	public abstract Solution<E> createEmptySol();

	/**
	 * The TS local search phase is responsible for repeatedly applying a
	 * neighborhood operation while the solution is getting improved, i.e.,
	 * until a local optimum is attained. When a local optimum is attained
	 * the search continues by exploring moves which can make the current 
	 * solution worse. Cycling is prevented by not allowing forbidden
	 * (tabu) moves that would otherwise backtrack to a previous solution.
	 * 
	 * @return An local optimum solution.
	 */
	public abstract Solution<E> neighborhoodMove();
	
	public abstract void updateIntensificationByRestartCounter();
	
	public abstract void resetIntensificationStructures();
	
	public abstract void setFixedComponentsIntensification();
	
	public void initIntensificationByRestart() {
	    incumbentSol = new Solution<E>(bestSol);
	    updateCL();
	    setFixedComponentsIntensification();
	}

	/**
	 * Constructor for the AbstractTS class.
	 * 
	 * @param objFunction
	 *            The objective function being minimized.
	 * @param tenure
	 *            The Tabu tenure parameter. 
	 * @param iterations
	 *            The number of iterations which the TS will be executed.
	 */
	public AbstractTS(Evaluator<E> objFunction, Integer tenure, Integer iterations
	        ,int numberOfIterationsToStartIntensification, int numberOfIterationsOfIntensification) {
		this.ObjFunction = objFunction;
		this.tenure = tenure;
		this.iterations = iterations;
		this.numberOfIterationsOfIntensification = numberOfIterationsOfIntensification;
		this.numberOfIterationsToStartIntensification = numberOfIterationsToStartIntensification;
		this.countIterationsOfIntensification = 0;
		this.countIterationsStartIntensification = 0;
		this.statusIntensificationProcess = STATUS.DEACTIVE;
	}

	/**
	 * The TS constructive heuristic, which is responsible for building a
	 * feasible solution by selecting in a greedy fashion, candidate
	 * elements to enter the solution.
	 * 
	 * @return A feasible solution to the problem being minimized.
	 */
	public Solution<E> constructiveHeuristic() {

		CL = makeCL();
		RCL = makeRCL();
		incumbentSol = createEmptySol();
		incumbentCost = Double.POSITIVE_INFINITY;

		/* Main loop, which repeats until the stopping criteria is reached. */
		while (!constructiveStopCriteria()) {

			Double maxCost = Double.NEGATIVE_INFINITY, minCost = Double.POSITIVE_INFINITY;
			incumbentCost = incumbentSol.cost;
			//updateCL();

			/*
			 * Explore all candidate elements to enter the solution, saving the
			 * highest and lowest cost variation achieved by the candidates.
			 */
			for (E c : CL) {
				Double deltaCost = ObjFunction.evaluateInsertionCost(c, incumbentSol);
				if (deltaCost < minCost)
					minCost = deltaCost;
				if (deltaCost > maxCost)
					maxCost = deltaCost;
			}

			/*
			 * Among all candidates, insert into the RCL those with the highest
			 * performance.
			 */
			for (E c : CL) {
				Double deltaCost = ObjFunction.evaluateInsertionCost(c, incumbentSol);
				if (deltaCost <= minCost) {
					RCL.add(c);
				}
			}
		
			/* Choose a candidate randomly from the RCL */
			int rndIndex = rng.nextInt(RCL.size());
			E inCand = RCL.get(rndIndex);
			CL.remove(inCand);
			incumbentSol.add(inCand);
			ObjFunction.evaluate(incumbentSol);
			RCL.clear();

		}

		return incumbentSol;
	}

	/**
	 * The TS mainframe. It consists of a constructive heuristic followed by
	 * a loop, in which each iteration a neighborhood move is performed on
	 * the current solution. The best solution is returned as result.
	 * 
	 * @return The best feasible solution obtained throughout all iterations.
	 */
	public Solution<E> solve() {

	    long startTime = System.nanoTime();
	    int i = 0;
	    
		bestSol = createEmptySol();
		constructiveHeuristic();
		TL = makeTL();
		
		while(calculateElapsedTimeSeconds(startTime) < TOTAL_EXECUTION_TIME
		        && i < iterations) {
		    i++;
			neighborhoodMove();
			if (bestSol.cost > incumbentSol.cost) {
				bestSol = new Solution<E>(incumbentSol);
				if (verbose)
					System.out.println((statusIntensificationProcess == STATUS.ACTIVE ? "INTENSIFICATION - " : "") 
				    + "(Iter. " + i + ") BestSol = " + bestSol);
			}
			
			if(isTTTPlotExecution && targetCost >= bestSol.cost) {
                return bestSol;
            }
			
			applyIntensificationByRestart();
		}

		return bestSol;
	}
	
	private long calculateElapsedTimeSeconds(long startTime) {
	    return (System.nanoTime() - startTime) / 1000000000;
	}
	
	private void applyIntensificationByRestart() {
	    if(statusIntensificationProcess == STATUS.DEACTIVE) {
            countIterationsStartIntensification++;
            updateIntensificationByRestartCounter();
        } else {
            countIterationsOfIntensification++;
        }
        
        if(countIterationsStartIntensification == numberOfIterationsToStartIntensification) {
            //start intensification process
            statusIntensificationProcess = STATUS.ACTIVE;
            countIterationsStartIntensification = 0;
            initIntensificationByRestart();
        }
        
        if(countIterationsOfIntensification == numberOfIterationsOfIntensification) {
            //end intensification process
            statusIntensificationProcess = STATUS.DEACTIVE;
            countIterationsOfIntensification = 0;
            resetIntensificationStructures();
        }
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

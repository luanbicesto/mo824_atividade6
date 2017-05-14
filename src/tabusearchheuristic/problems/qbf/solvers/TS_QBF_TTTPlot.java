package tabusearchheuristic.problems.qbf.solvers;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import tabusearchheuristic.metaheuristics.tabusearch.AbstractTS;
import tabusearchheuristic.problems.qbf.QBF_Inverse;
import tabusearchheuristic.solutions.Solution;



/**
 * Metaheuristic TS (Tabu Search) for obtaining an optimal solution to a QBF
 * (Quadractive Binary Function -- {@link #QuadracticBinaryFunction}).
 * Since by default this TS considers minimization problems, an inverse QBF
 *  function is adopted.
 * 
 * @author ccavellucci, fusberti
 */
public class TS_QBF_TTTPlot extends AbstractTS<Integer> {
	
	private final Integer fake = new Integer(-1);
	
	private static final int TTTPlot_ITERATIONS = 1;
	
	//private static final int INTERATIONS_TO_START_INTENSIFICATION = 800;
	
	//private static final int INTERATIONS_OF_INTENSIFICATION = 400;
	
	//private static final int PERCENTAGE_FIXED_ITENS = 20;
	private int percentageFixedItems;
	
	private enum OperationNeighborhood {INSERT, REMOVE, EXCHANGE};
	
	private enum SearchStrategy {BEST_IMPROVING, FIRST_IMPROVING};
	
	private SearchStrategy searchStrategy = SearchStrategy.FIRST_IMPROVING;
	
	private ArrayDeque<Integer> tlRemovedRandomItens;
	
	private Map<Integer, Integer> intensificationByRestartCounter;
	
	private Set<Integer> fixedVariablesIntensification;

	/**
	 * Constructor for the TS_QBF class. An inverse QBF objective function is
	 * passed as argument for the superclass constructor.
	 * 
	 * @param tenure
	 *            The Tabu tenure parameter.
	 * @param iterations
	 *            The number of iterations which the TS will be executed.
	 * @param filename
	 *            Name of the file for which the objective function parameters
	 *            should be read.
	 * @throws IOException
	 *             necessary for I/O operations.
	 */
    public TS_QBF_TTTPlot(Integer tenure, int interationsToStartIntensification, int interationsOfIntensification,
            int percentageFixedItems, Integer iterations, String filename) throws IOException {
        super(new QBF_Inverse(filename), tenure, iterations, interationsToStartIntensification,
                interationsOfIntensification);
        tlRemovedRandomItens = new ArrayDeque<>();
        intensificationByRestartCounter = new HashMap<>();
        fixedVariablesIntensification = new HashSet<>();
        this.percentageFixedItems = percentageFixedItems;
    }

	/* (non-Javadoc)
	 * @see metaheuristics.tabusearch.AbstractTS#makeCL()
	 */
	@Override
	public ArrayList<Integer> makeCL() {

		ArrayList<Integer> _CL = new ArrayList<Integer>();
		for (int i = 0; i < ObjFunction.getDomainSize(); i++) {
			Integer cand = new Integer(i);
			_CL.add(cand);
		}

		return _CL;

	}

	/* (non-Javadoc)
	 * @see metaheuristics.tabusearch.AbstractTS#makeRCL()
	 */
	@Override
	public ArrayList<Integer> makeRCL() {

		ArrayList<Integer> _RCL = new ArrayList<Integer>();

		return _RCL;

	}
	
	/* (non-Javadoc)
	 * @see metaheuristics.tabusearch.AbstractTS#makeTL()
	 */
	@Override
	public ArrayDeque<Integer> makeTL() {

		ArrayDeque<Integer> _TS = new ArrayDeque<Integer>(2*tenure);
		for (int i=0; i<2*tenure; i++) {
			_TS.add(fake);
		}

		return _TS;

	}

	/* (non-Javadoc)
	 * @see metaheuristics.tabusearch.AbstractTS#updateCL()
	 */
	@Override
	public void updateCL() {
	    CL = makeCL();
	    CL.removeAll(incumbentSol);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * This createEmptySol instantiates an empty solution and it attributes a
	 * zero cost, since it is known that a QBF solution with all variables set
	 * to zero has also zero cost.
	 */
	@Override
	public Solution<Integer> createEmptySol() {
		Solution<Integer> sol = new Solution<Integer>();
		sol.cost = 0.0;
		return sol;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * The local search operator developed for the QBF objective function is
	 * composed by the neighborhood moves Insertion, Removal and 2-Exchange.
	 */
	@Override
	public Solution<Integer> neighborhoodMove() {
		Pair<Integer, Integer> bestCandidates = new Pair<Integer, Integer>(fake, fake);
		//searchStrategy = Double.compare(rng.nextDouble(), 0.6) <= 0 ? SearchStrategy.FIRST_IMPROVING : SearchStrategy.BEST_IMPROVING;
		
		if(searchStrategy == SearchStrategy.FIRST_IMPROVING) {
		    bestCandidates = firstImprovingSearch();
		}
		
        if (searchStrategy == SearchStrategy.BEST_IMPROVING
                || (searchStrategy == SearchStrategy.FIRST_IMPROVING && bestCandidates.getLeft() == fake && bestCandidates.getRight() == fake)) {
            bestCandidates = bestImprovingSearch();
        }

		// Implement the best non-tabu move
		TL.poll();
		if (bestCandidates.getRight() != fake) {
			incumbentSol.remove(bestCandidates.getRight());
			CL.add(bestCandidates.getRight());
			TL.add(bestCandidates.getRight());
		} else {
			TL.add(fake);
		}
		TL.poll();
		if (bestCandidates.getLeft() != fake) {
			incumbentSol.add(bestCandidates.getLeft());
			CL.remove(bestCandidates.getLeft());
			TL.add(bestCandidates.getLeft());
		} else {
			TL.add(fake);
		}
		
		tlRemovedRandomItens.clear();
		repair();
		
		return null;
	}
	
	private Pair<Integer, Integer> bestImprovingSearch() {
	    Double minDeltaCost;
	    Integer bestCandIn = fake;
	    Integer bestCandOut = fake;
	    
	    minDeltaCost = Double.POSITIVE_INFINITY;
        //updateCL();
        // Evaluate insertions
        for (Integer candIn : CL) {
            Double deltaCost = ObjFunction.evaluateInsertionCost(candIn, incumbentSol);
            if (evaluationAllowed(candIn, OperationNeighborhood.INSERT)) {
                if (deltaCost < minDeltaCost) {
                    minDeltaCost = deltaCost;
                    bestCandIn = candIn;
                    bestCandOut = fake;
                }
            }
        }
        // Evaluate removals
        for (Integer candOut : incumbentSol) {
            Double deltaCost = ObjFunction.evaluateRemovalCost(candOut, incumbentSol);
            if (evaluationAllowed(candOut, OperationNeighborhood.REMOVE)) {
                if (deltaCost < minDeltaCost) {
                    minDeltaCost = deltaCost;
                    bestCandIn = fake;
                    bestCandOut = candOut;
                }
            }
        }
        // Evaluate exchanges
        for (Integer candIn : CL) {
            for (Integer candOut : incumbentSol) {
                Double deltaCost = ObjFunction.evaluateExchangeCost(candIn, candOut, incumbentSol);
                if (evaluationAllowed(candIn, candOut)) {
                    if (deltaCost < minDeltaCost) {
                        minDeltaCost = deltaCost;
                        bestCandIn = candIn;
                        bestCandOut = candOut;
                    }
                }
            }
        }
        
        return new Pair<Integer, Integer>(bestCandIn, bestCandOut);
	}
	
	private Pair<Integer, Integer> firstImprovingSearch() {
	    ArrayList<Pair<Integer, Integer>> allMoviments = new ArrayList<>();
	    
	    generateIndexesForInsertion(allMoviments);
	    generateIndexesForRemoval(allMoviments);
	    generateIndexesForExchange(allMoviments);
	    Collections.shuffle(allMoviments, rng);
	    
	    return firstImprovingCheckMoviments(allMoviments);
	}
	
    private Pair<Integer, Integer> firstImprovingCheckMoviments(ArrayList<Pair<Integer, Integer>> allMoviments) {
        Integer bestCandIn = fake;
        Integer bestCandOut = fake;
        
        for (Pair<Integer, Integer> moviment : allMoviments) {
            if (isInsertionMovimentFI(moviment)) {
                if (applyInsertionMoviment(moviment.getLeft())) {
                    bestCandIn = moviment.getLeft();
                    break;
                }
            } else if (isRemovalMovimentFI(moviment)) {
                if (applyRemovalMoviment(moviment.getRight())) {
                    bestCandOut = moviment.getRight();
                    break;
                }
            } else { // exchange
                if (applyExchangeMoviment(moviment.getLeft(), moviment.getRight())) {
                    bestCandIn = moviment.getLeft();
                    bestCandOut = moviment.getRight();
                    break;
                }
            }
        }
        
        return new Pair<Integer, Integer>(bestCandIn, bestCandOut);
    }
	
	private boolean isInsertionMovimentFI(Pair<Integer, Integer> moviment) {
        return moviment.getRight() == fake;
    }
	
	private boolean isRemovalMovimentFI(Pair<Integer, Integer> moviment) {
	    return moviment.getLeft() == fake;
	}
	
	private boolean applyExchangeMoviment(Integer candIn, Integer candOut) {
	    Double deltaCost = ObjFunction.evaluateExchangeCost(candIn, candOut, incumbentSol);
        return evaluationAllowed(candIn, candOut) && deltaCost < 0;
    }

	private boolean applyInsertionMoviment(Integer candIn) {
	    Double deltaCost = ObjFunction.evaluateInsertionCost(candIn, incumbentSol);
        return evaluationAllowed(candIn, OperationNeighborhood.INSERT) && deltaCost < 0;
        
	}
	
	private boolean applyRemovalMoviment(Integer candOut) {
        Double deltaCost = ObjFunction.evaluateRemovalCost(candOut, incumbentSol);
        return evaluationAllowed(candOut, OperationNeighborhood.REMOVE) && deltaCost < 0;
    }
	
	private void generateIndexesForExchange(ArrayList<Pair<Integer, Integer>> allIndexes) {
	    for (Integer candIn : CL) {
            for (Integer candOut : incumbentSol) {
                Pair<Integer, Integer> newIndex = new Pair<Integer, Integer>(candIn, candOut);
                allIndexes.add(newIndex);
            }
	    }
    }
	
	private void generateIndexesForRemoval(ArrayList<Pair<Integer, Integer>> allIndexes) {
        for (Integer candOut : incumbentSol) {
            Pair<Integer, Integer> newIndex = new Pair<Integer, Integer>(fake, candOut);
            allIndexes.add(newIndex);
        }
    }
	
	private void generateIndexesForInsertion(ArrayList<Pair<Integer, Integer>> allIndexes) {
	    for (Integer candIn : CL) {
	        Pair<Integer, Integer> newIndex = new Pair<Integer, Integer>(candIn, fake);
	        allIndexes.add(newIndex);
	    }
	}
	
	public void updateIntensificationByRestartCounter() {
	    for(Integer element : incumbentSol) {
	        Integer elementCount = intensificationByRestartCounter.get(element.intValue());
	        
	        if(elementCount == null) {
	            intensificationByRestartCounter.put(element.intValue(), 1);
	        } else {
	            intensificationByRestartCounter.put(element.intValue(), elementCount.intValue() + 1);
	        }
	    }
	}
	
	public void resetIntensificationStructures() {
	    this.intensificationByRestartCounter.clear();
	    this.fixedVariablesIntensification.clear();
	}
	
	public void setFixedComponentsIntensification() {
	    fixedVariablesIntensification.clear();
	    int index = 0;
	    int numberFixedElements = 0;
	    
	    Map<Integer, Integer> orderedVariablesOcurrence = intensificationByRestartCounter.entrySet()
                                        .stream()
                                        .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                                        .collect(Collectors.toMap(
                                                Map.Entry::getKey, 
                                                Map.Entry::getValue, 
                                                (e1, e2) -> e1, 
                                                LinkedHashMap::new
                                              ));
	    
	    numberFixedElements = computeNumberFixedElements();
	    Iterator<Integer> iterator = orderedVariablesOcurrence.keySet().iterator();
	    while(iterator.hasNext() && index < numberFixedElements) {
	        fixedVariablesIntensification.add(iterator.next().intValue());
	        index++;
	    }
	}
	
	private int computeNumberFixedElements() {
	    return (int) Math.round(intensificationByRestartCounter.size() * ((double)this.percentageFixedItems/100));
	}

	private boolean evaluationAllowed(Integer candidate, OperationNeighborhood operation) {
	    if(statusIntensificationProcess == STATUS.ACTIVE 
	            && operation == OperationNeighborhood.REMOVE
	            && fixedVariablesIntensification.contains(candidate.intValue())) {
	        return false;
	    }
	    
	    return !(TL.contains(candidate) || tlRemovedRandomItens.contains(candidate)); //|| incumbentSol.cost+deltaCost < bestSol.cost;
	}
	
	private boolean evaluationAllowed(Integer candIn, Integer candOut) {
        return evaluationAllowed(candIn, OperationNeighborhood.INSERT) && evaluationAllowed(candOut, OperationNeighborhood.REMOVE);
    }
	
	private void repair() {
        randomizedSimplestRepair();
        ObjFunction.evaluate(incumbentSol);
    }
	
	private void randomizedSimplestRepair() {
        double removeCandIndexProb = 0;
        int removeCandIndex = 0;
        Solution<Integer> incumbentSolCopy = new Solution<Integer>(incumbentSol);
        sortSolution(incumbentSolCopy);
        
        /*Simplest repair: remove the right element that is incorrect*/
        for(int index = 0; index < incumbentSolCopy.size(); index++) {
            if(index < (incumbentSolCopy.size() - 1) && applyAdjacentConstraint(incumbentSolCopy, index)) {
                removeCandIndexProb = rng.nextDouble();
                removeCandIndex = Double.compare(removeCandIndexProb, 0.5) <= 0 ? index : index + 1;
                
                int indexElement = incumbentSol.indexOf(incumbentSolCopy.get(removeCandIndex));
                Integer element = incumbentSol.get(indexElement);
                CL.add(element);
                
                if(!tlRemovedRandomItens.contains(element)) {
                    tlRemovedRandomItens.add(incumbentSol.get(indexElement));
                }
                
                removeElementByValue(incumbentSol, incumbentSolCopy.get(removeCandIndex));
                incumbentSolCopy.remove(removeCandIndex);
                
                if(removeCandIndex == index) {
                    index--;
                }
            }
        }
    }
	
	private void removeElementByValue(Solution<Integer> solution, int targetValue) {
        for(int index = 0; index < solution.size(); index++) {
            if(solution.get(index).intValue() == targetValue) {
                solution.remove(index);
                break;
            }
        }
    }
	
	private boolean applyAdjacentConstraint(Solution<Integer> currentSolution, int currentIndex) {
        return currentSolution.get(currentIndex) + 1 == currentSolution.get(currentIndex+1);
    }
	
	private void sortSolution(Solution<Integer> sol) {
        sol.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer element1, Integer element2)
            {

                return  element1.compareTo(element2);
            }
        });
    }
	
	/**
	 * A main method used for testing the TS metaheuristic.
	 * 
	 */
	public static void main(String[] args) throws IOException {
	    isTTTPlotExecution = true;
	    TabuSearchParametersTTTPlot parameters = buildParameters(args);
	    targetCost = parameters.getTargetValue();
	    
	    for(int i = 0; i < TTTPlot_ITERATIONS; i++) {
            rng = new Random();
  
            long startTime = System.currentTimeMillis();          
            TS_QBF_TTTPlot tabusearch = new TS_QBF_TTTPlot(parameters.getTenure(), parameters.getInterationsToStartIntensification(),
                    parameters.getInterationsOfIntensification(), parameters.getPercentageFixeditems(), 500000, parameters.getInstanceName());
            tabusearch.solve();
            //System.out.println("Best " + bestSol);
            long endTime   = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            System.out.println((double)totalTime/(double)1000);
	    }
	}
	
	private static TabuSearchParametersTTTPlot buildParameters(String[] args) {
	    TabuSearchParametersTTTPlot parameters = new TabuSearchParametersTTTPlot();
	    
	    parameters.setInstanceName(args[0]);
	    parameters.setTenure(Integer.parseInt(args[1]));
	    parameters.setInterationsToStartIntensification(Integer.parseInt(args[2]));
	    parameters.setInterationsOfIntensification(Integer.parseInt(args[3]));
	    parameters.setPercentageFixeditems(Integer.parseInt(args[4]));
	    parameters.setTargetValue(Double.parseDouble(args[5]));
	    
	    /*parameters.setTenure(20);
	    parameters.setInstanceName("instances/qbf060");
        parameters.setInterationsToStartIntensification(800);
        parameters.setInterationsOfIntensification(400);
        parameters.setPercentageFixeditems(20);
        parameters.setTargetValue(-300.00);*/
        
	    return parameters;
	}

}

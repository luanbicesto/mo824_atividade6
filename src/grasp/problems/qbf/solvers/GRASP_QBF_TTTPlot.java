package grasp.problems.qbf.solvers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import grasp.metaheuristics.grasp.AbstractGRASP;
import grasp.problems.qbf.QBF_Inverse;
import grasp.solutions.Solution;

/**
 * Metaheuristic GRASP (Greedy Randomized Adaptive Search Procedure) for
 * obtaining an optimal solution to a QBF (Quadractive Binary Function --
 * {@link #QuadracticBinaryFunction}). Since by default this GRASP considers
 * minimization problems, an inverse QBF function is adopted.
 *
 * @author ccavellucci, fusberti
 */
public class GRASP_QBF_TTTPlot extends AbstractGRASP<Integer> {

    private static final int TTTPlot_ITERATIONS = 200;
    
    /**
     * Constructor for the GRASP_QBF class. An inverse QBF objective function is
     * passed as argument for the superclass constructor.
     *
     * @param alpha The GRASP greediness-randomness parameter (within the range
     * [0,1])
     * @param iterations The number of iterations which the GRASP will be
     * executed.
     * @param filename Name of the file for which the objective function
     * parameters should be read.
     * @throws IOException necessary for I/O operations.
     */
    public GRASP_QBF_TTTPlot(Double alpha, Integer iterations, String filename) throws IOException {
        super(new QBF_Inverse(filename), alpha, iterations);
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see grasp.abstracts.AbstractGRASP#makeCL()
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

    /*
	 * (non-Javadoc)
	 * 
	 * @see grasp.abstracts.AbstractGRASP#makeRCL()
     */
    @Override
    public ArrayList<Integer> makeRCL() {

        ArrayList<Integer> _RCL = new ArrayList<Integer>();

        return _RCL;

    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see grasp.abstracts.AbstractGRASP#updateCL()
     */
    @Override
    public void updateCL() {
        for (int i = 0; i < incumbentSol.size(); i++) {
            if (CL.contains(incumbentSol.get(i) - 1)) {
                Object aux = incumbentSol.get(i) - 1;

                CL.remove(aux);
            }

            if (CL.contains(incumbentSol.get(i) + 1)) {
                Object aux = incumbentSol.get(i) + 1;

                CL.remove(aux);
            }
        }
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
    public Solution<Integer> localSearch() {
        boolean improvement = true;
        Double minDeltaCost;
        Integer bestCandIn = null, bestCandOut = null;
        boolean flag = false;

        do {
            minDeltaCost = Double.POSITIVE_INFINITY;
            updateCL();

            Random gen = new Random();

            // Evaluate insertions
            for (int i = 0; i < ObjFunction.getDomainSize(); i++) {
                double deltaCost = 0.0;

                if(improvement)
                    deltaCost = ObjFunction.evaluateInsertionCost(i, incumbentSol);
                else
                    deltaCost = ObjFunction.evaluateInsertionCost(gen.nextInt(ObjFunction.getDomainSize()), incumbentSol);
                
                if (deltaCost < minDeltaCost) {
                    minDeltaCost = deltaCost;
                    bestCandIn = i;
                    bestCandOut = null;
                    if (!improvement) {
                        flag = true;
                        break;
                    }
                }
            }
            // Evaluate removals
            for (Integer i : incumbentSol) {
                double deltaCost = 0.0;

                if(improvement)
                    deltaCost = ObjFunction.evaluateRemovalCost(i, incumbentSol);
                else
                    deltaCost = ObjFunction.evaluateRemovalCost(incumbentSol.get(gen.nextInt(incumbentSol.size())), incumbentSol);
                
                if (deltaCost < minDeltaCost) {
                    minDeltaCost = deltaCost;
                    bestCandIn = null;
                    bestCandOut = i;
                    if (!improvement) {
                        flag = true;
                        break;
                    }
                }
            }

            // Evaluate exchanges
            for (Integer i : CL) {
                int candIn = 0;

                if(improvement)
                    candIn = i;
                else
                    candIn = CL.get(gen.nextInt(CL.size()));

                for (Integer candOut : incumbentSol) {
                    if (incumbentSol.contains(candIn) || !incumbentSol.contains(candOut) || Math.abs(candIn - candOut) <= 1) {
                        continue;
                    }
                    
                    double deltaCost = 0.0;

                    if(improvement)
                        deltaCost = ObjFunction.evaluateExchangeCost(candIn, candOut, incumbentSol);
                    else
                        deltaCost = ObjFunction.evaluateExchangeCost(candIn, incumbentSol.get(gen.nextInt(incumbentSol.size())), incumbentSol);

                    if (deltaCost < minDeltaCost) {
                        minDeltaCost = deltaCost;
                        bestCandIn = candIn;
                        bestCandOut = candOut;
                        if (!improvement) {
                            flag = true;
                            break;
                        }
                    }
                }
            }

            // Implement the best move, if it reduces the solution cost.
            if (minDeltaCost < -Double.MIN_VALUE) {
                if (bestCandOut != null) {
                    incumbentSol.remove(bestCandOut);

                    if (bestCandOut - 1 >= 0) {
                        CL.add(bestCandOut - 1);
                    }
                    CL.add(bestCandOut);
                    if (bestCandOut + 1 < ObjFunction.getDomainSize()) {
                        CL.add(bestCandOut + 1);
                    }
                }
                if (bestCandIn != null) {
                    incumbentSol.add(bestCandIn);

                    Object aux = bestCandIn - 1;
                    incumbentSol.remove(aux);
                    aux = bestCandIn + 1;
                    incumbentSol.remove(aux);

                    CL.remove(bestCandIn);

                }
                ObjFunction.evaluate(incumbentSol);
            }

        } while (minDeltaCost < -Double.MIN_VALUE);

        return null;
    }

    /**
     * A main method used for testing the GRASP metaheuristic.
     *
     */
    public static void main(String[] args) throws IOException {
        isTTTPlotExecution = true;
        GraspParametersTTTPlot parameters = buildParameters(args);
        targetCost = parameters.getTargetCost();
        
        for(int i = 0; i < TTTPlot_ITERATIONS; i++) {
            rng = new Random();
            
            long startTime = System.currentTimeMillis();
            GRASP_QBF_TTTPlot grasp = new GRASP_QBF_TTTPlot(parameters.getAlpha(), 1000, parameters.getInstanceName());
            grasp.solve();
            //System.out.println("Best " + bestSol);
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            System.out.println((double)totalTime/(double)1000);
        }
    }
    
    private static GraspParametersTTTPlot buildParameters(String[] args) {
        GraspParametersTTTPlot parameters = new GraspParametersTTTPlot();
        parameters.setInstanceName(args[0]);
        parameters.setAlpha(Double.parseDouble(args[1]));
        parameters.setTargetCost(Double.parseDouble(args[2]));
        
        /*parameters.setInstanceName("instances/qbf080");
        parameters.setAlpha(0.05);
        parameters.setTargetCost(-400.00);*/
        
        return parameters;
    }

}

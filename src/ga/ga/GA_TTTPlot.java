/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ga.ga;

import java.io.IOException;

import ga.metaheuristics.ga.AbstractGA;
import ga.problems.qbf.solvers.GA_QBF;

/**
 *
 * @author Felipe
 */
public class GA_TTTPlot {
    
    private static final int TTTPlot_ITERATIONS = 200;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        GAParametersTTTPlot parameters = buildParameters(args);
        String construcao = parameters.getConstructionType();
        int tam = Integer.parseInt(parameters.getInstanceName().split("qbf")[1]);
        int tamPop = (int) Math.ceil(parameters.getPopulationSize() * Math.log(tam));
        int taxaMut = parameters.getMutationTax();
        
        GA_QBF ga = new GA_QBF(3000, tamPop, taxaMut / (double) tamPop, parameters.getInstanceName());
        if (construcao.equals("alternativa")) {
            ga.fitRate = parameters.getFitRate();
            ga.sizeRate = parameters.getLocusTax();
        }
        ga.contrucao = construcao;
        AbstractGA.targetCost = parameters.getTargetValue();
        AbstractGA.isTTTPlotExecution = true;
        
        for(int i = 0; i < TTTPlot_ITERATIONS; i++) {
            long startTime = System.currentTimeMillis();
            ga.solve();
            //System.out.println(bestSol.cost);
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            System.out.println((double) totalTime / (double) 1000);
        }
    }
    
    private static GAParametersTTTPlot buildParameters(String[] args) {
        GAParametersTTTPlot parameters = new GAParametersTTTPlot();
        
        parameters.setInstanceName(args[0]);
        parameters.setConstructionType(args[1]);
        parameters.setPopulationSize(Integer.parseInt(args[2]));
        parameters.setMutationTax(Integer.parseInt(args[3]));
        parameters.setTargetValue(Double.parseDouble(args[4]));
        
        if(parameters.getConstructionType().equals("alternativa")) {
            parameters.setFitRate(Double.parseDouble(args[5]));
            parameters.setLocusTax(Double.parseDouble(args[6]));
        }
        
        /*parameters.setInstanceName("instances/qbf060");
        parameters.setConstructionType("padrao");
        parameters.setPopulationSize(3);
        parameters.setMutationTax(2);
        parameters.setTargetValue(300.00);
        
        if(parameters.getConstructionType().equals("alternativa")) {
            parameters.setFitRate(Double.parseDouble(args[4]));
            parameters.setLocusTax(Double.parseDouble(args[5]));
        }*/
        
        return parameters;
    }

}

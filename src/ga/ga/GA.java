/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ga.ga;

import java.io.IOException;
import ga.problems.qbf.solvers.GA_QBF;
import ga.solutions.Solution;

/**
 *
 * @author Felipe
 */
public class GA {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        String construcao = args[1];

        int tam = Integer.parseInt(args[0].split("qbf")[1]);

        int tamPop = (int) Math.ceil(Integer.parseInt(args[2]) * Math.log(tam));
        int taxaMut = Integer.parseInt(args[3]);

        long startTime = System.currentTimeMillis();

        GA_QBF ga = new GA_QBF(3000, tamPop, taxaMut / (double) tamPop, args[0]);
        if (construcao.equals("alternativa")) {
            ga.fitRate = Double.parseDouble(args[4]);
            ga.sizeRate = Double.parseDouble(args[5]);
        }
        ga.contrucao = construcao;
        Solution<Integer> bestSol = ga.solve();
        System.out.println(bestSol.cost);
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        //System.out.println("Time = " + (double) totalTime / (double) 1000 + " seg");
    }

}

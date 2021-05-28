//  SolutionSet.Java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Copyright (c) 2011 Antonio J. Nebro, Juan J. Durillo
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
// 
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.
package jmetal.metaheuristics.mocell;

import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.SolutionSet;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.SelectionFactory;
import jmetal.problems.UDN.DynamicCSO;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.JMException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * * This class executes the algorithms described in A.J. Nebro, J.J. Durillo,
 * F. Luna, B. Dorronsoro, E. Alba "Design Issues in a Multiobjective Cellular
 * Genetic Algorithm." Evolutionary Multi-Criterion Optimization. 4th
 * International Conference, EMO 2007. Sendai/Matsushima, Japan, March 2007.
 */
public class DMOCell_NRC_main {

    /**
     * @param args Command line arguments. The first (optional) argument
     *             specifies the problem to solve.
     * @throws JMException
     * @throws IOException
     * @throws SecurityException Usage: three options -
     *                           jmetal.metaheuristics.mocell.MOCell_main -
     *                           jmetal.metaheuristics.mocell.MOCell_main problemName -
     *                           jmetal.metaheuristics.mocell.MOCell_main problemName ParetoFrontFile
     */
    public static void main(String[] args) throws JMException, SecurityException, IOException, ClassNotFoundException {
        Problem problem;         // The problem to solve
        DMOCell_NRC algorithm;         // The algorithm to use
        Operator crossover;         // Crossover operator
        Operator mutation;         // Mutation operator
        Operator selection;         // Selection operator

        QualityIndicator indicators; // Object to get quality indicators

        HashMap parameters; // Operator parameters

        // Logger object and file to store log messages

        indicators = null;
        int popSize = Integer.parseInt(args[0]);
        int numEvals = Integer.parseInt(args[1]);
        int epochs = Integer.parseInt(args[2]);
        int run = Integer.parseInt(args[3]);
        //problem = new CSO("main.conf", "cells.conf", "socialAttractors.conf", "users.conf", run, "bw");
        problem = new DynamicCSO("main.conf", run, epochs);

        algorithm = new DMOCell_NRC(problem);

        // Algorithm parameters
        algorithm.setInputParameter("populationSize", popSize);
        algorithm.setInputParameter("archiveSize", 100);
        algorithm.setInputParameter("maxEvaluations", numEvals);
        algorithm.setInputParameter("feedBack", 20);

        // Mutation and Crossover for Real codification 
        parameters = new HashMap();
        parameters.put("probability", 1.0);
        crossover = CrossoverFactory.getCrossoverOperator("TwoPointsCrossover", parameters);

        parameters = new HashMap();
        parameters.put("probability", 1.0 / ((DynamicCSO) problem).getTotalNumberOfActivableCells());
        mutation = MutationFactory.getMutationOperator("BitFlipMutation", parameters);

        // Selection Operator 
        parameters = null;
        selection = SelectionFactory.getSelectionOperator("BinaryTournament", parameters);

        // Add the operators to the algorithm
        algorithm.addOperator("crossover", crossover);
        algorithm.addOperator("mutation", mutation);
        algorithm.addOperator("selection", selection);

        long initTime = System.currentTimeMillis();
        SolutionSet population = algorithm.execute();
        long estimatedTime = System.currentTimeMillis() - initTime;

        // Result messages 
        System.out.println("Total execution time: " + estimatedTime + "ms");
        //population.printVariablesToFile("VAR");
        List<SolutionSet> frontsAtEpoch = algorithm.getFrontsAtEpochs();
        for (int i = 0; i <= epochs; i++) {
            frontsAtEpoch.get(i).printVariablesToFile("DMOCell_NRC.VAR.r" + i + "." + run);
            frontsAtEpoch.get(i).printObjectivesToFile("DMOCell_NRC.FUN.r" + i + "." + run);
        }


    }//main
} // MOCell_main

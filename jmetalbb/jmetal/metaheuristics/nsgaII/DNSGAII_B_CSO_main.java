//  NSGAII_main.java
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
package jmetal.metaheuristics.nsgaII;

import jmetal.core.Operator;
import jmetal.core.SolutionSet;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.SelectionFactory;
import jmetal.problems.UDN.CSO;
import jmetal.problems.UDN.DynamicCSO;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.JMException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Class to configure and execute the NSGA-II algorithm.
 * <p>
 * Besides the classic NSGA-II, a steady-state version (ssNSGAII) is also
 * included (See: J.J. Durillo, A.J. Nebro, F. Luna and E. Alba "On the Effect
 * of the Steady-State Selection Scheme in Multi-Objective Genetic Algorithms"
 * 5th International Conference, EMO 2009, pp: 183-197. April 2009)
 */
public class DNSGAII_B_CSO_main {

    /**
     * @param args Command line arguments.
     * @throws JMException
     * @throws IOException
     * @throws SecurityException Usage: three options -
     *                           jmetal.metaheuristics.nsgaII.NSGAII_main -
     *                           jmetal.metaheuristics.nsgaII.NSGAII_main problemName -
     *                           jmetal.metaheuristics.nsgaII.NSGAII_main problemName paretoFrontFile
     */
    public static void main(String[] args) throws
            JMException,
            SecurityException,
            IOException,
            ClassNotFoundException {
        CSO problem; // The problem to solve
        DNSGAII_B algorithm; // The algorithm to use
        Operator crossover; // Crossover operator
        Operator mutation; // Mutation operator
        Operator selection; // Selection operator

        HashMap parameters; // Operator parameters

        QualityIndicator indicators = null; // Object to get quality indicators

        int popSize = Integer.parseInt(args[0]);
        int numEvals = Integer.parseInt(args[1]);
        double restart = Double.parseDouble(args[2]);
        int epochs = Integer.parseInt(args[3]);
        int run = Integer.parseInt(args[4]);

        //defines the CSO problem
        //problem = new CSO("main.conf", "cells.conf", "socialAttractors.conf", "users.conf", run, "bw");
        problem = new DynamicCSO("main.conf", run, epochs);

        algorithm = new DNSGAII_B(problem);
        //algorithm = new ssNSGAII(problem);

        // Algorithm parameters
        algorithm.setInputParameter("populationSize", popSize);
        algorithm.setInputParameter("maxEvaluations", numEvals);
        algorithm.setInputParameter("restartPercentage", restart);

        // Mutation and Crossover for Real codification 
        parameters = new HashMap();
        parameters.put("probability", 0.9);
        crossover = CrossoverFactory.getCrossoverOperator("TwoPointsCrossover", parameters);

        parameters = new HashMap();
        parameters.put("probability", 1.0 / problem.getTotalNumberOfActivableCells());
        mutation = MutationFactory.getMutationOperator("BitFlipMutation", parameters);

        // Selection Operator 
        parameters = null;
        selection = SelectionFactory.getSelectionOperator("BinaryTournament2", parameters);

        // Add the operators to the algorithm
        algorithm.addOperator("crossover", crossover);
        algorithm.addOperator("mutation", mutation);
        algorithm.addOperator("selection", selection);

        // Add the indicator object to the algorithm
        algorithm.setInputParameter("indicators", indicators);

        // Execute the Algorithm
        long initTime = System.currentTimeMillis();
        SolutionSet population = algorithm.execute();
        long estimatedTime = System.currentTimeMillis() - initTime;

        // Result messages 
        System.out.println("Total execution time: " + estimatedTime + "ms");
        //population.printVariablesToFile("VAR");
        List<SolutionSet> frontsAtEpoch = algorithm.getFrontsAtEpochs();
        for (int i = 0; i <= epochs; i++) {
            frontsAtEpoch.get(i).printVariablesToFile("DNSGAII_B.VAR.r" + restart + ".e" + i + "." + run);
            frontsAtEpoch.get(i).printObjectivesToFile("DNSGAII_B.FUN.r" + restart + ".e" + i + "." + run);
        }

    } //main
} // NSGAII_main

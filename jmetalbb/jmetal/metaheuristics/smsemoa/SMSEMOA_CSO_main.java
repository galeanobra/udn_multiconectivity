/*
 * SMSEMOA_main.java
 *
 * @author Simon Wessing
 * @version 1.0 This implementation of SMS-EMOA makes use of a QualityIndicator
 * object to obtained the convergence speed of the algorithm.
 */
package jmetal.metaheuristics.smsemoa;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.SolutionSet;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.SelectionFactory;
import jmetal.problems.UDN.CSO;
import jmetal.problems.UDN.StaticCSO;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.JMException;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.FileHandler;

/**
 * Class for configuring and running the SMS-EMOA algorithm. This implementation
 * of SMS-EMOA makes use of a QualityIndicator object to obtained the
 * convergence speed of the algorithm.
 */
public class SMSEMOA_CSO_main {

    public static FileHandler fileHandler_; // FileHandler object

    /**
     * @param args Command line arguments.
     * @throws JMException
     * @throws IOException
     * @throws SecurityException Usage: three options -
     *                           jmetal.metaheuristics.smsemoa.SMSEMOA_main -
     *                           jmetal.metaheuristics.smsemoa.SMSEMOA_main problemName -
     *                           jmetal.metaheuristics.smsemoa.SMSEMOA_main problemName paretoFrontFile
     */
    public static void main(String[] args) throws
            JMException,
            SecurityException,
            IOException,
            ClassNotFoundException {
        CSO problem;         // The problem to solve
        Algorithm algorithm;         // The algorithm to use
        Operator crossover;         // Crossover operator
        Operator mutation;         // Mutation operator
        Operator selection;         // Selection operator

        QualityIndicator indicators; // Object to get quality indicators

        HashMap parameters; // Operator parameters

        indicators = null;
        int popSize = Integer.parseInt(args[0]);
        int numEvals = Integer.parseInt(args[1]);
        int run = Integer.parseInt(args[2]);
        int taskID = Integer.parseInt(args[3]); // Slurm task ID (for filename)
        int jobID = Integer.parseInt(args[4]);  // Slurm job ID (for filename)
        String name = args[5];                  // Name (for output directory)
        String main = args[6];                  // Main config file

        problem = new StaticCSO(main, run);

        algorithm = new HybridSMSEMOA(problem);

        // Algorithm parameters
        algorithm.setInputParameter("populationSize", popSize);
        algorithm.setInputParameter("maxEvaluations", numEvals);
        algorithm.setInputParameter("offset", 100.0);

        // Mutation and Crossover for Real codification 
        parameters = new HashMap();

        parameters.put("probability", 0.9);
        crossover = CrossoverFactory.getCrossoverOperator("TwoPointsCrossover", parameters);

        parameters = new HashMap();
        parameters.put("probability", 1.0 / problem.getTotalNumberOfActivableCells());
        mutation = MutationFactory.getMutationOperator("BitFlipMutation", parameters);

        // Geographic operators
//        parameters = new HashMap();
//        parameters.put("probability", 0.6);
//        parameters.put("range", new int[]{20, 100});
//        parameters.put("udn", ((StaticCSO) problem).getUDN());
//        crossover = CrossoverFactory.getCrossoverOperator("RGX", parameters);
//
//        parameters = new HashMap();
//        parameters.put("probability", 100.0 / ((StaticCSO) problem).getTotalNumberOfActivableCells());
//        parameters.put("flipProbability", 0.05);
//        parameters.put("range", new int[]{10, 50});
//        parameters.put("udn", ((StaticCSO) problem).getUDN());
//        mutation = MutationFactory.getMutationOperator("GeographicMutation", parameters);

        // Selection Operator
        parameters = null;
        selection = SelectionFactory.getSelectionOperator("RandomSelection", parameters);
        // also possible
        //selection = SelectionFactory.getSelectionOperator("BinaryTournament2");

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
        population.printVariablesToFile(name + ".VAR." + taskID + "." + jobID); //"out/" +name + "/VAR/" +
        //population.printVariablesToFile("VAR");
        population.printObjectivesToFile(name + ".FUN." + taskID + "." + jobID); //"out/" +name + "/FUN/" +
        //population.printObjectivesToFile("FUN");
    } //main
} // SMSEMOA_main

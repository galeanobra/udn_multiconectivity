//  Epsilon.java
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

package jmetal.qualityIndicator;

/**
 * This class implements the unary epsilon additive indicator as proposed in
 * E. Zitzler, E. Thiele, L. Laummanns, M., Fonseca, C., and Grunert da Fonseca.
 * V (2003): Performance Assesment of Multiobjective Optimizers: An Analysis and
 * Review. The code is the a Java version of the orginal metric implementation
 * by Eckart Zitzler.
 * It can be used also as a command line program just by typing
 * $java jmetal.qualityIndicator.Epsilon <solutionFrontFile> <trueFrontFile> <numberOfOjbectives>
 */

public class Epsilon {

    /* stores the number of objectives */
    int dim_;
    /* obj_[i]=0 means objective i is to be minimized. This code always suposse
     * the minimization of all the objectives
     */
    int[] obj_;     /* obj_[i] = 0 means objective i is to be minimized */
    /* method_ = 0 means apply additive epsilon and method_ = 1 means multiplicative
     * epsilon. This code always apply additive epsilon
     */
    int method_;
    /* stores a reference to  qualityIndicatorUtils */
    public jmetal.qualityIndicator.util.MetricsUtil utils_ =
            new jmetal.qualityIndicator.util.MetricsUtil();


    /**
     * Returns the epsilon indicator.
     *
     * @param b. True Pareto front
     * @param a. Solution front
     * @return the value of the epsilon indicator
     */
    public double epsilon(double[][] b, double[][] a, int dim) {
        double eps, epsJ = 0.0, epsK = 0.0, epsTemp;

        int numberOfObjectives = dim;

        eps = Double.MIN_VALUE;

        for (int i = 0; i < b.length; i++) {
            for (int j = 0; j < a.length; j++) {
                for (int k = 0; k < numberOfObjectives; k++) {
                    epsTemp = a[j][k]
                            - b[i][k];
                    if (k == 0) {
                        epsK = epsTemp;
                    } else if (epsK < epsTemp) {
                        epsK = epsTemp;
                    }
                }
                if (j == 0) {
                    epsJ = epsK;
                } else if (epsJ > epsK) {
                    epsJ = epsK;
                }
            }
            if (i == 0) {
                eps = epsJ;
            } else if (eps < epsJ) {
                eps = epsJ;
            }
        }
        return eps;
    } // epsilon

    /**
     * Established the params by default
     */
    void set_params() {
        int i;
        obj_ = new int[dim_];
        for (i = 0; i < dim_; i++) {
            obj_[i] = 0;
        }
        method_ = 0;
    } // set_params


    /**
     * Returns the additive-epsilon value of the paretoFront. This method call to the
     * calculate epsilon-indicator one
     *
     * @param paretoFront        The pareto front
     * @param paretoTrueFront    The true pareto front
     * @param numberOfObjectives Number of objectives of the pareto front
     */
    public static void main(String[] args) {
        double ind_value;

        if (args.length < 2) {
            System.err.println("Error using delta. Type: \n java AdditiveEpsilon " +
                    "<FrontFile>" +
                    "<TrueFrontFile> + <numberOfObjectives>");
            System.exit(1);
        }

        Epsilon qualityIndicator = new Epsilon();
        double[][] solutionFront = qualityIndicator.utils_.readFront(args[0]);
        double[][] trueFront = qualityIndicator.utils_.readFront(args[1]);
        //qualityIndicator.dim_ = trueParetoFront[0].length;
        //qualityIndicator.set_params();

        ind_value = qualityIndicator.epsilon(trueFront, solutionFront, new Integer(args[2]));

        System.out.println(ind_value);
    } // main
} // Epsilon


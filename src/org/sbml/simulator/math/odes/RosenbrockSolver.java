/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of ODE Toolkit: a free application for solving
 * systems of ordinary differential equations.
 * 
 * Copyright (C) 2002-2011 Beky Kotcon, Samantha Mesuro, Daniel 
 * Rozenfeld, Anak Yodpinyanee, Andres Perez, Eric Doi, Richard 
 * Mehlinger, Steven Ehrlich, Martin Hunt, George Tucker, Peter 
 * Scherpelz, Aaron Becker, Eric Harley, and Chris Moore
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package org.sbml.simulator.math.odes;

import org.sbml.simulator.math.Mathematics;
import org.sbml.simulator.math.MatrixOperations;
import org.sbml.simulator.math.MatrixOperations.MatrixException;

/**
 * RosenbrockSolver implements the Rosenbrock method to approximate ODE
 * solutions.
 * <p>
 * References: William H. Press, Saul A. Teukolsky, William T. Vetterling, and
 * Brian P. Flannery. Numerical recipes in C. Cambridge Univ. Press Cambridge,
 * 1992, pp. 738-747.
 * <p>
 * This solver has been adapted from ODE Toolkit: a free application for solving
 * systems of ordinary differential equations.
 * 
 * @author Chris Moore
 * @author Roland Keller
 * @version $Rev$
 * @since 0.9
 */
public class RosenbrockSolver extends AbstractDESSolver {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//private boolean includeIntermediates;

	/**
	 * Constants used to adapt the stepsize according to the error in the last
	 * step (see rodas.f)
	 */
	public static final double SAFETY = 0.9, fac1 = 1.0 / 6.0, fac2 = 5,
			PWR = 0.25;

	// The constants cX, dX, aXY, and cXY, are coefficients used in method
	// step()
	// Given in a webnote (see _Numerical Recipies_3rd ed) ----give
	// reference-----

	/** Constants for solving */
	public static final double c2 = 0.386, c3 = 0.21, c4 = 0.63;

	/** Constants for solving */
	public static final double a21 = 1.544000000000000,
			a31 = 0.9466785280815826, a32 = 0.2557011698983284,
			a41 = 3.314825187068521, a42 = 2.896124015972201,
			a43 = 0.9986419139977817, a51 = 1.221224509226641,
			a52 = 6.019134481288629, a53 = 12.53708332932087,
			a54 = -0.6878860361058950;

	/** Constants for solving */
	public static final double gam = 0.250;

	/** Constants for solving */
	public static final double c21 = -5.668800000000000,
			c31 = -2.430093356833875, c32 = -0.2063599157091915,
			c41 = -0.1073529058151375, c42 = -9.594562251023355,
			c43 = -20.47028614809616, c51 = 7.496443313967647,
			c52 = -10.24680431464352, c53 = -33.99990352819905,
			c54 = 11.70890893206160, c61 = 8.083246795921522,
			c62 = -7.981132988064893, c63 = -31.52159432874371,
			c64 = 16.31930543123136, c65 = -6.058818238834054;

	public static final double d1 = 0.25, d2 = 0.1043, d3 = 0.1035,
			d4 = -0.0362;

	/**
	 * the minimum acceptable value of relTol - attempts to obtain higher
	 * accuracy than this are usually very expensive
	 */
	public static final double RELMIN = 1.0E-12;

	/**
	 * 
	 */
  private static final double standardStepSize = 0.01;

	/** maximum stepsize */
	private double hMax;
	/** minimum stepsize */
	private double hMin;

	/** absolute tolerance */
	private double absTol;
	/** relative tolerance */
	private double relTol;
	

	/** the current value of the independent variable */
	private double t;

	/** the current step size */
	private double h;

	/** factor for calculating error value used in adjusting step size */
	double sk;
	/**
	 * factor used for adjusting the step size, divide current step size by
	 * hAdap to get new step size
	 */
	double hAdap;

	/** The number of equations */
	int numEqn;

	/** the current values of the dependent variables */
	private double[] y;

	/** arrays to store derivative evaluations and intermediate steps */
	private double[] f1, f2, f3, f4, f5, f6, k1, k2, k3, k4, k5;

	/**
	 * array that is y with approximated errors added on, used for comparing y
	 * to y+yerr
	 */
	double[] yNew;

	/** array that holds approximate errors in the values in y */
	private double[] yerr;

	/** helper array to hold intermediate values */
	double[] yTemp, ya, yb, g0, g1, g2, g1x, g2x, DFDX;
	/** helper array to hold intermediate values */
	int[] indx;
	/** helper array to hold intermediate values */
	double[][] JAC, FAC, I;

	/** Keep track whether the thread is killed or not */
	boolean stop;

	/**
	 * 
	 */
  private double[] timePoints;
	
	/**
	 * 
	 * @param size
	 * @param stepsize
	 */
	/**
   * 
   */
  public RosenbrockSolver() {
     super();
  }
	public RosenbrockSolver(int size, double stepsize) {
		super();

		init(size,stepsize,2);
		
	}

	private void init(int size, double stepsize, int nTimepoints) {
    numEqn = size;
		
		hMin = 1E-12;
		this.setStepSize(stepsize);
		hMax = Math.min(stepsize,0.1);
		absTol = 1E-10;
		relTol = 1E-7;

		stop = false;
		timePoints = new double[nTimepoints]; 
		// allocate arrays
		y = new double[numEqn];
		f1 = new double[numEqn];
		f2 = new double[numEqn];
		f3 = new double[numEqn];
		f4 = new double[numEqn];
		f5 = new double[numEqn];
		f6 = new double[numEqn];
		k1 = new double[numEqn];
		k2 = new double[numEqn];
		k3 = new double[numEqn];
		k4 = new double[numEqn];
		k5 = new double[numEqn];
		yNew = new double[numEqn];
		yerr = new double[numEqn];
		yTemp = new double[numEqn];
		ya = new double[numEqn];
		yb = new double[numEqn];
		g0 = new double[numEqn];
		g1 = new double[numEqn];
		g2 = new double[numEqn];
		g1x = new double[numEqn];
		g2x = new double[numEqn];
		DFDX = new double[numEqn];
		indx = new int[numEqn];
		JAC = new double[numEqn][numEqn];
		FAC = new double[numEqn][numEqn];
		I = new double[numEqn][numEqn];
  }
	
	@Override
	public AbstractDESSolver clone() {
		return new RosenbrockSolver(numEqn,this.getStepSize());
	}

	/*
	public MultiBlockTable solve(DESystem DES, double[] initialValues,double timeBegin,
			double timeEnd) throws IntegrationException {
	  if (((DES instanceof FastProcessDESystem) &&( ((FastProcessDESystem) DES).containsFastProcesses()))||(DES.containsEventsOrRules())) {
	    super.solve(DES, initialValues, timeBegin,timeEnd);
	  }
	  
	  MultiBlockTable data = initResultMatrix(DES, initialValues, timeBegin,timeEnd);
		double result[][] = data.getBlock(0).getData();
		additionalResults(DES, t, result[0], data, 0);
	  if(y==null) {
		  init(DES.getDESystemDimension(),this.getStepSize(),data.getTimePoints().length);
		}
		
		
		
		try {

			double localError = 0;
			int solutionIndex = 0;

			// temporary variable used when adjusting stepsize
			double tNew;

			// was the last step successful? (do we have to repeat the step
			// with a smaller stepsize?)
			boolean lastStepSuccessful = false;

			// Compute epsilon. This is the smallest double X such that
			// 1.0+X!=1.0
			double eps = unitRoundoff();
			// Restrict relative error tolerance to be at least as large as
			// 2*eps+RELMIN to avoid limiting precision difficulties arising
			// from impossible accuracy requests
			double relMin = 2.0 * eps + RELMIN;
			if (relTol < relMin)
				relTol = relMin;

			// set t to the initial independent value and y[] to the
			// initial dependent values
			t = timeBegin;
      timePoints[0] = t;
      

			for (int i = 0; i < initialValues.length; i++){
				y[i] = initialValues[i];

			}
			
			// add the initial conditions to the solution matrix and let all
			// point
			// ready listeners know about it

			// set initial stepsize - we want to try the maximum stepsize to
			// begin
			// with and move to smaller values if necessary
			h = hMax;

			while (!stop) {

				// if the last step was successful (t was updated)...
				if (lastStepSuccessful) {

					// ... and the current t differs from the last recorded one
					// by
					// at least stepsize...
					if (Math.abs(timePoints[solutionIndex] - t) >= Math
							.abs(this.getStepSize())) {

						// ...we want to record the current point in the
						// solution
						// matrix and notify all pointReadyListeners of the
						// point
						
						
						solutionIndex++;
						timePoints[solutionIndex] = t;
						System.arraycopy(y, 0, result[solutionIndex], 0, y.length);
						additionalResults(DES, t - this.getStepSize(), result[solutionIndex - 1], data, solutionIndex);
					}
				}

				// see if we're done
				if (t >= timeEnd) {
					break;
				}

				// copy the current point into yTemp
				System.arraycopy(y, 0, yTemp, 0, numEqn);
				try {

					// take a step
					localError = step(DES);
				} catch (Exception ex) {
					new Error("RB.step() threw an exception" + ex);
					stop = true;
				}

				if (localError == -1) {
					new Error("Infinity or NaN encountered by the RB solver... stopping solve");
					stop = true;
				}

				// good step
				if (localError <= 1.0) {
					t += h;
					System.arraycopy(yTemp, 0, y, 0, numEqn);

					// change stepsize (see Rodas.f) require 0.2<=hnew/h<=6
					hAdap = Math.max(fac1,
							Math.min(fac2, Math.pow(localError, PWR) / SAFETY));
					h = h / hAdap;
					lastStepSuccessful = true;

				} else {

					// if we just tried to use the minimum stepsize and still
					// failed to achieve the desired accuracy, it's useless to
					// continue, so we stop
					if (Math.abs(h) <= Math.abs(hMin)) {
						new Error("Requested tolerance could not be achieved, even at the minumum stepsize.  Please increase the tolerance or decrease the minimum stepsize.");
						stop = true;
					}

					// change stepsize (see Rodas.f) require 0.2<=hnew/h<=6
					hAdap = Math.max(fac1,
							Math.min(fac2, Math.pow(localError, PWR) / SAFETY));
					h = h / hAdap;
					tNew = t + h;
					if (tNew == t) {
						new Error("Stepsize underflow in Rosenbrock solver");
						stop = true;
					}
					lastStepSuccessful = false;
				}

				// check bounds on the new stepsize
				if (Math.abs(h) < hMin) {
						h = hMin;

				} else if (Math.abs(h) > hMax) {
						h = hMax;
				}

			}

			if (!stop){
				
			}
			//solveDone();
		} catch (OutOfMemoryError e) {
			new Error("Out of memory : try reducing solve span or increasing step size.");
		}

		return data;

	}
	*/

	
	public double step(DESystem DES) throws IntegrationException {

		double largestError = 0;

		DES.getValue(t, y, g0);
		for (int j = 0; j < numEqn; j++) {
			System.arraycopy(y, 0, ya, 0, numEqn);
			ya[j] += h;
			System.arraycopy(y, 0, yb, 0, numEqn);
			yb[j] += 2 * h;
			DES.getValue(t, ya, g1);
			DES.getValue(t, yb, g2);
			for (int q = 0; q < numEqn; q++) {
				JAC[q][j] = (-3 * g0[q] + 4 * g1[q] - g2[q]) / (2 * h);
			}
		}
		for (int i = 0; i < numEqn; i++) {
			for (int j = 0; j < numEqn; j++) {
				if (i == j) {
					I[i][j] = 1;
				} else {
					I[i][j] = 0;
				}
			}
		}

		for (int i = 0; i < numEqn; i++)
			for (int j = 0; j < numEqn; j++)
				FAC[i][j] = I[i][j] / (gam * h) - JAC[i][j];

		// Forward difference approx for derivative of f
		// WRT the independent variable
		DES.getValue(t + h, y, g1x);
		DES.getValue(t + 2 * h, y, g2x);
		for (int i = 0; i < numEqn; i++)
			DFDX[i] = g0[i] * -3 / (2 * h) + g1x[i] * 2 / h + g2x[i] * -1
					/ (2 * h);

		// Here the work of taking the step begins
		// It uses the derivatives calculated above
		DES.getValue(t, yTemp, f1);
		for (int i = 0; i < numEqn; i++) {
			k1[i] = f1[i] + DFDX[i] * h * d1;
		}

		try {
			MatrixOperations.ludcmp(FAC, indx);
		} catch (MatrixException e) {
			throw new Error(
					"Rosenbrock solver returns an error due to singular matrix.");
		}

		MatrixOperations.lubksb(FAC, indx, k1);

		for (int i = 0; i < numEqn; i++)
			yTemp[i] = y[i] + k1[i] * a21;
		DES.getValue(t + c2 * h, yTemp, f2);
		for (int i = 0; i < numEqn; i++)
			k2[i] = f2[i] + DFDX[i] * h * d2 + k1[i] * c21 / h;
		MatrixOperations.lubksb(FAC, indx, k2);

		for (int i = 0; i < numEqn; i++)
			yTemp[i] = y[i] + k1[i] * a31 + k2[i] * a32;
		DES.getValue(t + c3 * h, yTemp, f3);
		for (int i = 0; i < numEqn; i++)
			k3[i] = f3[i] + DFDX[i] * h * d3 + k1[i] * c31 / h + k2[i] * c32
					/ h;
		MatrixOperations.lubksb(FAC, indx, k3);

		for (int i = 0; i < numEqn; i++)
			yTemp[i] = y[i] + k1[i] * a41 + k2[i] * a42 + k3[i] * a43;
		DES.getValue(t + c4 * h, yTemp, f4);
		for (int i = 0; i < numEqn; i++)
			k4[i] = f4[i] + DFDX[i] * h * d4 + k1[i] * c41 / h + k2[i] * c42
					/ h + k3[i] * c43 / h;
		MatrixOperations.lubksb(FAC, indx, k4);

		for (int i = 0; i < numEqn; i++)
			yTemp[i] = y[i] + k1[i] * a51 + k2[i] * a52 + k3[i] * a53 + k4[i]
					* a54;
		DES.getValue(t + h, yTemp, f5);
		for (int i = 0; i < numEqn; i++)
			k5[i] = f5[i] + k1[i] * c51 / h + k2[i] * c52 / h + k3[i] * c53 / h
					+ k4[i] * c54 / h;
		MatrixOperations.lubksb(FAC, indx, k5);

		for (int i = 0; i < numEqn; i++)
			yTemp[i] += k5[i];
		DES.getValue(t + h, yTemp, f6);
		for (int i = 0; i < numEqn; i++)
			yerr[i] = f6[i] + k1[i] * c61 / h + k2[i] * c62 / h + k3[i] * c63
					/ h + k4[i] * c64 / h + k5[i] * c65 / h;
		MatrixOperations.lubksb(FAC, indx, yerr);

		for (int i = 0; i < numEqn; i++)
			yNew[i] = yTemp[i] + yerr[i];

		for (int i = 0; i < numEqn; i++) {
			sk = absTol + relTol * Math.max(Math.abs(y[i]), Math.abs(yNew[i]));
			largestError += Math.pow(yerr[i] / sk, 2);

			if (Double.isInfinite(yTemp[i]) || Double.isNaN(yTemp[i]))
				return -1;
		}
		largestError = Math.pow(largestError / numEqn, 0.5);
		return largestError;

	}

	/**
	 * Returns an approximation to the error involved with the current
	 * arithmetic implementation
	 * 
	 * @return the approximation as described above
	 */
	public double unitRoundoff() {
		double u;
		double one_plus_u;

		u = 1.0;
		one_plus_u = 1.0 + u;
		// Check to see if the number 1.0 plus some positive offset
		// computes to be the same as one.
		while (one_plus_u != 1.0) {
			u /= 2.0;
			one_plus_u = 1.0 + u;
		}
		u *= 2.0; // Go back one step

		return (u);
	}

	@Override
	public String getName() {
		return "Rosenbrock solver";
	}

	@Override
	public double[] computeChange(DESystem DES, double[] y2, double time,
			double currentStepSize, double[] change) throws IntegrationException {
	  if(y==null) {
      init(DES.getDESystemDimension(),this.getStepSize(),2);
    }
	  if(currentStepSize!=this.getStepSize()) {
	    this.hMax = Math.min(currentStepSize,standardStepSize);
	  }
	  
	  double timeEnd=time+currentStepSize;
    try {
      
      double localError = 0;
      int solutionIndex = 0;

      // temporary variable used when adjusting stepsize
      double tNew;

      // was the last step successful? (do we have to repeat the step
      // with a smaller stepsize?)
      boolean lastStepSuccessful = false;

      // Compute epsilon. This is the smallest double X such that
      // 1.0+X!=1.0
      double eps = unitRoundoff();
      // Restrict relative error tolerance to be at least as large as
      // 2*eps+RELMIN to avoid limiting precision difficulties arising
      // from impossible accuracy requests
      double relMin = 2.0 * eps + RELMIN;
      if (relTol < relMin)
        relTol = relMin;

      // set t to the initial independent value and y[] to the
      // initial dependent values
      t = time;
      timePoints[0] = t;
      
      if(y.length!=y2.length) {
        y=y2.clone();
      }
      else {
        System.arraycopy(y2, 0, y, 0, y.length);
      }
      // add the initial conditions to the solution matrix and let all
      // point
      // ready listeners know about it

      // set initial stepsize - we want to try the maximum stepsize to
      // begin
      // with and move to smaller values if necessary
      h = hMax;

      while (!stop) {
        
        // if the last step was successful (t was updated)...
        if (lastStepSuccessful) {

          // ... and the current t differs from the last recorded one
          // by
          // at least stepsize...
          if (Math.abs(timePoints[solutionIndex] - t) >= Math
              .abs(currentStepSize)) {

            // ...we want to record the current point in the
            // solution
            // matrix and notify all pointReadyListeners of the
            // point
            solutionIndex++;
            timePoints[solutionIndex] = t;
            
          }
        }

        // see if we're done
        if (t >= timeEnd) {
          Mathematics.vvSub(y, y2, change);
            break;
        }
        // copy the current point into yTemp
        System.arraycopy(y, 0, yTemp, 0, numEqn);
        try {
          
          // take a step
          localError = step(DES);
        } catch (Exception ex) {
          new Error("RB.step() threw an exception" + ex);
          stop = true;
        }
        if (localError == -1) {
          new Error("Infinity or NaN encountered by the RB solver... stopping solve");
          stop = true;
        }

        // good step
        if (localError <= 1.0) {
          t += h;
          System.arraycopy(yTemp, 0, y, 0, numEqn);

          // change stepsize (see Rodas.f) require 0.2<=hnew/h<=6
          hAdap = Math.max(fac1,
              Math.min(fac2, Math.pow(localError, PWR) / SAFETY));
          h = h / hAdap;
          if(timeEnd-t-h<hMin) {
            h=timeEnd-t;
          }
          lastStepSuccessful = true;

        } else {

          // if we just tried to use the minimum stepsize and still
          // failed to achieve the desired accuracy, it's useless to
          // continue, so we stop
          if (Math.abs(h) <= Math.abs(hMin)) {
            new Error("Requested tolerance could not be achieved, even at the minumum stepsize.  Please increase the tolerance or decrease the minimum stepsize.");
            stop = true;
          }

          // change stepsize (see Rodas.f) require 0.2<=hnew/h<=6
          hAdap = Math.max(fac1,
              Math.min(fac2, Math.pow(localError, PWR) / SAFETY));
          h = h / hAdap;
          if(timeEnd-t-h<hMin) {
            h=timeEnd-t;
          }
          tNew = t + h;
          if (tNew == t) {
            new Error("Stepsize underflow in Rosenbrock solver");
            stop = true;
          }
          lastStepSuccessful = false;
        }

        // check bounds on the new stepsize
        if (Math.abs(h) < hMin) {
            h = hMin;

        } else if (Math.abs(h) > hMax) {
            h = hMax;
        }
      }

      if (!stop){
        
      }
      //solveDone();
    } catch (OutOfMemoryError e) {
      new Error("Out of memory : try reducing solve span or increasing step size.");
    }


    return change;
	}
	

}
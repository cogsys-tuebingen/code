/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsimulator, a Java-based simulator for models
 * of biochemical processes encoded in the modeling language SBML.
 *
 * Copyright (C) 2007-2011 by the University of Tuebingen, Germany.
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

import org.apache.commons.math.ode.nonstiff.GraggBulirschStoerIntegrator;

/**
 * 
 * @author Roland Keller
 * @version $Rev$
 * @since 1.0
 */
public class GraggBulirschStoerSolver extends FirstOrderSolver {
    
	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -2601862472447650296L;
	
	/**
	 * 
	 */
	public GraggBulirschStoerSolver() {
	    super();
	}
	
	/**
	 * 
	 * @param stepSize
	 */
	public GraggBulirschStoerSolver(double stepSize) {
		super(stepSize);
	}
	
	/**
	 * 
	 * @param stepSize
	 * @param nonnegative
	 */
	public GraggBulirschStoerSolver(double stepSize, boolean nonnegative) {
		super(stepSize, nonnegative);
		
	}
	
	/**
	 * 
	 * @param graggBulirschStoerSolver
	 */
	public GraggBulirschStoerSolver(GraggBulirschStoerSolver solver) {
		super(solver);
		this.integrator=solver.getIntegrator();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sbml.simulator.math.odes.FirstOrderSolver#clone()
	 */
	@Override
	public GraggBulirschStoerSolver clone() {
		return new GraggBulirschStoerSolver(this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.simulator.math.odes.FirstOrderSolver#createIntegrator()
	 */
	@Override
	protected void createIntegrator() {
		integrator=new GraggBulirschStoerIntegrator(Math.min(1e-8,Math.min(1.0,getStepSize())), Math.min(1.0,getStepSize()),0.00001, 0.00001);
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.simulator.math.odes.AbstractDESSolver#getName()
	 */
	@Override
	public String getName() {
		return "Gragg-Bulirsch-Stoer solver";
	}

}
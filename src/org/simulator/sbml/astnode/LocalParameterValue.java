/*
 * ---------------------------------------------------------------------
 * This file is part of the Simulation Core Library, a Java-based
 * library for efficient numerical simulation of biological models.
 *
 * Copyright (C) 2007-2012 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package org.simulator.sbml.astnode;

import java.beans.PropertyChangeEvent;

import javax.swing.tree.TreeNode;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.util.TreeNodeChangeListener;


/**
 * This class computes and stores values of ASTNodes that refer to a local parameter.
 * @author Roland Keller
 * @version $Rev$
 * @since 1.0
 */
public class LocalParameterValue extends ASTNodeValue implements TreeNodeChangeListener {
	
	/**
	 * The corresponding local parameter
	 */
	protected LocalParameter lp;

	/**
	 * 
	 * @param interpreter
	 * @param node
	 * @param lp
	 */
	public LocalParameterValue(ASTNodeInterpreterWithTime interpreter, ASTNode node,
			LocalParameter lp) {
		super(interpreter, node);
		this.lp = lp;
		lp.addTreeNodeChangeListener(this);
		doubleValue=lp.getValue();
		isDouble=true;
	}

	/* (non-Javadoc)
	 * @see org.sbml.simulator.math.astnode.ASTNodeObject#compileDouble(double)
	 */
	@Override
	public double compileDouble(double time) {
		this.time=time;
		return doubleValue;
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		String property = evt.getPropertyName();

		if ("value".equals(property)) {
			doubleValue = (Double) evt.getNewValue();
		}
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.util.TreeNodeChangeListener#nodeAdded(javax.swing.tree.TreeNode)
	 */
	public void nodeAdded(TreeNode node) {
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.util.TreeNodeChangeListener#nodeRemoved(javax.swing.tree.TreeNode)
	 */
	public void nodeRemoved(TreeNode node) {
	}

}

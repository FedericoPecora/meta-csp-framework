/*******************************************************************************
 * Copyright (c) 2010-2013 Federico Pecora <federico.pecora@oru.se>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package org.metacsp.meta.TCSP;

import java.util.Vector;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariableOrderingH;
import org.metacsp.framework.meta.MetaConstraint;
import org.metacsp.framework.meta.MetaVariable;
import org.metacsp.framework.multi.MultiConstraint;
import org.metacsp.multi.TCSP.DistanceConstraint;
import org.metacsp.time.Bounds;
import org.metacsp.time.SimpleDistanceConstraint;

public class TCSPLabeling extends MetaConstraint {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4312671705957049958L;

	public TCSPLabeling(VariableOrderingH varOH, ValueOrderingH valOH) {
		super(varOH, valOH);
		// TODO Auto-generated constructor stub
	}

	@Override
	public ConstraintNetwork[] getMetaVariables() {
		ConstraintNetwork dcn = getGroundSolver().getConstraintNetwork();//(ConstraintNetwork)metaCS.getConstraintSolvers()[0].getConstraintNetwork();
		Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
		//ConstraintNetwork[] ret = new ConstraintNetwork[dcn.getConstraints().length];
		for (Constraint con : dcn.getConstraints()) {
			//Only get variables representing constraints that are not propagated
			DistanceConstraint dc = (DistanceConstraint)con;
			//if (dc.getInternalConstraints().length > 1 && !dc.isPropagated()) {
			if (!dc.propagateImmediately()) {
				ConstraintNetwork oneEdge = new ConstraintNetwork(dc.getFrom().getConstraintSolver());
				oneEdge.addVariable(dc.getFrom());
				oneEdge.addVariable(dc.getTo());
				oneEdge.addConstraint(dc);
				ret.add(oneEdge);
			}
		}
		return ret.toArray(new ConstraintNetwork[ret.size()]);
	}

	
	@Override
	public ConstraintNetwork[] getMetaValues(MetaVariable metaVariable) {
		ConstraintNetwork conflict = metaVariable.getConstraintNetwork();
		Constraint[] cons = conflict.getConstraints();
		ConstraintNetwork[] dcs = new ConstraintNetwork[((DistanceConstraint)cons[0]).getInternalConstraints().length];
		Variable from = ((DistanceConstraint)cons[0]).getFrom();
		Variable to = ((DistanceConstraint)cons[0]).getTo();
		ConstraintSolver groundSolver = getGroundSolver();//from.getConstraintSolver();
		for (int i = 0; i < dcs.length; i++) {
			SimpleDistanceConstraint sdc = (SimpleDistanceConstraint)((DistanceConstraint)cons[0]).getInternalConstraints()[i];
			Bounds interval = new Bounds(sdc.getMinimum(), sdc.getMaximum());
			DistanceConstraint dc = new DistanceConstraint(interval);
			dc.setFrom(from);
			dc.setTo(to);
			ConstraintNetwork dcn = new ConstraintNetwork(groundSolver);
			dcn.addVariable(from);
			dcn.addVariable(to);
			dcn.addConstraint(dc);
			dcs[i] = dcn;
		}
		return dcs; 
	}


	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.getClass().getSimpleName();
	}

	@Override
	public void draw(ConstraintNetwork network) {
		// TODO Auto-generated method stub

	}

	@Override
	public void markResolvedSub(MetaVariable con, ConstraintNetwork metaValue) {
		Constraint[] dcs = con.getConstraintNetwork().getConstraints();
		MultiConstraint mc = (MultiConstraint)dcs[0];
		mc.setPropagateImmediately();
	}

	@Override
	public String getEdgeLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEquivalent(Constraint c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ConstraintSolver getGroundSolver() {
		return metaCS.getConstraintSolvers()[0];
	}

}

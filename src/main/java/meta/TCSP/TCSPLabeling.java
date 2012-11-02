package meta.TCSP;

import java.util.Vector;

import multi.TCSP.DistanceConstraint;
import multi.TCSP.DistanceConstraintNetwork;
import time.Bounds;
import time.SimpleDistanceConstraint;
import framework.Constraint;
import framework.ConstraintNetwork;
import framework.ConstraintSolver;
import framework.ValueOrderingH;
import framework.Variable;
import framework.VariableOrderingH;
import framework.meta.MetaConstraint;
import framework.meta.MetaVariable;
import framework.multi.MultiConstraint;

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
		DistanceConstraintNetwork dcn = (DistanceConstraintNetwork)metaCS.getConstraintSolvers()[0].getConstraintNetwork();
		Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
		//ConstraintNetwork[] ret = new ConstraintNetwork[dcn.getConstraints().length];
		for (Constraint con : dcn.getConstraints()) {
			//Only get variables representing constraints that are not propagated
			DistanceConstraint dc = (DistanceConstraint)con;
			//if (dc.getInternalConstraints().length > 1 && !dc.isPropagated()) {
			if (!dc.propagateImmediately()) {
				DistanceConstraintNetwork oneEdge = new DistanceConstraintNetwork(dc.getFrom().getConstraintSolver());
				oneEdge.addVariable(dc.getFrom());
				oneEdge.addVariable(dc.getTo());
				oneEdge.addConstraint(dc);
				ret.add(oneEdge);
			}
		}
		return ret.toArray(new ConstraintNetwork[ret.size()]);
	}

	@Override
	public ConstraintNetwork[] getMetaValues(MetaVariable metaVariable, int initialTime) {
		return getMetaValues(metaVariable);
	}
	
	
	@Override
	//public ConstraintNetwork[] getMetaValues(ConstraintNetwork conflict) {
	public ConstraintNetwork[] getMetaValues(MetaVariable metaVariable) {
		ConstraintNetwork conflict = metaVariable.getConstraintNetwork();
		Constraint[] cons = conflict.getConstraints();
		ConstraintNetwork[] dcs = new ConstraintNetwork[((DistanceConstraint)cons[0]).getInternalConstraints().length];
		Variable from = ((DistanceConstraint)cons[0]).getFrom();
		Variable to = ((DistanceConstraint)cons[0]).getTo();
		ConstraintSolver groundSolver = from.getConstraintSolver();
		for (int i = 0; i < dcs.length; i++) {
			SimpleDistanceConstraint sdc = (SimpleDistanceConstraint)((DistanceConstraint)cons[0]).getInternalConstraints()[i];
			Bounds interval = new Bounds(sdc.getMinimum(), sdc.getMaximum());
			DistanceConstraint dc = new DistanceConstraint(interval);
			dc.setFrom(from);
			dc.setTo(to);
			DistanceConstraintNetwork dcn = new DistanceConstraintNetwork(groundSolver);
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

}

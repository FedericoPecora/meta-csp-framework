package org.metacsp.meta.hybridPlanner;

import java.util.HashMap;
import java.util.Vector;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.VariableOrderingH;
import org.metacsp.framework.meta.MetaConstraint;
import org.metacsp.framework.meta.MetaVariable;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenInterval;
import org.metacsp.multi.spatioTemporal.SpatialFluent;
import org.metacsp.multi.spatioTemporal.SpatialFluentSolver;
import org.metacsp.time.Bounds;

public class MetaInverseReachabilityConstraint extends MetaConstraint{

	public MetaInverseReachabilityConstraint(VariableOrderingH varOH, ValueOrderingH valOH) {
		super(varOH, valOH);
		// TODO Auto-generated constructor stub
	}

	@Override
	public ConstraintNetwork[] getMetaVariables() {


		Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
		for (int i = 0; i < getGroundSolver().getVariables().length; i++) {
			SpatialFluent manFlunet = ((SpatialFluent)getGroundSolver().getVariables()[i]);
			AllenInterval intervalX = ((AllenInterval)manFlunet.getRectangularRegion().getInternalVariables()[0]);
			AllenInterval intervalY = ((AllenInterval)manFlunet.getRectangularRegion().getInternalVariables()[1]);
			if(!manFlunet.getActivity().getSymbolicVariable().getSymbols()[0].contains("manipulationArea"))//has to replaced with proper typed constraint checking no hard coding!!!!
				continue;
			if(isUnboundedBoundingBox(
					new Bounds(intervalX.getEST(), intervalX.getLST()), new Bounds(intervalX.getEET(), intervalX.getLET()), 
					new Bounds(intervalY.getEST(), intervalY.getLST()), new Bounds(intervalY.getEET(), intervalY.getLET()))){
				System.out.println("SPATIALLY UNBOUND Manipulation Fluent: " + manFlunet);
				ConstraintNetwork nw = new ConstraintNetwork(null);
				nw.addVariable(manFlunet);
				ret.add(nw);				
			}
		}

		return ret.toArray(new ConstraintNetwork[ret.size()]);
	}


	private boolean isUnboundedBoundingBox(Bounds xLB, Bounds xUB, Bounds yLB, Bounds yUB) {

		long horizon = ((ActivityNetworkSolver)((SpatialFluentSolver)this.metaCS.getConstraintSolvers()[0]).getConstraintSolvers()[1]).getHorizon();

		if( (xLB.min == 0 && xLB.max == horizon) && (xUB.min == 0&& xUB.max == horizon) &&
				(yLB.min == 0 && yLB.max == horizon) &&(yLB.min == 0 && yUB.max == horizon))
			return true;

		return false;
	}
	@Override
	public ConstraintNetwork[] getMetaValues(MetaVariable metaVariable) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ConstraintNetwork[] getMetaValues(MetaVariable metaVariable,
			int initial_time) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void markResolvedSub(MetaVariable metaVariable,
			ConstraintNetwork metaValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void draw(ConstraintNetwork network) {
		// TODO Auto-generated method stub

	}

	@Override
	public ConstraintSolver getGroundSolver() {
		return ((SpatialFluentSolver)metaCS.getConstraintSolvers()[0]);
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
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

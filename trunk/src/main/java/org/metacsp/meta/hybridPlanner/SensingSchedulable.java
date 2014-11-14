package org.metacsp.meta.hybridPlanner;

import java.util.Vector;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.VariableOrderingH;
import org.metacsp.framework.meta.MetaConstraint;
import org.metacsp.framework.meta.MetaVariable;
import org.metacsp.multi.activity.SymbolicVariableActivity;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.spatioTemporal.SpatialFluentSolver;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;

public class SensingSchedulable extends MetaConstraint{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5232587914603939099L;
	long beforeParameter = 1;
	public SensingSchedulable(VariableOrderingH varOH, ValueOrderingH valOH) {
		super(varOH, valOH);
		// TODO Auto-generated constructor stub
	}

	
	private ConstraintNetwork[] binaryPeakCollection(Vector<SymbolicVariableActivity> activities) {
		
		if (activities != null && !activities.isEmpty()) {
			Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
			logger.finest("Doing binary peak collection with " + activities.size() + " activities...");
			SymbolicVariableActivity[] groundVars = activities.toArray(new SymbolicVariableActivity[activities.size()]);
			
//			for (Activity a : groundVars) {
//				if (isConflicting(new Activity[] {a})) {
//					ConstraintNetwork cn = new ConstraintNetwork(null);
//					cn.addVariable(a);
//					ret.add(cn);
//				}
//			}
			if (!ret.isEmpty()) {
				return ret.toArray(new ConstraintNetwork[ret.size()]);
			}
			for (int i = 0; i < groundVars.length-1; i++) {
				for (int j = i+1; j < groundVars.length; j++) {
					Bounds bi = new Bounds(groundVars[i].getTemporalVariable().getEST(), groundVars[i].getTemporalVariable().getEET());
					Bounds bj = new Bounds(groundVars[j].getTemporalVariable().getEST(), groundVars[j].getTemporalVariable().getEET());
					if (bi.intersectStrict(bj) != null) {
						ConstraintNetwork cn = new ConstraintNetwork(null);
						cn.addVariable(groundVars[i]);
						cn.addVariable(groundVars[j]);
						ret.add(cn);
					}
				}
			}
			if (!ret.isEmpty()) {
				return ret.toArray(new ConstraintNetwork[ret.size()]);			
			}
		}
		return (new ConstraintNetwork[0]);
	}

	@Override
	public ConstraintNetwork[] getMetaVariables() {
		
		Vector<SymbolicVariableActivity> activities = new Vector<SymbolicVariableActivity>();
		for (int i = 0; i < ((SpatialFluentSolver)this.metaCS.getConstraintSolvers()[0]).getConstraintSolvers()[1].getVariables().length; i++) {
			SymbolicVariableActivity act = (SymbolicVariableActivity)((SpatialFluentSolver)this.metaCS.getConstraintSolvers()[0]).getConstraintSolvers()[1].getVariables()[i];			
			if(act.getSymbolicVariable().getSymbols()[0].toString().contains("sens")){
				activities.add(act);
			}
		}
		
		
		return binaryPeakCollection(activities);
	}

	@Override
	public ConstraintNetwork[] getMetaValues(MetaVariable metaVariable) {

		ConstraintNetwork conflict = metaVariable.getConstraintNetwork();
		//we know that is the result of binary conflict! so it is safe not to enumerate all, and hard coded
		Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
		
		AllenIntervalConstraint before01 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, new Bounds(beforeParameter, APSPSolver.INF));
		before01.setFrom((SymbolicVariableActivity) conflict.getVariables()[0]);			
		before01.setTo((SymbolicVariableActivity) conflict.getVariables()[1]);
		ConstraintNetwork resolver0 = new ConstraintNetwork(((SpatialFluentSolver)this.metaCS.getConstraintSolvers()[0]).getConstraintSolvers()[1]);
		resolver0.addVariable((SymbolicVariableActivity) conflict.getVariables()[0]);
		resolver0.addVariable((SymbolicVariableActivity) conflict.getVariables()[1]);
		resolver0.addConstraint(before01);
		ret.add(resolver0);
		
		AllenIntervalConstraint before10 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, new Bounds(beforeParameter, APSPSolver.INF));
		before10.setFrom((SymbolicVariableActivity) conflict.getVariables()[1]);			
		before10.setTo((SymbolicVariableActivity) conflict.getVariables()[0]);
		ConstraintNetwork resolver = new ConstraintNetwork(((SpatialFluentSolver)this.metaCS.getConstraintSolvers()[0]).getConstraintSolvers()[1]);
		resolver.addVariable((SymbolicVariableActivity) conflict.getVariables()[1]);
		resolver.addVariable((SymbolicVariableActivity) conflict.getVariables()[0]);
		resolver.addConstraint(before10);
		ret.add(resolver);
		
		return ret.toArray(new ConstraintNetwork[ret.size()]);
	
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

package org.metacsp.meta.hybridPlanner;

import java.util.HashMap;
import java.util.Vector;


import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariablePrototype;
import org.metacsp.framework.meta.MetaConstraint;
import org.metacsp.framework.meta.MetaConstraintSolver;
import org.metacsp.framework.meta.MetaVariable;
import org.metacsp.meta.simplePlanner.SimpleDomain.markings;
import org.metacsp.meta.simplePlanner.SimpleOperator;
import org.metacsp.meta.simplePlanner.SimpleReusableResource;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.spatial.rectangleAlgebra.BoundingBox;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangleConstraint;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangularRegion;
import org.metacsp.multi.spatial.rectangleAlgebra.UnaryRectangleConstraint;
import org.metacsp.multi.spatioTemporal.SpatialFluent;
import org.metacsp.multi.spatioTemporal.SpatialFluentSolver;
import org.metacsp.multi.symbols.SymbolicValueConstraint;

public class SimpleHybridPlanner extends MetaConstraintSolver {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long horizon = 0;
	public Vector<SimpleOperator> operatorsAlongBranch = new Vector<SimpleOperator>();

	
	public SimpleHybridPlanner(long origin, long horizon, long animationTime) {
		super(new Class[] {RectangleConstraint.class, UnaryRectangleConstraint.class, AllenIntervalConstraint.class, SymbolicValueConstraint.class}, 
				animationTime, new SpatialFluentSolver(origin, horizon)	);
		this.horizon = horizon;
	}


	@Override
	public void preBacktrack() {
	}

	@Override
	public void postBacktrack(MetaVariable mv) {
		
		if (mv.getMetaConstraint() instanceof FluentBasedSimpleDomain)
			for (Variable v : mv.getConstraintNetwork().getVariables()) {
				v.setMarking(markings.UNJUSTIFIED);
			}

		
	}

	@Override
	protected void retractResolverSub(ConstraintNetwork metaVariable, ConstraintNetwork metaValue) {
		
		if (metaValue.annotation != null && metaValue.annotation instanceof SimpleOperator) {
			this.operatorsAlongBranch.remove(operatorsAlongBranch.size()-1);
//			System.out.println("-------------------> popped " + metaValue.annotation);
		}

		ActivityNetworkSolver groundSolver = (ActivityNetworkSolver)((SpatialFluentSolver)this.getConstraintSolvers()[0]).getConstraintSolvers()[1];
		Vector<Variable> activityToRemove = new Vector<Variable>();

		for (Variable v : metaValue.getVariables()) {
			if (!metaVariable.containsVariable(v)) {
				if (v instanceof VariablePrototype) {
					Variable vReal = metaValue.getSubstitution((VariablePrototype)v);
					if (vReal != null) {
						activityToRemove.add(vReal);
					}
				}
			}
		}

		for (int j = 0; j < this.metaConstraints.size(); j++){ 
			if(this.metaConstraints.get(j) instanceof FluentBasedSimpleDomain ){
				FluentBasedSimpleDomain mcc = (FluentBasedSimpleDomain)this.metaConstraints.get(j);
				for (Variable v : activityToRemove) {
					for (SimpleReusableResource rr : mcc.getCurrentReusableResourcesUsedByActivity((Activity)v)) {
						rr.removeUsage((Activity)v);
					}
				}
			}
		}
		
		boolean isRtractingSpatialRelations = false;
		for (int i = 0; i < metaValue.getVariables().length; i++) {
			if(metaValue.getVariables()[i] instanceof RectangularRegion ){
				isRtractingSpatialRelations = true;
				break;
			}
		}
					
		
		if(isRtractingSpatialRelations){
			Vector<SpatialFluent> spatialFluentToBeRemoved = new Vector<SpatialFluent>();
			System.out.println("Meta Value of MetaSpatialConstraint is retracted");
			
			for (int i = 0; i < this.getConstraintSolvers()[0].getVariables().length; i++) {
				if(((Activity)((SpatialFluent)((SpatialFluentSolver)this.getConstraintSolvers()[0]).getVariables()[i]).getActivity()).getTemporalVariable().getEST() == 0 &&
						((Activity)((SpatialFluent)((SpatialFluentSolver)this.getConstraintSolvers()[0]).getVariables()[i]).getActivity()).getTemporalVariable().getLST() == horizon){
					spatialFluentToBeRemoved.add((SpatialFluent)((SpatialFluentSolver)this.getConstraintSolvers()[0]).getVariables()[i]);
//					System.out.println((SpatialFluent)((SpatialFluentSolver)this.getConstraintSolvers()[0]).getVariables()[i]);
				}
			}
			
			for (int i = 0; i < this.metaConstraints.size(); i++){
				if(this.metaConstraints.get(i) instanceof MetaSpatialAdherenceConstraint ){	
					for (int j = 0; j < ((MetaSpatialAdherenceConstraint)this.metaConstraints.get(i)).getsAssertionalRels().size(); j++) {
							((MetaSpatialAdherenceConstraint)this.metaConstraints.get(i)).getsAssertionalRels().get(j).setUnaryAtRectangleConstraint
							(((MetaSpatialAdherenceConstraint)this.metaConstraints.get(i)).getCurrentAssertionalCons().
									get(((MetaSpatialAdherenceConstraint)this.metaConstraints.get(i)).getsAssertionalRels().get(j).getFrom()));
//							System.out.println("Assertional Realtion: " + (((SpatialSchedulable)this.metaConstraints.get(i)).getCurrentAssertionalCons().
//									get(((SpatialSchedulable)this.metaConstraints.get(i)).getsAssertionalRels()[j].getFrom())));
					}			
				}
			}
			((SpatialFluentSolver)this.getConstraintSolvers()[0]).removeVariables(spatialFluentToBeRemoved.toArray(new Variable[spatialFluentToBeRemoved.size()]));
		}
		
		
		groundSolver.removeVariables(activityToRemove.toArray(new Variable[activityToRemove.size()]));
		
		
	}
	
	
	
	@Override
	protected boolean addResolverSub(ConstraintNetwork metaVariable, ConstraintNetwork metaValue) {
				
		if (metaValue.annotation != null && metaValue.annotation instanceof SimpleOperator) {
			if (operatorsAlongBranch.contains((metaValue.annotation))) {
//				boolean isSkipable = false;
//				for (int i = 0; i < getRepeatedActivitySymbolicDomain().size(); i++) {
//					if(((Activity)metaVariable.getVariables()[0]).getSymbolicVariable().getSymbols()[0].compareTo(getRepeatedActivitySymbolicDomain().get(i)) == 0){
//						System.out.println("-------------------> NOT skipped " + metaVariable);
//						isSkipable = true;
//					}
//				}
//				if(!isSkipable)
				{
//					System.out.println("-------------------> skipped " + metaValue.annotation);
//					System.out.println("-------------------> skipped " + metaVariable);
					return false;					
				}
			}
			operatorsAlongBranch.add((SimpleOperator)metaValue.annotation);
//			System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
//			System.out.println(operatorsAlongBranch);
//			System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
//			System.out.println("-------------------> pushed " + metaValue.annotation);
		}
				
		ActivityNetworkSolver groundSolver = (ActivityNetworkSolver)((SpatialFluentSolver)this.getConstraintSolvers()[0]).getConstraintSolvers()[1];

		//Make real variables from variable prototypes
		for (Variable v :  metaValue.getVariables()) {
			if (v instanceof VariablePrototype) {
				// 	Parameters for real instantiation: the first is the component itself, the second is
				//	the symbol of the Activity to be instantiated
				String component = (String)((VariablePrototype) v).getParameters()[0];
				String symbol = (String)((VariablePrototype) v).getParameters()[1];
				Activity tailActivity = (Activity)groundSolver.createVariable(component);
				tailActivity.setSymbolicDomain(symbol);
				tailActivity.setMarking(v.getMarking());
				metaValue.addSubstitution((VariablePrototype)v, tailActivity);
			}
		}
		

		
		//Involve real variables in the constraints
		for (Constraint con : metaValue.getConstraints()) {
			Constraint clonedConstraint = (Constraint)con.clone();  
			Variable[] oldScope = con.getScope();
			Variable[] newScope = new Variable[oldScope.length];
			for (int i = 0; i < oldScope.length; i++) {
				if (oldScope[i] instanceof VariablePrototype) newScope[i] = metaValue.getSubstitution((VariablePrototype)oldScope[i]);
				else newScope[i] = oldScope[i];
			}
			clonedConstraint.setScope(newScope);
			metaValue.removeConstraint(con);
			metaValue.addConstraint(clonedConstraint);
		}
		

		
		for (Variable v : metaValue.getVariables()) {
			for (int j = 0; j < this.metaConstraints.size(); j++) {
				if(this.metaConstraints.get(j) instanceof FluentBasedSimpleDomain ){
					FluentBasedSimpleDomain metaCausalConatraint = (FluentBasedSimpleDomain)this.metaConstraints.elementAt(j);
					for (SimpleReusableResource rr : metaCausalConatraint.getCurrentReusableResourcesUsedByActivity(v)) {
						rr.setUsage((Activity)v);
					}
				}
			}
		}
		
		
		return true;
	}
		

	@Override
	protected double getUpperBound() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void setUpperBound() {
		// TODO Auto-generated method stub

	}

	@Override
	protected double getLowerBound() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void setLowerBound() {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean hasConflictClause(ConstraintNetwork metaValue) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void resetFalseClause() {
		// TODO Auto-generated method stub

	}
	
	public HashMap<String, BoundingBox> getOldRectangularRegion(){
				
		for (int j = 0; j < this.metaConstraints.size(); j++){ 
			if(this.metaConstraints.get(j) instanceof MetaSpatialAdherenceConstraint ){
				return ((MetaSpatialAdherenceConstraint)this.metaConstraints.get(j)).getOldRectangularRegion();
			}
		}
		return null;
	}
	


}

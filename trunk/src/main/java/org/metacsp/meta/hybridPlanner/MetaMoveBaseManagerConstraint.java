package org.metacsp.meta.hybridPlanner;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Vector;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariableOrderingH;
import org.metacsp.framework.VariablePrototype;
import org.metacsp.framework.meta.MetaConstraint;
import org.metacsp.framework.meta.MetaVariable;
import org.metacsp.meta.simplePlanner.SimpleDomain.markings;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenInterval;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.spatial.rectangleAlgebra.BoundingBox;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangularRegion;
import org.metacsp.multi.spatioTemporal.SpatialFluent;
import org.metacsp.multi.spatioTemporal.SpatialFluentSolver;
import org.metacsp.spatial.utility.SpatialRule;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;

public class MetaMoveBaseManagerConstraint extends MetaConstraint{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1829628266124534057L;
	long beforeParameter = 1;
	public MetaMoveBaseManagerConstraint(VariableOrderingH varOH,
			ValueOrderingH valOH) {
		super(varOH, valOH);

	}

	@Override
	public ConstraintNetwork[] getMetaVariables() {
		
//		System.out.println("===================================================");
//
//		ActivityNetworkSolver actSolver = ((ActivityNetworkSolver)((SpatialFluentSolver)getGroundSolver()).getConstraintSolvers()[1]);
//		
//		HashMap<Activity, Long> starttimes = new HashMap<Activity, Long>();
//		for (int i = 0; i < actSolver.getVariables().length; i++) {
//			starttimes.put((Activity) actSolver.getVariables()[i], ((Activity)actSolver.getVariables()[i]).getTemporalVariable().getStart().getLowerBound());                       
//		}
//
//		//          Collections.sort(starttimes.values());
//		starttimes =  sortHashMapByValuesD(starttimes);
//		for (Activity act : starttimes.keySet()) {
//			System.out.println(act + " --> " + starttimes.get(act));
//		}
//		
//		System.out.println("===================================================");

		HashMap<Activity, SpatialFluent> activityToFluent = new HashMap<Activity, SpatialFluent>();
		Vector<Activity> activities = new Vector<Activity>();
		for (int i = 0; i < getGroundSolver().getVariables().length; i++) {
			if(((SpatialFluent)getGroundSolver().getVariables()[i]).getActivity().getSymbolicVariable().getSymbols()[0].contains("manipulationArea")){
				activities.add(((SpatialFluent)getGroundSolver().getVariables()[i]).getActivity());
				activityToFluent.put(((SpatialFluent)(getGroundSolver()).getVariables()[i]).getActivity(), 
				((SpatialFluent)getGroundSolver().getVariables()[i]));
			}
		}
		
//		System.out.println("===================================================");
//		System.out.println("activities: " + activityToFluent);
//		System.out.println("===================================================");
		return binaryPeakCollection(activityToFluent);
	}

	private ConstraintNetwork[] binaryPeakCollection(HashMap<Activity, SpatialFluent> aTOsf) {
		
		
		Vector<Activity> activities = new Vector<Activity>();
		for (Activity act : aTOsf.keySet()) {
			activities.add(act);
		}
		
		
		if (activities != null && !activities.isEmpty()) {
			Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
			logger.finest("Doing binary peak collection with " + activities.size() + " activities...");
			Activity[] groundVars = activities.toArray(new Activity[activities.size()]);
//			for (Activity a : groundVars) {
//				if (isConflicting(new Activity[] {a}, aTOsf)) {
//					ConstraintNetwork cn = new ConstraintNetwork(null);
//					cn.addVariable(a);
//					ret.add(cn);
//				}
//			}

			for (int i = 0; i < groundVars.length-1; i++) {
				for (int j = i+1; j < groundVars.length; j++) {
					Bounds bi = new Bounds(groundVars[i].getTemporalVariable().getEST(), groundVars[i].getTemporalVariable().getEET());
					Bounds bj = new Bounds(groundVars[j].getTemporalVariable().getEST(), groundVars[j].getTemporalVariable().getEET());
					if ((bi.max + 1 == bj.min || bj.max + 1 == bi.min)  && isConflicting(new Activity[] {groundVars[i], groundVars[j]}, aTOsf)) {
//						System.out.println("===================================");
//						System.out.println(groundVars[i]);
//						System.out.println(groundVars[j]);
////						System.out.println("bi" + bi);
////						System.out.println("bj" + bj);
//						System.out.println("===================================");
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
	
	
	private boolean isConflicting(Activity[] peak, HashMap<Activity, SpatialFluent> aTOsf) {
//		System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
		if(peak.length == 1){
			System.out.println("I am returnin here " + peak[0]);
			return false;
			
		}
		
		
//		System.out.println("peak1: " + peak[0]);
//		System.out.println("peak1: " + peak[1]);
		
		Rectangle[] recs = new Rectangle[peak.length];
		for (int i = 0; i < peak.length; i++) {
			RectangularRegion r = aTOsf.get(peak[i]).getRectangularRegion();
			AllenInterval intervalX = (AllenInterval)r.getInternalVariables()[0];
			AllenInterval intervalY = ((AllenInterval)r.getInternalVariables()[1]);
			
			BoundingBox manipulationBB = new BoundingBox(new Bounds(intervalX.getEST(), intervalX.getLST()), new Bounds(intervalX.getEET(), intervalX.getLET()), 
					new Bounds(intervalY.getEST(), intervalY.getLST()), new Bounds(intervalY.getEET(), intervalY.getLET()));
			recs[i] = manipulationBB.getAlmostCentreRectangle(); 
		}

		//since it is binary sampling it should be just two
		//it has to be more complicated, this is very simple..it should be overlapped a lot otherwise the robot has to be moved
		//both center of two area has to be in the intersection, otherwise it has to be moved

		if((recs[0].height == 0 && recs[0].width == 0) || (recs[1].height == 0 && recs[1].width == 0)) {
			//the position is not assigned yet
//			System.out.println("Height of ZERO");
			return false;
		}
			
		
//		System.out.println("++++ rec1 ++++ " + recs[0]);
//		System.out.println("++++ rec2 ++++ " + recs[1]);
		if(!recs[0].intersects(recs[1])){
//			System.out.println("they dont intersect");
//			System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
			return true;
		}
			
//		System.out.println("they intersect");
//		System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
		return false;
	}

	@Override
	public ConstraintNetwork[] getMetaValues(MetaVariable metaVariable) {
		
		ConstraintNetwork conflict = metaVariable.getConstraintNetwork();
		//we know that is the result of binary conflict! so it is safe not to enumerate all, and hard coded
		Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
		Activity act1 = (Activity) conflict.getVariables()[0];
		Activity act2 = (Activity) conflict.getVariables()[1];
		
		
//		" (Head atLocation::at_robot1_manipulationArea_"+ objVar[i] +"_table1())" +  " \n"+
//		" (RequiredState req1 RobotAction::moveTo_manipulationArea_"+ objVar[i] +"())" +  " \n"+
//		" (Constraint MetBy(Head,req1))" +  " \n"+
		
//		" (Constraint Duration[" + duration +",INF](Head))" +  " \n"+
//		" (RequiredResource robot1(1)) " +  " \n"+

		if(act1.getTemporalVariable().getEST() < act2.getTemporalVariable().getEST()){
			Activity move = (Activity)((SpatialFluentSolver)this.metaCS.getConstraintSolvers()[0]).getConstraintSolvers()[1].createVariable("RobotAction");
			String extract = act2.getSymbolicVariable().getSymbols()[0].substring(9);			
			String actname = "moveTo" + extract;
			move.setSymbolicDomain(actname);
			move.setMarking(markings.UNJUSTIFIED);
			AllenIntervalConstraint moveMetByManFluent = new AllenIntervalConstraint(AllenIntervalConstraint.Type.MetBy, AllenIntervalConstraint.Type.MetBy.getDefaultBounds());
			moveMetByManFluent.setFrom(act2);
			moveMetByManFluent.setTo(move);
			ConstraintNetwork resolver0 = new ConstraintNetwork(((SpatialFluentSolver)this.metaCS.getConstraintSolvers()[0]).getConstraintSolvers()[1]);
			resolver0.addVariable(act1);
			resolver0.addVariable(act2);
			resolver0.addVariable(move);
			resolver0.addConstraint(moveMetByManFluent);

			
//			AllenIntervalConstraint moveMetByManFluent1 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.MetBy, AllenIntervalConstraint.Type.MetBy.getDefaultBounds());
//			moveMetByManFluent1.setFrom(move);
//			moveMetByManFluent1.setTo(act1);
//			resolver0.addConstraint(moveMetByManFluent1);
			
			
			ret.add(resolver0);
		}
		else{
			Activity move = (Activity)((SpatialFluentSolver)this.metaCS.getConstraintSolvers()[0]).getConstraintSolvers()[1].createVariable("RobotAction");
			String extract = act1.getSymbolicVariable().getSymbols()[0].substring(9);
			String actname = "moveTo" + extract;
			move.setSymbolicDomain(actname);
			move.setMarking(markings.UNJUSTIFIED);
			AllenIntervalConstraint moveMetByManFluent = new AllenIntervalConstraint(AllenIntervalConstraint.Type.MetBy, AllenIntervalConstraint.Type.MetBy.getDefaultBounds());
			moveMetByManFluent.setFrom(act1);
			moveMetByManFluent.setTo(move);
			ConstraintNetwork resolver0 = new ConstraintNetwork(((SpatialFluentSolver)this.metaCS.getConstraintSolvers()[0]).getConstraintSolvers()[1]);
			resolver0.addVariable(act1);
			resolver0.addVariable(act2);
			resolver0.addVariable(move);
			resolver0.addConstraint(moveMetByManFluent);
			
//			AllenIntervalConstraint moveMetByManFluent1 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.MetBy, AllenIntervalConstraint.Type.MetBy.getDefaultBounds());
//			moveMetByManFluent1.setFrom(move);
//			moveMetByManFluent1.setTo(act1);
//			resolver0.addConstraint(moveMetByManFluent1);
			
			ret.add(resolver0);
		}
		
//		System.out.println("%%%%%%% RET %%%%%%%%%%%" + ret);
		
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
		return "MetaMoveBaseManagerConstraint";
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
	
	private static LinkedHashMap sortHashMapByValuesD(HashMap passedMap) {
		ArrayList mapKeys = new ArrayList(passedMap.keySet());
		ArrayList mapValues = new ArrayList(passedMap.values());
		Collections.sort(mapValues);
		Collections.sort(mapKeys);

		LinkedHashMap sortedMap =  new LinkedHashMap();

		Iterator valueIt = ((java.util.List<SpatialRule>) mapValues).iterator();
		while (valueIt.hasNext()) {
			long val = (Long) valueIt.next();
			Iterator keyIt = ((java.util.List<SpatialRule>) mapKeys).iterator();

			while (keyIt.hasNext()) {
				Activity key = (Activity) keyIt.next();
				long comp1 = (Long) passedMap.get(key);
				long comp2 = val;

				if (comp1 == comp2){
					passedMap.remove(key);
					mapKeys.remove(key);
					sortedMap.put(key, val);
					break;
				}
			}
		}
		return sortedMap;
	}

}

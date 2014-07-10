package org.metacsp.meta.hybridPlanner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.VariableOrderingH;
import org.metacsp.framework.meta.MetaConstraint;
import org.metacsp.framework.meta.MetaVariable;
import org.metacsp.framework.multi.MultiBinaryConstraint;
import org.metacsp.meta.simplePlanner.SimpleDomain.markings;
import org.metacsp.meta.simplePlanner.SimpleOperator;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityComparator;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.allenInterval.AllenIntervalNetworkSolver;
import org.metacsp.multi.spatial.rectangleAlgebra.BoundingBox;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangleConstraint;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangleConstraintSolver;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangularRegion;
import org.metacsp.multi.spatial.rectangleAlgebra.UnaryRectangleConstraint;
import org.metacsp.multi.spatioTemporal.SpatialFluent;
import org.metacsp.multi.spatioTemporal.SpatialFluentSolver;
import org.metacsp.spatial.reachability.ConfigurationVariable;
import org.metacsp.spatial.reachability.ReachabilityConstraint;
import org.metacsp.spatial.utility.SpatialAssertionalRelation;
import org.metacsp.spatial.utility.SpatialRule;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;
import org.metacsp.utility.PermutationsWithRepetition;


public class MetaSpatialAdherenceConstraint extends MetaConstraint {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1598461825401250494L;
	private long origin = 0, horizon = 1000;
	private Vector<SpatialAssertionalRelation> sAssertionalRels = new Vector<SpatialAssertionalRelation>();
	private SpatialRule[] rules;
	private HashMap<HashMap<String, Bounds[]>, Integer> permutation;
	private Vector<String> initialUnboundedObjName = new Vector<String>();
	private Vector<String> potentialCulprit; 
	private HashMap<String, UnaryRectangleConstraint> currentAssertionalCons;
	//	private Vector<HashMap<String, BoundingBox>> newRectangularRegion = null;
	private HashMap<String, BoundingBox> oldRectangularRegion = null;
	//	private HashMap<Activity, SpatialFluent> activityToFluent = new HashMap<Activity, SpatialFluent>();
	//	private HashMap<Activity, SpatialFluent> activityToFluent;
	//	protected Vector<Activity> activities;
	private long totalTime = 0; 

	public long getCulpritDetectionTime(){
		return totalTime;
	}

	private int numberOfmisplaced = 0;
	public int getNumberofMisplaced(){

		return numberOfmisplaced;

	}

	public HashMap<String, UnaryRectangleConstraint> getCurrentAssertionalCons(){
		return currentAssertionalCons;
	}



	public HashMap<String, BoundingBox> getOldRectangularRegion(){
		return oldRectangularRegion;
	}


	public MetaSpatialAdherenceConstraint(VariableOrderingH varOH, ValueOrderingH valOH) {
		super(varOH, valOH);
		this.beforeParameter = 1;
	}

	public void setSpatialRules(SpatialRule... rules) {
		this.rules = new SpatialRule[rules.length];
		this.rules = rules;
	}

	//	public void setSpatialAssertionalRelations(SpatialAssertionalRelation2... sAssertionalRels) {
	//		this.sAssertionalRels = new SpatialAssertionalRelation2[sAssertionalRels.length];
	//		this.sAssertionalRels = sAssertionalRels;
	//	}

	public void setSpatialAssertionalRelations(Vector<SpatialAssertionalRelation> sAssertionalRels) {
		this.sAssertionalRels.clear();
		this.sAssertionalRels = sAssertionalRels;
	}


	public Vector<SpatialAssertionalRelation> getsAssertionalRels() {
		return sAssertionalRels;
	}


	public int getBeforeParameter() {
		return beforeParameter;
	}

	public void setBeforeParameter(int beforeParameter) {
		this.beforeParameter = beforeParameter;
	}

	int beforeParameter;

	public PEAKCOLLECTION getPeakCollectionStrategy() {
		return peakCollectionStrategy;
	}

	public void setPeakCollectionStrategy(PEAKCOLLECTION peakCollectionStrategy) {
		this.peakCollectionStrategy = peakCollectionStrategy;
	}



	public static enum PEAKCOLLECTION {
		SAMPLING, COMPLETE, BINARY
	};

	protected PEAKCOLLECTION peakCollectionStrategy = PEAKCOLLECTION.SAMPLING;

	public static <T> Set<Set<T>> powerSet(Set<T> originalSet) {
		Set<Set<T>> sets = new HashSet<Set<T>>();
		if (originalSet.isEmpty()) {
			sets.add(new HashSet<T>());
			return sets;
		}
		List<T> list = new ArrayList<T>(originalSet);
		T head = list.get(0);
		Set<T> rest = new HashSet<T>(list.subList(1, list.size()));
		for (Set<T> set : powerSet(rest)) {
			Set<T> newSet = new HashSet<T>();
			newSet.add(head);
			newSet.addAll(set);
			sets.add(newSet);
			sets.add(set);
		}
		return sets;
	}




	// Finds sets of overlapping activities and assesses whether they are
	// conflicting (e.g., over-consuming a resource)
	private ConstraintNetwork[] samplingPeakCollection(HashMap<Activity, SpatialFluent> aTOsf) {



		Vector<Activity> observation = new Vector<Activity>();
		Vector<Activity> activities = new Vector<Activity>();
		for (Activity act : aTOsf.keySet()) {
			activities.add(act);
			if(act.getTemporalVariable().getEST() == act.getTemporalVariable().getLST())
				observation.add(act);
		}
		if (activities != null && !activities.isEmpty()) {

			Activity[] groundVars = activities.toArray(new Activity[activities.size()]);			
			Arrays.sort(groundVars, new ActivityComparator(true));
			Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
			Vector<Vector<Activity>> overlappingAll = new Vector<Vector<Activity>>();

			// if an activity is spatially inconsistent even with itself
			for (Activity act : activities) {
				if (isConflicting(new Activity[] { act }, aTOsf)) {
					ConstraintNetwork temp = new ConstraintNetwork(null);
					temp.addVariable(act);
					ret.add(temp);
				}
			}

			// groundVars are ordered activities
			for (int i = 0; i < groundVars.length; i++) {
				Vector<Activity> overlapping = new Vector<Activity>();
				overlapping.add(groundVars[i]);
				long start = (groundVars[i]).getTemporalVariable().getEST();
				long end = (groundVars[i]).getTemporalVariable().getEET();
				Bounds intersection = new Bounds(start, end);
				for (int j = 0; j < groundVars.length; j++) {
					if (i != j) {
						start = (groundVars[j]).getTemporalVariable().getEST();
						end = (groundVars[j]).getTemporalVariable().getEET();
						Bounds nextInterval = new Bounds(start, end);
						//						 System.out.println("nextinterval: " + groundVars[j] + " " +nextInterval);
						//						 System.out.println("____________________________________");
						Bounds intersectionNew = intersection.intersectStrict(nextInterval);
						if (intersectionNew != null) {
							overlapping.add(groundVars[j]);
							if (isConflicting(overlapping.toArray(new Activity[overlapping.size()]), aTOsf)) {
								overlappingAll.add(overlapping);
								if(overlapping.containsAll(observation))
									break;
							}
							else
								intersection = intersectionNew;
						}
					}
				}
			}
			if(overlappingAll.size() > 0){
				Vector<Vector<Activity>> retActivities = new Vector<Vector<Activity>>();
				Vector<Activity>  current = overlappingAll.get(0);
				for (int i = 1; i < overlappingAll.size(); i++) {
					if(!isEqual(current, overlappingAll.get(i))){
						retActivities.add(current);
						current = overlappingAll.get(i);
					}
				}
				retActivities.add(current);

				for (Vector<Activity> actVec : retActivities) {
					ConstraintNetwork tmp = new ConstraintNetwork(null);
					for (Activity act : actVec){
						tmp.addVariable(act);
					}
					ret.add(tmp);					
				}

				//				System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
				//				for (int i = 0; i < ret.size(); i++) {
				//					System.out.println("ret: " + ret);
				//				}
				//				System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

				//				return new ConstraintNetwork[]{ret.size()};
				return ret.toArray(new ConstraintNetwork[ret.size()]);
			}
		}
		return (new ConstraintNetwork[0]);
	}


	private boolean isEqual(Vector<Activity> current, Vector<Activity> next) {

		if(current.size() != next.size()) return false;

		int[] nextId = new int[next.size()]; 
		int[] currentId = new int[current.size()];
		for (int i = 0; i < next.size(); i++){ 
			nextId[i] = next.get(i).getID();
			currentId[i] = current.get(i).getID();
		}

		Arrays.sort(nextId);
		Arrays.sort(currentId);

		for (int i = 0; i < currentId.length; i++) {
			if(currentId[i] != nextId[i])
				return false;
		}

		return true;
	}


	@Override
	public ConstraintNetwork[] getMetaVariables() {
		HashMap<Activity, SpatialFluent> activityToFluent = new HashMap<Activity, SpatialFluent>();
		Vector<Activity> activities = new Vector<Activity>();
		for (int i = 0; i < ((SpatialFluentSolver)this.metaCS.getConstraintSolvers()[0]).getVariables().length; i++) {
			activities.add(((SpatialFluent)((SpatialFluentSolver)this.metaCS.getConstraintSolvers()[0]).getVariables()[i]).getActivity());
			activityToFluent.put(((SpatialFluent)((SpatialFluentSolver)this.metaCS.getConstraintSolvers()[0]).getVariables()[i]).getActivity(), 
					((SpatialFluent)((SpatialFluentSolver)this.metaCS.getConstraintSolvers()[0]).getVariables()[i]));
		}

		//		System.out.println("==========================================================================");
		//		System.out.println("activityToFluent: " + activityToFluent);
		//		System.out.println("==========================================================================");

		return samplingPeakCollection(activityToFluent);
		//		return completePeakCollection(activityToFluent);
	}

	@Override
	public ConstraintNetwork[] getMetaValues(MetaVariable metaVariable,
			int initialTime) {
		return getMetaValues(metaVariable);
	}

	@Override
	public ConstraintNetwork[] getMetaValues(MetaVariable metaVariable) {

		if (metaVariable == null)
			return null;

		//#######################################################################################################
		HashMap<Activity, SpatialFluent> activityToFluent = new HashMap<Activity, SpatialFluent>();
		Vector<Activity> activities = new Vector<Activity>();
		for (int i = 0; i < ((SpatialFluentSolver)this.metaCS.getConstraintSolvers()[0]).getVariables().length; i++) {
			activities.add(((SpatialFluent)((SpatialFluentSolver)this.metaCS.getConstraintSolvers()[0]).getVariables()[i]).getActivity());
			activityToFluent.put(((SpatialFluent)((SpatialFluentSolver)this.metaCS.getConstraintSolvers()[0]).getVariables()[i]).getActivity(), 
					((SpatialFluent)((SpatialFluentSolver)this.metaCS.getConstraintSolvers()[0]).getVariables()[i]));

		}
		//########################################################################################################
		permutation = new HashMap<HashMap<String, Bounds[]>, Integer>();
		potentialCulprit = new Vector<String>();
		Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
		ConstraintNetwork mvalue = new ConstraintNetwork(this.metaCS.getConstraintSolvers()[0]);
		ConstraintNetwork conflict = metaVariable.getConstraintNetwork();
		Vector<SpatialFluent> conflictvars = new Vector<SpatialFluent>();
		Vector<RectangularRegion> conflictRecvars = new Vector<RectangularRegion>();
		HashMap<String, RectangularRegion> getVariableByName = new HashMap<String, RectangularRegion>();
		//		newRectangularRegion = new Vector<HashMap<String, BoundingBox>>(); //this is for tracking the alternative position and check whether the new places is already overlapped by previous places 
		oldRectangularRegion = new HashMap<String, BoundingBox>();

		//#########################################################################################################

		for (int j = 0; j < conflict.getVariables().length; j++) {
			conflictvars.add(activityToFluent.get((Activity) conflict.getVariables()[j]));
			conflictRecvars.add(activityToFluent.get((Activity) conflict.getVariables()[j]).getRectangularRegion());
		}

		setPermutationHashMAP(conflictvars, conflictRecvars);//it only generate permutation, does not perform any propagation
		long timeNow = Calendar.getInstance().getTimeInMillis();
		Vector<HashMap<String, Bounds[]>> alternativeSets = generateAllAlternativeSet(conflictRecvars);//it ranks the alternative
		totalTime = (Calendar.getInstance().getTimeInMillis()-timeNow);
		HashMap<String, Bounds[]> alternativeSet = alternativeSets.get(0);



		// TBOX general knowledge in RectangleCN
		mvalue.join(createTBOXspatialNetwork(((SpatialFluentSolver) this.metaCS.getConstraintSolvers()[0]).getConstraintSolvers()[0],getVariableByName)); 
		// Att At cpnstraint
		Vector<RectangularRegion> metaVaribales = new Vector<RectangularRegion>();		
		for (SpatialFluent var : conflictvars) {
			//skip fluents which are not in the general spatial knowledge
			if(alternativeSet.get(var.getRectangularRegion().getName()) == null) continue;

			Bounds[] atBounds = new Bounds[alternativeSet.get(var.getRectangularRegion().getName()).length];
			for (int j = 0; j < atBounds.length; j++) {
				Bounds at = new Bounds(alternativeSet.get(var.getName())[j].min,alternativeSet.get(var.getName())[j].max);
				atBounds[j] = at;
			}
			UnaryRectangleConstraint atCon = new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.At, atBounds);
			atCon.setFrom(var.getRectangularRegion());
			atCon.setTo(var.getRectangularRegion());

			metaVaribales.add(var.getRectangularRegion());
			mvalue.addConstraint(atCon);
			mvalue.addVariable(var.getRectangularRegion());
		}


		//###########################################################################################################
		//		logger.finest("pregenerated meta value for scoring: " + mvalue);

		//		System.out.println("mvalue" + mvalue);
		Vector<String> newGoal = new Vector<String>();
		HashMap<String, Activity> culpritActivities = new HashMap<String, Activity>(); 

		for (int i = 0; i < mvalue.getConstraints().length; i++) {
			if (mvalue.getConstraints()[i] instanceof UnaryRectangleConstraint) {
				// this if will check for unboudned obj in order to create the goal
				if (((UnaryRectangleConstraint) mvalue.getConstraints()[i])
						.getType().equals(UnaryRectangleConstraint.Type.At)) {
					if (this.isUnboundedBoundingBox(((UnaryRectangleConstraint) mvalue.getConstraints()[i]).getBounds()[0],
							((UnaryRectangleConstraint) mvalue.getConstraints()[i]).getBounds()[1],
							((UnaryRectangleConstraint) mvalue.getConstraints()[i]).getBounds()[2],
							((UnaryRectangleConstraint) mvalue.getConstraints()[i]).getBounds()[3])) {
						//						System.out.println("%%%%%%%%");
						for (int j = 0; j < metaVariable.getConstraintNetwork().getVariables().length; j++) {
							if (((RectangularRegion) mvalue.getConstraints()[i].getScope()[0]).getName().compareTo
									(((SpatialFluent) activityToFluent.get((Activity)metaVariable.getConstraintNetwork().getVariables()[j]) ).getName()) == 0) {
								//								System.out.println("ADDED ACTIVITY: " + ((Activity)(metaVariable.getConstraintNetwork().getVariables()[j])));								
								if (this.getPotentialCulprit().contains(((RectangularRegion) mvalue.getConstraints()[i].getScope()[0]).getName())) {


									if(((Activity)(metaVariable.getConstraintNetwork().getVariables()[j])).getTemporalVariable().getEST() == 
											((Activity)(metaVariable.getConstraintNetwork().getVariables()[j])).getTemporalVariable().getLST()){
										System.out.println(((RectangularRegion)mvalue.getConstraints()[i].getScope()[0]).getName()); //this has to uncommented
										//										System.out.println("==== " + ((Activity)(metaVariable.getConstraintNetwork().getVariables()[j])));
										culpritActivities.put(((RectangularRegion)mvalue.getConstraints()[i].getScope()[0]).getName(), 
												((Activity)(metaVariable.getConstraintNetwork().getVariables()[j])));
										if(!newGoal.contains(((RectangularRegion) mvalue.getConstraints()[i].getScope()[0]).getName()))
											newGoal.add(((RectangularRegion) mvalue.getConstraints()[i].getScope()[0]).getName());
									}
								}
							}
						}
					}
				}						
			}
		}
		//		System.out.println("%%%%%%%%");


		//extract the fluent which is relevant to the original goal(s)
		Vector<Activity> originalGoals = new Vector<Activity>(); //e.g., cup1 in the so called wellSetTable Scenario
		for (Activity act : activityToFluent.keySet()) {
			if(initialUnboundedObjName.contains(activityToFluent.get(act).getName()))
				originalGoals.add(act);
		}
		//maintain the the current At unary constraint for retraction case
		currentAssertionalCons = new HashMap<String, UnaryRectangleConstraint>();
		Vector<String> nonMovableObj = new Vector<String>();
		for (int j = 0; j < sAssertionalRels.size(); j++) {
			if(!sAssertionalRels.get(j).getOntologicalProp().isMovable()) {
				nonMovableObj.add(sAssertionalRels.get(j).getFrom());
				nonMovableObj.add(sAssertionalRels.get(j).getTo());
			}

			currentAssertionalCons.put(sAssertionalRels.get(j).getFrom(), new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.At,
					new Bounds(sAssertionalRels.get(j).getUnaryAtRectangleConstraint().getBounds()[0].min, sAssertionalRels.get(j).getUnaryAtRectangleConstraint().getBounds()[0].max), 
					new Bounds(sAssertionalRels.get(j).getUnaryAtRectangleConstraint().getBounds()[1].min, sAssertionalRels.get(j).getUnaryAtRectangleConstraint().getBounds()[1].max),
					new Bounds(sAssertionalRels.get(j).getUnaryAtRectangleConstraint().getBounds()[2].min, sAssertionalRels.get(j).getUnaryAtRectangleConstraint().getBounds()[2].max),
					new Bounds(sAssertionalRels.get(j).getUnaryAtRectangleConstraint().getBounds()[3].min, sAssertionalRels.get(j).getUnaryAtRectangleConstraint().getBounds()[3].max)));

			BoundingBox bb = new BoundingBox(sAssertionalRels.get(j).getUnaryAtRectangleConstraint().getBounds()[0], 
					sAssertionalRels.get(j).getUnaryAtRectangleConstraint().getBounds()[1],
					sAssertionalRels.get(j).getUnaryAtRectangleConstraint().getBounds()[2],
					sAssertionalRels.get(j).getUnaryAtRectangleConstraint().getBounds()[3]);
			oldRectangularRegion.put(sAssertionalRels.get(j).getFrom(), bb);

		}

		Vector<SpatialFluent> newGoalFluentsVector = new Vector<SpatialFluent>();
		ConstraintNetwork actNetwork = new ConstraintNetwork(((SpatialFluentSolver)(this.metaCS.getConstraintSolvers()[0])).getConstraintSolvers()[1]);

		//################################################################################
		//These are hard coded for testing an heuristic!
		//[pen1, notebook1, book1, penHolder1, phone1, keyboard1]
		//		newGoal.removeAllElements();
		//		newGoal.add("book1");
		//		newGoal.add("monitor1");
		//		newGoal.add("keyboard1");
		//		newGoal.add("pen1");
		//################################################################################
		//		//This is a hard code for testing RACE YEAR2 Demo
		//		newGoal.removeAllElements();
		//		newGoal.add("at_knife1_table1");
		//		newGoal.add("at_fork1_table1");
		//################################################################################


		numberOfmisplaced = newGoal.size();

		//set new Goal After old activity
		for (String st :newGoal) {
			//add new fluent if there is not already a fluent which represent the goal, 
			//we make a difference between a fluent which has an activity with fixed release point (i.e., observation) 
			//and with the one already generated subgoal and for some other reason (e.g., resources) retracted
			SpatialFluent newgoalFlunet = null;


			newgoalFlunet = (SpatialFluent)((SpatialFluentSolver)(this.metaCS.getConstraintSolvers()[0]))
					.createVariable(culpritActivities.get(st).getComponent());
			newgoalFlunet.setName(st);
			((Activity)newgoalFlunet.getInternalVariables()[1]).setSymbolicDomain(culpritActivities.get(st).getSymbolicVariable().getSymbols()[0]);
			//			((Activity)newgoalFlunet.getInternalVariables()[1]).setSymbolicDomain(culpritActivities.get(st).getSymbolicVariable().toString()
			//						.subSequence(21, ((Activity)culpritActivities.get(st)).getSymbolicVariable().toString().length() - 1).toString());
			((Activity)newgoalFlunet.getInternalVariables()[1]).setMarking(markings.UNJUSTIFIED);
			((RectangularRegion)newgoalFlunet.getInternalVariables()[0]).setName(st);
			mvalue.addVariable(newgoalFlunet);
			activityToFluent.put(((Activity)newgoalFlunet.getInternalVariables()[1]), newgoalFlunet);


			newGoalFluentsVector.add(newgoalFlunet);
			//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
			//update assertional rule
			for (int j = 0; j < sAssertionalRels.size(); j++) {
				if(sAssertionalRels.get(j).getFrom().compareTo(st) == 0)
					sAssertionalRels.get(j).setUnaryAtRectangleConstraint(new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.At, 
							new Bounds(0, APSPSolver.INF), new Bounds(0, APSPSolver.INF), new Bounds(0, APSPSolver.INF), new Bounds(0, APSPSolver.INF)));
			}			
			//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
			if(!activities.contains(((Activity)newgoalFlunet.getInternalVariables()[1])))
				activities.add(((Activity)newgoalFlunet.getInternalVariables()[1]));
			AllenIntervalConstraint newOnAfteroldOn = new AllenIntervalConstraint(AllenIntervalConstraint.Type.After,
					AllenIntervalConstraint.Type.After.getDefaultBounds());
			newOnAfteroldOn.setFrom(((Activity)newgoalFlunet.getInternalVariables()[1]));
			newOnAfteroldOn.setTo(culpritActivities.get(st));

			//((SpatialFluentSolver)(this.metaCS.getConstraintSolvers()[0])).getConstraintSolvers()[1].addConstraints(new
			//					 Constraint[] {newOnAfteroldOn});
			actNetwork.addConstraint(newOnAfteroldOn);			
			//			System.out.println("newOnAfteroldOn" + newOnAfteroldOn);


			//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
			//it generates manipulationFluent for corresponding new goal fluent
			//the name of fluent comes from parsing the operator with certain relations :
			//it generate for example, atLocation::at_robot1_manipulationArea_fork1_table1()) for at_fork1_table1() 
			SpatialFluent newmanFlunetPick = null;
			
			String extractedNameAndSupport = st.substring(3); //remove at_
			String manFluentsName = "at_robot1_manipulationArea_" + extractedNameAndSupport + "()";
			System.out.println("---NEWNMANFLUNT--" + manFluentsName);
			newmanFlunetPick = (SpatialFluent)((SpatialFluentSolver)(this.metaCS.getConstraintSolvers()[0]))
					.createVariable(culpritActivities.get(st).getComponent()); //e.g., at_location
			newmanFlunetPick.setName(manFluentsName);
			((Activity)newmanFlunetPick.getInternalVariables()[1]).setSymbolicDomain(manFluentsName);
			((Activity)newmanFlunetPick.getInternalVariables()[1]).setMarking(markings.JUSTIFIED);
			((RectangularRegion)newmanFlunetPick.getInternalVariables()[0]).setName(manFluentsName);
			mvalue.addVariable(newmanFlunetPick);
			
			////////////////////////////////////////////////////////////////////////////////////////
			ReachabilityConstraint rc1 = new ReachabilityConstraint(ReachabilityConstraint.Type.basePickingupReachable);
			rc1.setFrom(((ConfigurationVariable)activityToFluent.get(culpritActivities.get(st)).getInternalVariables()[2]));
			rc1.setTo(((ConfigurationVariable)newmanFlunetPick.getInternalVariables()[2]));
			actNetwork.addConstraint(rc1);
			
//			System.out.println("----------------! " + rc1);
			//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
			//the another flunet with the same name, in case, the position of the objects are different, should be two different 
			SpatialFluent newmanFlunetPlace = null;			
			System.out.println("---NEWNMANFLUNT--" + manFluentsName);
			newmanFlunetPlace = (SpatialFluent)((SpatialFluentSolver)(this.metaCS.getConstraintSolvers()[0]))
					.createVariable(culpritActivities.get(st).getComponent()); //e.g., at_location
			newmanFlunetPlace.setName(manFluentsName);
			((Activity)newmanFlunetPlace.getInternalVariables()[1]).setSymbolicDomain(manFluentsName);
			((Activity)newmanFlunetPlace.getInternalVariables()[1]).setMarking(markings.JUSTIFIED);
			((RectangularRegion)newmanFlunetPlace.getInternalVariables()[0]).setName(manFluentsName);
			mvalue.addVariable(newmanFlunetPlace);
			
			////////////////////////////////////////////////////////////////////////////////////////
			ReachabilityConstraint rc2 = new ReachabilityConstraint(ReachabilityConstraint.Type.baseplacingReachable);
			rc2.setFrom(((ConfigurationVariable)newgoalFlunet.getInternalVariables()[2]));
			rc2.setTo(((ConfigurationVariable)newmanFlunetPlace.getInternalVariables()[2]));
			actNetwork.addConstraint(rc2);
//			System.out.println("----------------" + rc2);
			
			////////////////////////////////////////////////
			//Put before constraint to seprate manipulation Area for pick and Place of the same Objects
			///////////////////////////////////////////////
			AllenIntervalConstraint beforeForSeprationOfManFleunts = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before,
					AllenIntervalConstraint.Type.Before.getDefaultBounds());
			beforeForSeprationOfManFleunts.setFrom(((Activity)newmanFlunetPick.getInternalVariables()[1]));
			beforeForSeprationOfManFleunts.setTo(((Activity)newmanFlunetPlace.getInternalVariables()[1]));
			actNetwork.addConstraint(beforeForSeprationOfManFleunts);	

			////////////////////////////////////////////////
			//create manipulationArea for orginal goal
			///////////////////////////////////////////////
//			for (int j = 0; j < originalGoals.size(); j++) {
//				
//				String extractOriginalFlunet = originalGoals.get(j).getSymbolicVariable().getSymbols()[0].substring(3);
//				String originalGoalManFleuntName =  "at_robot1_manipulationArea_" + extractOriginalFlunet;
//				SpatialFluent originalGoalmanFlunetPlace = null;			
//				System.out.println("---NEWNMANFLUNT--" + originalGoalManFleuntName);
//				originalGoalmanFlunetPlace = (SpatialFluent)((SpatialFluentSolver)(this.metaCS.getConstraintSolvers()[0]))
//						.createVariable(originalGoals.get(j).getComponent()); //e.g., at_location
//				originalGoalmanFlunetPlace.setName(originalGoalManFleuntName);
//				((Activity)originalGoalmanFlunetPlace.getInternalVariables()[1]).setSymbolicDomain(originalGoalManFleuntName);
//				((Activity)originalGoalmanFlunetPlace.getInternalVariables()[1]).setMarking(markings.JUSTIFIED);
//				((RectangularRegion)originalGoalmanFlunetPlace.getInternalVariables()[0]).setName(originalGoalManFleuntName);
//				mvalue.addVariable(originalGoalmanFlunetPlace);
//				
//			}

			
			
		}


		Vector<RectangleConstraint> assertionList = new Vector<RectangleConstraint>();
		//		newGoal
		for(int i = 0; i < sAssertionalRels.size(); i++) {
			boolean isAdded = false;
			if(newGoal.contains(sAssertionalRels.get(i).getFrom())){
				for (int j = 0; j < newGoalFluentsVector.size(); j++) { //this is rectangle represents new places!
					if (sAssertionalRels.get(i).getFrom().compareTo(
							((newGoalFluentsVector.get(j))).getName()) == 0) {
						RectangleConstraint assertion = new RectangleConstraint(
								new AllenIntervalConstraint(AllenIntervalConstraint.Type.Equals,
										AllenIntervalConstraint.Type.Equals.getDefaultBounds()),
										new AllenIntervalConstraint(AllenIntervalConstraint.Type.Equals,
												AllenIntervalConstraint.Type.Equals.getDefaultBounds()));

						assertion.setFrom(((RectangularRegion) newGoalFluentsVector.get(j).getRectangularRegion()));
						assertion.setTo(getVariableByName.get(sAssertionalRels.get(i).getTo()));
						assertionList.add(assertion);
						mvalue.addConstraint(assertion);
					}				
				}
				isAdded = true;
			}
			else{
				for (int j = 0; j < metaVaribales.size(); j++) { //this is rectangle represents new places!
					if (sAssertionalRels.get(i).getFrom().compareTo(((metaVaribales.get(j))).getName()) == 0) {

						RectangleConstraint assertion = new RectangleConstraint(
								new AllenIntervalConstraint(AllenIntervalConstraint.Type.Equals,
										AllenIntervalConstraint.Type.Equals.getDefaultBounds()),
										new AllenIntervalConstraint(AllenIntervalConstraint.Type.Equals,
												AllenIntervalConstraint.Type.Equals.getDefaultBounds()));

						assertion.setFrom(((RectangularRegion) metaVaribales.get(j)));
						assertion.setTo(getVariableByName.get(sAssertionalRels.get(i).getTo()));
						if(getVariableByName.get(sAssertionalRels.get(i).getTo()) !=  null){
							assertionList.add(assertion);
							mvalue.addConstraint(assertion);
							isAdded = true;
						}

					}
				}
			}
			if(!isAdded){ //goals and we do not care about the rest of the objects which is in the well set table and can not be observed and does not exist in the set of original goal!
				for (int j = 0; j < originalGoals.size(); j++) {
					if(activityToFluent.get(originalGoals.get(j)).getRectangularRegion().getName().compareTo(sAssertionalRels.get(i).getFrom()) == 0){
						RectangleConstraint assertion = new RectangleConstraint(
								new AllenIntervalConstraint(AllenIntervalConstraint.Type.Equals,
										AllenIntervalConstraint.Type.Equals.getDefaultBounds()),
										new AllenIntervalConstraint(AllenIntervalConstraint.Type.Equals,
												AllenIntervalConstraint.Type.Equals.getDefaultBounds()));

						assertion.setFrom(activityToFluent.get(originalGoals.get(j)).getRectangularRegion());
						assertion.setTo(getVariableByName.get(sAssertionalRels.get(i).getTo()));
						if(getVariableByName.get(sAssertionalRels.get(i).getTo()) !=  null){
							assertionList.add(assertion);
							mvalue.addConstraint(assertion);
						}


					}
				}
			}
		}

		actNetwork.join(mvalue);
		ret.add(actNetwork);
		actNetwork.annotation = this.currentAssertionalCons;

		return ret.toArray(new ConstraintNetwork[ret.size()]);

	}

	protected boolean temporalOverlap(Activity a1, Activity a2) {
		return !(a1.getTemporalVariable().getEET() <= a2.getTemporalVariable()
				.getEST() || a2.getTemporalVariable().getEET() <= a1
				.getTemporalVariable().getEST());
	}

	@Override
	public void markResolvedSub(MetaVariable con, ConstraintNetwork metaValue) {
		// TODO Auto-generated method stub
	}

	public boolean isConflicting(Activity[] peak, HashMap<Activity, SpatialFluent> aTOsf) {

		if(peak.length == 1) return false;

		//		System.out.println("------------------------------------------------------------");
		//		for (int i = 0; i < peak.length; i++) {
		////			System.out.println(peak[i]);
		//			System.out.println(aTOsf.get(peak[i]));
		//		}
		//		System.out.println("------------------------------------------------------------");

		Vector<UnaryRectangleConstraint> atConstraints = new Vector<UnaryRectangleConstraint>();
		HashMap<String, SpatialFluent> currentFluent = new HashMap<String, SpatialFluent>();
		Vector<RectangularRegion> targetRecs = new Vector<RectangularRegion>();

		for (int i = 0; i < peak.length; i++) {
			currentFluent.put(aTOsf.get(peak[i]).getName(),aTOsf.get(peak[i]));
			targetRecs.add(aTOsf.get(peak[i]).getRectangularRegion());
		}

		// ###################################################################################################
		RectangleConstraintSolver iterSolver = new RectangleConstraintSolver(origin, horizon);
		HashMap<String, RectangularRegion> getVariableByName = new HashMap<String, RectangularRegion>();

		Vector<MultiBinaryConstraint> addedGeneralKn = new Vector<MultiBinaryConstraint>();
		for (int i = 0; i < this.rules.length; i++) {
			if (this.rules[i].getFrom().compareTo(this.rules[i].getTo()) == 0) {
				Bounds[] sizeBounds = new Bounds[this.rules[i].getUnaryRAConstraint().getBounds().length];
				for (int j = 0; j < sizeBounds.length; j++) {
					Bounds bSize = new Bounds(
							this.rules[i].getUnaryRAConstraint().getBounds()[j].min,
							this.rules[i].getUnaryRAConstraint().getBounds()[j].max);
					sizeBounds[j] = bSize;
				}
				UnaryRectangleConstraint uConsSize = new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.Size, sizeBounds);

				if (getVariableByName.get(this.rules[i].getFrom()) != null)
					uConsSize.setFrom(getVariableByName.get(this.rules[i].getFrom()));
				else {
					RectangularRegion var = (RectangularRegion) iterSolver.createVariable();
					var.setName(this.rules[i].getFrom());
					uConsSize.setFrom(var);
					getVariableByName.put(this.rules[i].getFrom(), var);
				}
				if (getVariableByName.get(this.rules[i].getTo()) != null)
					uConsSize
					.setTo(getVariableByName.get(this.rules[i].getTo()));
				else {
					RectangularRegion var = (RectangularRegion) iterSolver.createVariable();
					var.setName(this.rules[i].getTo());
					uConsSize.setTo(var);
					getVariableByName.put(this.rules[i].getTo(), var);
				}
				// System.out.println(tmpRule[i].getRAConstraint());
				addedGeneralKn.add(uConsSize);
			} else {

				Bounds[] allenBoundsX = new Bounds[(this.rules[i].getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[0].getBounds().length];
				for (int j = 0; j < allenBoundsX.length; j++) {
					Bounds bx = new Bounds(
							(this.rules[i].getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[0].getBounds()[j].min, (this.rules[i]
									.getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[0].getBounds()[j].max);
					allenBoundsX[j] = bx;
				}

				Bounds[] allenBoundsY = new Bounds[(this.rules[i].getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[1].getBounds().length];
				for (int j = 0; j < allenBoundsY.length; j++) {
					Bounds by = new Bounds(
							(this.rules[i].getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[1]
									.getBounds()[j].min, (this.rules[i].getBinaryRAConstraint())
									.getInternalAllenIntervalConstraints()[1].getBounds()[j].max);
					allenBoundsY[j] = by;
				}

				AllenIntervalConstraint xAllenCon = new AllenIntervalConstraint((this.rules[i].getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[0].getType(), allenBoundsX);
				AllenIntervalConstraint yAllenCon = new AllenIntervalConstraint(
						(this.rules[i].getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[1].getType(), allenBoundsY);


				//This part is for the Allen intervals do not have any bounds e.g., Equals
				if((this.rules[i].getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[0].getBounds().length == 0)
					xAllenCon = (AllenIntervalConstraint)(this.rules[i].getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[0].clone();
				if((this.rules[i].getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[1].getBounds().length == 0)
					yAllenCon = (AllenIntervalConstraint)(this.rules[i].getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[1].clone();


				RectangleConstraint uConsBinary = new RectangleConstraint(xAllenCon, yAllenCon);

				if (getVariableByName.get(this.rules[i].getFrom()) != null)
					uConsBinary.setFrom(getVariableByName.get(this.rules[i].getFrom()));
				else {
					RectangularRegion var = (RectangularRegion) iterSolver.createVariable();
					var.setName(this.rules[i].getFrom());
					uConsBinary.setFrom(var);
					getVariableByName.put(this.rules[i].getFrom(), var);
				}
				if (getVariableByName.get(this.rules[i].getTo()) != null)
					uConsBinary.setTo(getVariableByName.get(this.rules[i].getTo()));
				else {
					RectangularRegion var = (RectangularRegion) iterSolver.createVariable();
					var.setName(this.rules[i].getTo());
					uConsBinary.setTo(var);
					getVariableByName.put(this.rules[i].getTo(), var);
				}
				addedGeneralKn.add(uConsBinary);
			}
		}


		
		if (!iterSolver.addConstraints(addedGeneralKn.toArray(new MultiBinaryConstraint[addedGeneralKn.size()])))
			System.out.println("Failed to general knowledge add");
		// ####################################################################################
		//check whether the fluent which overlapped in time is a fluent of interest meaning they are mentioned in the spatial rules
		//otherwise return false

		//		System.out.println("++++++Before++++++");
		//		Vector<String> conceptsInUse = new Vector<String>();
		//		for (int j = 0; j < iterSolver.getVariables().length; j++) {
		//			for (int j2 = 0; j2 < sAssertionalRels.size(); j2++) {
		//				if(((RectangularRegion)iterSolver.getVariables()[j]).getName().compareTo(sAssertionalRels.get(j2).getTo()) == 0){
		//					conceptsInUse.add(sAssertionalRels.get(j2).getFrom());
		//				}
		//			}
		//			
		//		}
		//		
		//		System.out.println("conceptInUse" + conceptsInUse);
		//		
		//		boolean isThere = false;
		//		for (Activity a : aTOsf.keySet()) {
		//			System.out.println("====" + aTOsf.get(a).getName());
		//			for (int j = 0; j < conceptsInUse.size(); j++) {
		//				System.out.println(conceptsInUse.get(j));
		//				if(aTOsf.get(a).getName().compareTo(conceptsInUse.get(j)) == 0)
		//					isThere = true;	
		//			}
		//			if(!isThere) return false;
		//			isThere = false;
		//		}
		//		
		//		System.out.println("++++++After++++++");
		// ####################################################################################
		// Add at constraint
		Vector<RectangularRegion> metaVaribales = new Vector<RectangularRegion>();
		for (int i = 0; i < sAssertionalRels.size(); i++) {
			SpatialFluent sf = currentFluent.get(sAssertionalRels.get(i).getFrom());
			if (sf == null)
				continue;
			// Add at constraint of indivisuals
			if (sAssertionalRels.get(i).getUnaryAtRectangleConstraint() != null) {
				RectangularRegion var = (RectangularRegion) iterSolver.createVariable();
				var.setName(sAssertionalRels.get(i).getFrom());
				Bounds[] atBounds = new Bounds[sAssertionalRels.get(i).getUnaryAtRectangleConstraint().getBounds().length];
				for (int j = 0; j < atBounds.length; j++) {
					Bounds b = new Bounds(
							sAssertionalRels.get(i).getUnaryAtRectangleConstraint().getBounds()[j].min, sAssertionalRels.get(i)
							.getUnaryAtRectangleConstraint().getBounds()[j].max);
					atBounds[j] = b;
				}

				UnaryRectangleConstraint atCon = new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.At, atBounds);
				atCon.setFrom(var);
				atCon.setTo(var);
				atConstraints.add(atCon);
				metaVaribales.add(var);
				if (!iterSolver.addConstraint(atCon))
					System.out.println("Failed to add AT constraint");

			}

			if (sAssertionalRels.get(i).getOntologicalProp() != null)
				sf.getRectangularRegion().setOntologicalProp(sAssertionalRels.get(i).getOntologicalProp());
			// targetRecs.add(sf);
		}


		//ConstraintNetwork.draw(((SpatialFluentSolver)this.metaCS.getConstraintSolvers()[0]).getConstraintSolvers()[0].getConstraintNetwork(), "RA Constraint Network");

		// ######################################################################################################
		Vector<RectangleConstraint> assertionList = new Vector<RectangleConstraint>();
		for (int i = 0; i < sAssertionalRels.size(); i++) {
			for (int j = 0; j < metaVaribales.size(); j++) {
				if (sAssertionalRels.get(i).getFrom().compareTo(((RectangularRegion) (metaVaribales.get(j))).getName()) == 0) {

					RectangleConstraint assertion = new RectangleConstraint(
							new AllenIntervalConstraint(
									AllenIntervalConstraint.Type.Equals,
									AllenIntervalConstraint.Type.Equals.getDefaultBounds()),
									new AllenIntervalConstraint(
											AllenIntervalConstraint.Type.Equals,
											AllenIntervalConstraint.Type.Equals.getDefaultBounds()));

					assertion.setFrom(((RectangularRegion) metaVaribales.get(j)));
					assertion.setTo(getVariableByName.get(sAssertionalRels.get(i).getTo()));					
					if(getVariableByName.get(sAssertionalRels.get(i).getTo()) != null){
						assertionList.add(assertion);
						//						System.out.println(assertion);
					}
				}
			}
		}

		boolean isConsistent = true;


		// MetaCSPLogging.setLevel(Level.FINE);
		if (!iterSolver.addConstraints(assertionList.toArray(new RectangleConstraint[assertionList.size()])))
			isConsistent = false;

		//		System.out.println(isConsistent);
		//		System.out.println("------------------------------------------------------------");
		return (!isConsistent);
	}

	public void setInitialGoal(String[] initialGoals) {


		for (int i = 0; i < initialGoals.length; i++) {
			initialUnboundedObjName.add(initialGoals[i]);
		}

	}


	@Override
	public void draw(ConstraintNetwork network) {
		// TODO Auto-generated method stub

	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "MetaSpatialAdherenceConstraint ";
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
		return false;
	}

	private Vector<String> getPotentialCulprit() {
		return potentialCulprit;
	}


	public Vector<HashMap<String, Bounds[]>> generateAllAlternativeSet(Vector<RectangularRegion> targetRecs) {

		class ConstraintNetworkSortingCritera {

			public double rigidityNumber = 0;
			public int culpritLevel = 0;

			ConstraintNetworkSortingCritera(double rigidityNumber,
					int culpritLevel) {
				this.culpritLevel = culpritLevel;
				this.rigidityNumber = rigidityNumber;
			}
		}

		//		System.out.println("targetRecs: " + targetRecs);
		//		System.out.println("permutation" + permutation);

		final HashMap<Integer, ConstraintNetworkSortingCritera> sortingCN = new HashMap<Integer, ConstraintNetworkSortingCritera>();
		HashMap<Integer, HashMap<String, Bounds[]>> cnToInitPose = new HashMap<Integer, HashMap<String, Bounds[]>>();
		HashMap<Integer, Boolean> levelTracker = new HashMap<Integer, Boolean>(); //created for generating the culprit incrementally

		for (Integer level : permutation.values()) 
			levelTracker.put(level, false);

		int counter = 0;
		for (HashMap<String, Bounds[]> iterCN : permutation.keySet()) {

			//incremental generation break
			if(permutation.get(iterCN) > 0 && (levelTracker.get(permutation.get(iterCN) - 1) == true)){
				//				System.out.println(levelTracker.get(permutation.get(iterCN)));
				//				System.out.println(permutation.get(iterCN));
				break;
			}

			//			System.out.println("------------------------------------------");
			//			for (String st : iterCN.keySet()) {
			//				System.out.print(st + "  ");
			//				for (int i = 0; i < iterCN.get(st).length; i++) {
			//					System.out.print(iterCN.get(st)[i] + "  ");
			//				}
			//				System.out.println();
			//			}
			//			System.out.println("------------------------------------------");
			RectangleConstraintSolver iterSolver = new RectangleConstraintSolver(origin, horizon);
			HashMap<String, RectangularRegion> getVariableByName = new HashMap<String, RectangularRegion>();

			Vector<MultiBinaryConstraint> addedGeneralKn = new Vector<MultiBinaryConstraint>();
			for (int i = 0; i < this.rules.length; i++) {

				if (this.rules[i].getFrom().compareTo(this.rules[i].getTo()) == 0) {
					Bounds[] sizeBounds = new Bounds[this.rules[i].getUnaryRAConstraint().getBounds().length];
					for (int j = 0; j < sizeBounds.length; j++) {
						Bounds bSize = new Bounds(this.rules[i].getUnaryRAConstraint().getBounds()[j].min,
								this.rules[i].getUnaryRAConstraint().getBounds()[j].max);
						sizeBounds[j] = bSize;
					}
					UnaryRectangleConstraint uConsSize = new UnaryRectangleConstraint(
							UnaryRectangleConstraint.Type.Size, sizeBounds);

					if (getVariableByName.get(this.rules[i].getFrom()) != null)
						uConsSize.setFrom(getVariableByName.get(this.rules[i]
								.getFrom()));
					else {
						RectangularRegion var = (RectangularRegion) iterSolver.createVariable();
						var.setName(this.rules[i].getFrom());
						uConsSize.setFrom(var);
						getVariableByName.put(this.rules[i].getFrom(), var);
					}
					if (getVariableByName.get(this.rules[i].getTo()) != null)
						uConsSize.setTo(getVariableByName.get(this.rules[i].getTo()));
					else {
						RectangularRegion var = (RectangularRegion) iterSolver.createVariable();
						var.setName(this.rules[i].getTo());
						uConsSize.setTo(var);
						getVariableByName.put(this.rules[i].getTo(), var);
					}
					// System.out.println(tmpRule[i].getRAConstraint());
					addedGeneralKn.add(uConsSize);
				} else {

					Bounds[] allenBoundsX = new Bounds[(this.rules[i].getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[0].getBounds().length];
					for (int j = 0; j < allenBoundsX.length; j++) {
						Bounds bx = new Bounds(
								(this.rules[i].getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[0].getBounds()[j].min,
								(this.rules[i].getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[0].getBounds()[j].max);
						allenBoundsX[j] = bx;
					}

					Bounds[] allenBoundsY = new Bounds[(this.rules[i].getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[1].getBounds().length];
					for (int j = 0; j < allenBoundsY.length; j++) {
						Bounds by = new Bounds((this.rules[i].getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[1].getBounds()[j].min,
								(this.rules[i].getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[1].getBounds()[j].max);
						allenBoundsY[j] = by;
					}

					AllenIntervalConstraint xAllenCon = new AllenIntervalConstraint((this.rules[i].getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[0].getType(), allenBoundsX);
					AllenIntervalConstraint yAllenCon = new AllenIntervalConstraint((this.rules[i].getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[1].getType(), allenBoundsY);

					//					//This part is for the Allen intervals do not have any bounds e.g., Equals
					//					if((this.rules[i].getBinaryBAConstraint()).getInternalAllenIntervalConstraints()[0].getBounds().length == 0)
					//						xAllenCon = (AllenIntervalConstraint)(this.rules[i].getBinaryBAConstraint()).getInternalAllenIntervalConstraints()[0].clone();
					//					if((this.rules[i].getBinaryBAConstraint()).getInternalAllenIntervalConstraints()[1].getBounds().length == 0)
					//						yAllenCon = (AllenIntervalConstraint)(this.rules[i].getBinaryBAConstraint()).getInternalAllenIntervalConstraints()[1].clone();

					RectangleConstraint uConsBinary = new RectangleConstraint(xAllenCon, yAllenCon);

					if (getVariableByName.get(this.rules[i].getFrom()) != null)
						uConsBinary.setFrom(getVariableByName.get(this.rules[i].getFrom()));
					else {
						RectangularRegion var = (RectangularRegion) iterSolver.createVariable();
						var.setName(this.rules[i].getFrom());
						uConsBinary.setFrom(var);
						getVariableByName.put(this.rules[i].getFrom(), var);
					}
					if (getVariableByName.get(this.rules[i].getTo()) != null)
						uConsBinary.setTo(getVariableByName.get(this.rules[i].getTo()));
					else {
						RectangularRegion var = (RectangularRegion) iterSolver.createVariable();
						var.setName(this.rules[i].getTo());
						uConsBinary.setTo(var);
						getVariableByName.put(this.rules[i].getTo(), var);
					}
					addedGeneralKn.add(uConsBinary);
				}
			}

			if (!iterSolver.addConstraints(addedGeneralKn.toArray(new MultiBinaryConstraint[addedGeneralKn.size()])))
				System.out.println("Failed to add general knowledge");

			// Att At cpnstraint
			Vector<RectangularRegion> metaVaribales = new Vector<RectangularRegion>();

			for (RectangularRegion Metavar : targetRecs) {

				//skip those fluents which is not mention in general spatial knowledge
				if(iterCN.get(Metavar.getName()) == null) continue;

				RectangularRegion var = (RectangularRegion) iterSolver.createVariable();
				var.setName(Metavar.getName());


				//				System.out.println("Metavar.getName(): " + Metavar.getName());
				//				System.out.println("iterCN: " + iterCN);

				Bounds[] atBounds = new Bounds[iterCN.get(Metavar.getName()).length];
				for (int j = 0; j < atBounds.length; j++) {
					Bounds at = new Bounds(
							iterCN.get(Metavar.getName())[j].min,
							iterCN.get(Metavar.getName())[j].max);
					atBounds[j] = at;
				}

				UnaryRectangleConstraint atCon = new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.At, atBounds);
				atCon.setFrom(var);
				atCon.setTo(var);
				metaVaribales.add(var);
				if (!iterSolver.addConstraint(atCon))
					System.out.println("Failed to add AT constraint");
			}

			Vector<RectangleConstraint> assertionList = new Vector<RectangleConstraint>();
			for (int i = 0; i < sAssertionalRels.size(); i++) {
				for (int j = 0; j < metaVaribales.size(); j++) {
					if (sAssertionalRels.get(i).getFrom().compareTo(((RectangularRegion) (metaVaribales.get(j))).getName()) == 0) {
						RectangleConstraint assertion = new RectangleConstraint(
								new AllenIntervalConstraint(
										AllenIntervalConstraint.Type.Equals,
										AllenIntervalConstraint.Type.Equals
										.getDefaultBounds()),
										new AllenIntervalConstraint(
												AllenIntervalConstraint.Type.Equals,
												AllenIntervalConstraint.Type.Equals
												.getDefaultBounds()));

						assertion.setFrom(((RectangularRegion) metaVaribales.get(j)));
						assertion.setTo(getVariableByName.get(sAssertionalRels.get(i).getTo()));
						// System.out.println(assertion);
						if(getVariableByName.get(sAssertionalRels.get(i).getTo()) != null) //maybe there is nothing assigned to them in spatial general Knowledge
							assertionList.add(assertion);
					}
				}
			}

			//			System.out.println("assertionList: " + assertionList);

			boolean isConsistent = true;

			// MetaCSPLogging.setLevel(Level.FINE);
			if (!iterSolver.addConstraints(assertionList.toArray(new RectangleConstraint[assertionList.size()]))) {
				isConsistent = false;
				logger.fine("Failed to add Assertinal Constraint in first generation of all culprit..alternatives generate later...");
			}

			double rigidityavg = ((double) (((AllenIntervalNetworkSolver) iterSolver
					.getConstraintSolvers()[0]).getRigidityNumber()) + (double) (((AllenIntervalNetworkSolver) iterSolver
							.getConstraintSolvers()[1]).getRigidityNumber())) / 2;

			//			System.out.println("rigidityAvg:" + rigidityavg);


			if (isConsistent) {
				//				System.out.println("THIS IS CONSISTENT");
				//				sortingCN.put(iterSolver.getConstraintNetwork(), new ConstraintNetworkSortingCritera(rigidityavg,permutation.get(iterCN)));
				//				cnToInitPose.put(iterSolver.getConstraintNetwork(), iterCN);
				//				System.out.println("level: " + permutation.get(iterCN));
				sortingCN.put(counter, new ConstraintNetworkSortingCritera(rigidityavg,permutation.get(iterCN)));
				cnToInitPose.put(counter, iterCN);
				levelTracker.put(permutation.get(iterCN), true);
			}
			counter++;



			//			System.out.println(iterSolver.getConstraintNetwork());
			//			 System.out.println(iterSolver.extractBoundingBoxesFromSTPs("cup1").getAlmostCentreRectangle());
			//			
			//			 System.out.println("_______________________________________________________________________________");

		}

		ArrayList as = new ArrayList(sortingCN.keySet());
		Collections.sort(as, new Comparator() {
			public int compare(Object o1, Object o2) {
				Integer l1 = (Integer) o1;
				Integer l2 = (Integer) o2;
				Integer first = (Integer) sortingCN.get(l1).culpritLevel;
				Integer second = (Integer) sortingCN.get(l2).culpritLevel;
				int i = first.compareTo(second);
				if (i != 0)
					return i;

				Integer r1 = (Integer) o1;
				Integer r2 = (Integer) o2;
				Double firstRig = (Double) sortingCN.get(r1).rigidityNumber;
				Double secondRig = (Double) sortingCN.get(r2).rigidityNumber;

				i = firstRig.compareTo(secondRig);
				if (i != 0)
					return i;
				return -1;
			}
		});
		Vector<HashMap<String, Bounds[]>> alternativeSets = new Vector<HashMap<String, Bounds[]>>();
		Iterator i = as.iterator();
		while (i.hasNext()) {
			int ct = (Integer) i.next();
			//			HashMap<String, BoundingBox> strToBBs = new HashMap<String, BoundingBox>();
			//			for (int j = 0; j < ct.getVariables().length; j++) {
			//				BoundingBox bb = new BoundingBox(new Bounds(((AllenInterval)((RectangularRegion)ct.getVariables()[j]).getInternalVariables()[0]).getEST(), ((AllenInterval)((RectangularRegion)ct.getVariables()[j]).getInternalVariables()[0]).getLST()), 
			//						new Bounds(((AllenInterval)((RectangularRegion)ct.getVariables()[j]).getInternalVariables()[0]).getEET(), ((AllenInterval)((RectangularRegion)ct.getVariables()[j]).getInternalVariables()[0]).getLET()), 
			//						new Bounds(((AllenInterval)((RectangularRegion)ct.getVariables()[j]).getInternalVariables()[1]).getEST(), ((AllenInterval)((RectangularRegion)ct.getVariables()[j]).getInternalVariables()[1]).getLST()), 
			//						new Bounds(((AllenInterval)((RectangularRegion)ct.getVariables()[j]).getInternalVariables()[1]).getEET(), ((AllenInterval)((RectangularRegion)ct.getVariables()[j]).getInternalVariables()[1]).getLET()));
			//				
			//				strToBBs.put(((RectangularRegion)ct.getVariables()[j]).getName(), bb);
			//			}
			//			newRectangularRegion.add(strToBBs);
			alternativeSets.add(cnToInitPose.get(ct));

		}
		return alternativeSets;

	}

	private ConstraintNetwork[] completePeakCollection(HashMap<Activity, SpatialFluent> aTOsf) {
		Vector<Activity> activities = new Vector<Activity>();
		for (Activity act : aTOsf.keySet()) {
			activities.add(act);
		}

		if (activities != null && !activities.isEmpty()) {
			logger.finest("Doing complete peak collection with " + activities.size() + " activities...");

			Activity[] groundVars = activities.toArray(new Activity[activities.size()]);			
			Vector<Long> discontinuities = new Vector<Long>();
			for (Activity a : groundVars) {
				long start = a.getTemporalVariable().getEST();
				long end = a.getTemporalVariable().getEET();
				if (!discontinuities.contains(start)) discontinuities.add(start);
				if (!discontinuities.contains(end)) discontinuities.add(end);
			}

			Long[] discontinuitiesArray = discontinuities.toArray(new Long[discontinuities.size()]);
			Arrays.sort(discontinuitiesArray);

			HashSet<HashSet<Activity>> superPeaks = new HashSet<HashSet<Activity>>();

			for (int i = 0; i < discontinuitiesArray.length-1; i++) {
				HashSet<Activity> onePeak = new HashSet<Activity>();
				superPeaks.add(onePeak);
				Bounds interval = new Bounds(discontinuitiesArray[i], discontinuitiesArray[i+1]);
				for (Activity a : groundVars) {
					Bounds interval1 = new Bounds(a.getTemporalVariable().getEST(), a.getTemporalVariable().getEET());
					Bounds intersection = interval.intersectStrict(interval1);
					if (intersection != null && !intersection.isSingleton()) {
						onePeak.add(a);
					}
				}
			}



			Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
			for (HashSet<Activity> superSet : superPeaks) {
				for (Set<Activity> s : powerSet(superSet)) {
					if (!s.isEmpty()) {
						ConstraintNetwork cn = new ConstraintNetwork(null);
						for (Activity a : s) cn.addVariable(a); 
						if (!ret.contains(cn) && isConflicting(s.toArray(new Activity[s.size()]), aTOsf)) ret.add(cn);
					}
				}
			}

			//			System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			//			for (int i = 0; i < ret.size(); i++) {
			//				System.out.println(ret.get(i));
			//				System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			//			}
			//			System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

			logger.finest("Done peak sampling");
			return ret.toArray(new ConstraintNetwork[ret.size()]);			
		} 

		return (new ConstraintNetwork[0]);
	}


	private void generateCombinantion(Vector<UnaryRectangleConstraint> atConstraints) {

		Vector<UnaryRectangleConstraint> boundedUnaryCons = new Vector<UnaryRectangleConstraint>();
		Vector<UnaryRectangleConstraint> unboundedUnaryCons = new Vector<UnaryRectangleConstraint>();

		HashMap<Vector<UnaryRectangleConstraint>, Integer> rank = new HashMap<Vector<UnaryRectangleConstraint>, Integer>();
		for (int i = 0; i < atConstraints.size(); i++) {
			Bounds[] boundsX = ((AllenIntervalConstraint) ((UnaryRectangleConstraint) atConstraints
					.get(i)).getInternalConstraints()[0]).getBounds();
			Bounds[] boundsY = ((AllenIntervalConstraint) ((UnaryRectangleConstraint) atConstraints
					.get(i)).getInternalConstraints()[1]).getBounds();
			if (!isUnboundedBoundingBox(boundsX[0], boundsX[1], boundsY[0],boundsY[1])
					&& ((RectangularRegion) ((UnaryRectangleConstraint) atConstraints.get(i))
							.getFrom()).getOntologicalProp().isMovable()) {

				potentialCulprit.add(((RectangularRegion) ((UnaryRectangleConstraint) atConstraints.get(i)).getFrom()).getName());
				logger.fine("one potential culprit can be: " + ((RectangularRegion) ((UnaryRectangleConstraint) atConstraints.get(i)).getFrom()).getName());
				boundedUnaryCons.add((UnaryRectangleConstraint) atConstraints.get(i));
			}
			else {				
				//				if (((RectangularRegion) ((UnaryRectangleConstraint) atConstraints.get(i)).getFrom()).getOntologicalProp().isMovable())
				//					initialUnboundedObjName.add(((RectangularRegion) ((UnaryRectangleConstraint) atConstraints.get(i)).getFrom()).getName());
				unboundedUnaryCons.add((UnaryRectangleConstraint) atConstraints.get(i));
			}
		}


		PermutationsWithRepetition gen = new PermutationsWithRepetition(2, boundedUnaryCons.size());
		int[][] v = gen.getVariations();
		for (int i = 0; i < v.length; i++) {
			Vector<UnaryRectangleConstraint> tmpboundedUnaryCons = new Vector<UnaryRectangleConstraint>();
			int culpritNumber = 0;
			for (int j = 0; j < v[i].length; j++) {
				if(v[i][j] == 1){
					UnaryRectangleConstraint utmp = new UnaryRectangleConstraint(
							UnaryRectangleConstraint.Type.At, new Bounds(0,
									APSPSolver.INF), new Bounds(0,
											APSPSolver.INF), new Bounds(0,
													APSPSolver.INF), new Bounds(0,
															APSPSolver.INF));
					utmp.setFrom(boundedUnaryCons.get(j).getFrom());
					utmp.setTo(boundedUnaryCons.get(j).getTo());
					tmpboundedUnaryCons.add(utmp);
					culpritNumber++;					
				}
				else{
					tmpboundedUnaryCons.add(boundedUnaryCons.get(j));
				}
			}
			rank.put(tmpboundedUnaryCons, culpritNumber);
		}




		for (Vector<UnaryRectangleConstraint> cc : rank.keySet()) {
			HashMap<String, Bounds[]> culprit = new HashMap<String, Bounds[]>();

			for (int i = 0; i < cc.size(); i++) {

				Bounds[] bounds = new Bounds[4];
				bounds[0] = ((AllenIntervalConstraint) ((UnaryRectangleConstraint) cc
						.get(i)).getInternalConstraints()[0]).getBounds()[0];
				bounds[1] = ((AllenIntervalConstraint) ((UnaryRectangleConstraint) cc
						.get(i)).getInternalConstraints()[0]).getBounds()[1];
				bounds[2] = ((AllenIntervalConstraint) ((UnaryRectangleConstraint) cc
						.get(i)).getInternalConstraints()[1]).getBounds()[0];
				bounds[3] = ((AllenIntervalConstraint) ((UnaryRectangleConstraint) cc
						.get(i)).getInternalConstraints()[1]).getBounds()[1];

				culprit.put(
						((RectangularRegion) cc.get(i).getFrom()).getName(),
						bounds);
			}
			for (int i = 0; i < unboundedUnaryCons.size(); i++) {
				Bounds[] bounds = new Bounds[4];
				bounds[0] = ((AllenIntervalConstraint) ((UnaryRectangleConstraint) unboundedUnaryCons
						.get(i)).getInternalConstraints()[0]).getBounds()[0];
				bounds[1] = ((AllenIntervalConstraint) ((UnaryRectangleConstraint) unboundedUnaryCons
						.get(i)).getInternalConstraints()[0]).getBounds()[1];
				bounds[2] = ((AllenIntervalConstraint) ((UnaryRectangleConstraint) unboundedUnaryCons
						.get(i)).getInternalConstraints()[1]).getBounds()[0];
				bounds[3] = ((AllenIntervalConstraint) ((UnaryRectangleConstraint) unboundedUnaryCons
						.get(i)).getInternalConstraints()[1]).getBounds()[1];

				culprit.put(((RectangularRegion) unboundedUnaryCons.get(i)
						.getFrom()).getName(), bounds);
			}
			permutation.put(culprit, rank.get(cc));
		}

		//sort permutation
		HashMap<HashMap<String, Bounds[]>, Integer> tempPermutation = sortHashMapByValues(permutation);
		permutation.clear();
		permutation = tempPermutation;
	}

	private boolean isUnboundedBoundingBox(Bounds xLB, Bounds xUB, Bounds yLB,
			Bounds yUB) {

		if (xLB.min != 0 && xLB.max != APSPSolver.INF)
			return false;
		if (xUB.min != 0 && xUB.max != APSPSolver.INF)
			return false;
		if (yLB.min != 0 && yLB.max != APSPSolver.INF)
			return false;
		if (yLB.min != 0 && yUB.max != APSPSolver.INF)
			return false;
		return true;
	}

	private ConstraintNetwork createTBOXspatialNetwork(ConstraintSolver solver,
			HashMap<String, RectangularRegion> getVariableByName) {

		// general knowledge
		ConstraintNetwork ret = new ConstraintNetwork(
				solver);
		// Vector<MultiBinaryConstraint> addedGeneralKn = new
		// Vector<MultiBinaryConstraint>();
		for (int i = 0; i < this.rules.length; i++) {

			if (this.rules[i].getFrom().compareTo(this.rules[i].getTo()) == 0) {
				Bounds[] sizeBounds = new Bounds[this.rules[i]
						.getUnaryRAConstraint().getBounds().length];
				for (int j = 0; j < sizeBounds.length; j++) {
					Bounds bSize = new Bounds(
							this.rules[i].getUnaryRAConstraint().getBounds()[j].min,
							this.rules[i].getUnaryRAConstraint().getBounds()[j].max);
					sizeBounds[j] = bSize;
				}
				UnaryRectangleConstraint uConsSize = new UnaryRectangleConstraint(
						UnaryRectangleConstraint.Type.Size, sizeBounds);

				if (getVariableByName.get(this.rules[i].getFrom()) != null)
					uConsSize.setFrom(getVariableByName.get(this.rules[i]
							.getFrom()));
				else {
					RectangularRegion var = (RectangularRegion) solver
							.createVariable();
					var.setName(this.rules[i].getFrom());
					uConsSize.setFrom(var);
					getVariableByName.put(this.rules[i].getFrom(), var);
				}
				if (getVariableByName.get(this.rules[i].getTo()) != null)
					uConsSize
					.setTo(getVariableByName.get(this.rules[i].getTo()));
				else {
					RectangularRegion var = (RectangularRegion) solver
							.createVariable();
					var.setName(this.rules[i].getTo());
					uConsSize.setTo(var);
					getVariableByName.put(this.rules[i].getTo(), var);
				}
				// System.out.println(tmpRule[i].getRAConstraint());
				ret.addConstraint(uConsSize);
			} else {

				Bounds[] allenBoundsX = new Bounds[(this.rules[i]
						.getBinaryRAConstraint())
						.getInternalAllenIntervalConstraints()[0].getBounds().length];
				for (int j = 0; j < allenBoundsX.length; j++) {
					Bounds bx = new Bounds(
							(this.rules[i].getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[0]
									.getBounds()[j].min, (this.rules[i]
											.getBinaryRAConstraint())
											.getInternalAllenIntervalConstraints()[0]
													.getBounds()[j].max);
					allenBoundsX[j] = bx;
				}

				Bounds[] allenBoundsY = new Bounds[(this.rules[i]
						.getBinaryRAConstraint())
						.getInternalAllenIntervalConstraints()[1].getBounds().length];
				for (int j = 0; j < allenBoundsY.length; j++) {
					Bounds by = new Bounds(
							(this.rules[i].getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[1]
									.getBounds()[j].min, (this.rules[i]
											.getBinaryRAConstraint())
											.getInternalAllenIntervalConstraints()[1]
													.getBounds()[j].max);
					allenBoundsY[j] = by;
				}

				AllenIntervalConstraint xAllenCon = new AllenIntervalConstraint(
						(this.rules[i].getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[0]
								.getType(), allenBoundsX);
				AllenIntervalConstraint yAllenCon = new AllenIntervalConstraint(
						(this.rules[i].getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[1]
								.getType(), allenBoundsY);

				RectangleConstraint uConsBinary = new RectangleConstraint(
						xAllenCon, yAllenCon);

				if (getVariableByName.get(this.rules[i].getFrom()) != null)
					uConsBinary.setFrom(getVariableByName.get(this.rules[i]
							.getFrom()));
				else {
					RectangularRegion var = (RectangularRegion) solver
							.createVariable();
					var.setName(this.rules[i].getFrom());
					uConsBinary.setFrom(var);
					getVariableByName.put(this.rules[i].getFrom(), var);
				}
				if (getVariableByName.get(this.rules[i].getTo()) != null)
					uConsBinary.setTo(getVariableByName.get(this.rules[i]
							.getTo()));
				else {
					RectangularRegion var = (RectangularRegion) solver
							.createVariable();
					var.setName(this.rules[i].getTo());
					uConsBinary.setTo(var);
					getVariableByName.put(this.rules[i].getTo(), var);
				}
				ret.addConstraint(uConsBinary);
			}
		}

		return ret;
	}

	private void setPermutationHashMAP(Vector<SpatialFluent> conflictvars, Vector<RectangularRegion> targetRecs){

		Vector<UnaryRectangleConstraint> atConstraints = new Vector<UnaryRectangleConstraint>();
		HashMap<String, SpatialFluent> currentFluent = new HashMap<String, SpatialFluent>();
		//		Vector<RectangularRegion> targetRecs = new Vector<RectangularRegion>();

		for (int i = 0; i < conflictvars.size(); i++) {
			currentFluent.put(conflictvars.get(i).getName(), conflictvars.get(i));
		}


		// Add at constraint
		RectangleConstraintSolver iterSolver = new RectangleConstraintSolver(origin, horizon);
		Vector<RectangularRegion> metaVaribales = new Vector<RectangularRegion>();
		for (int i = 0; i < sAssertionalRels.size(); i++) {

			SpatialFluent sf = currentFluent.get(sAssertionalRels.get(i).getFrom());
			if (sf == null)
				continue;
			// Add at constraint of indivisuals
			RectangularRegion var = (RectangularRegion) iterSolver.createVariable();
			if (sAssertionalRels.get(i).getUnaryAtRectangleConstraint() != null) {				
				var.setName(sAssertionalRels.get(i).getFrom());				
				Bounds[] atBounds = new Bounds[sAssertionalRels.get(i).getUnaryAtRectangleConstraint().getBounds().length];
				for (int j = 0; j < atBounds.length; j++) {
					Bounds b = new Bounds(
							sAssertionalRels.get(i).getUnaryAtRectangleConstraint()
							.getBounds()[j].min, sAssertionalRels.get(i)
							.getUnaryAtRectangleConstraint()
							.getBounds()[j].max);
					atBounds[j] = b;
				}

				UnaryRectangleConstraint atCon = new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.At, atBounds);
				atCon.setFrom(var);
				atCon.setTo(var);
				atConstraints.add(atCon);
				metaVaribales.add(var);
				if (!iterSolver.addConstraint(atCon))
					System.out.println("Failed to add AT constraint");

			}

			if (sAssertionalRels.get(i).getOntologicalProp() != null){
				//				sf.getRectangularRegion().setOntologicalProp(sAssertionalRels[i].getOntologicalProp());
				var.setOntologicalProp(sAssertionalRels.get(i).getOntologicalProp());
				// targetRecs.add(sf);
			}
		}

		generateCombinantion(atConstraints);


	}


	private static LinkedHashMap sortHashMapByValues(HashMap passedMap) {
		ArrayList mapKeys = new ArrayList(passedMap.keySet());
		ArrayList mapValues = new ArrayList(passedMap.values());
		Collections.sort(mapValues);
		//Collections.sort(mapKeys);

		LinkedHashMap sortedMap = 
				new LinkedHashMap();

		Iterator valueIt = ((java.util.List<SpatialRule>) mapValues).iterator();
		while (valueIt.hasNext()) {
			int val = (Integer) valueIt.next();
			Iterator keyIt = ((java.util.List<SpatialRule>) mapKeys).iterator();

			while (keyIt.hasNext()) {
				HashMap<String, Bounds[]> key = (HashMap<String, Bounds[]>) keyIt.next();
				int comp1 = (Integer) passedMap.get(key);
				int comp2 = val;

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



	@Override
	public ConstraintSolver getGroundSolver() {
		// TODO Auto-generated method stub
		return null;
	}


}

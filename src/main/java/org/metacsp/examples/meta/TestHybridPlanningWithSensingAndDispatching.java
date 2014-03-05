package org.metacsp.examples.meta;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.logging.Level;

import org.metacsp.dispatching.DispatchingFunction;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.VariableOrderingH;
import org.metacsp.meta.hybridPlanner.FluentBasedSimpleDomain;
import org.metacsp.meta.hybridPlanner.MetaOccupiedConstraint;
import org.metacsp.meta.hybridPlanner.MetaSpatialAdherenceConstraint;
import org.metacsp.meta.hybridPlanner.SensingSchedulable;
import org.metacsp.meta.hybridPlanner.SimpleHybridPlanner;
import org.metacsp.meta.simplePlanner.ProactivePlanningDomain;
import org.metacsp.meta.simplePlanner.SimplePlanner;
import org.metacsp.meta.simplePlanner.SimpleDomain.markings;
import org.metacsp.meta.symbolsAndTime.Schedulable;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangleConstraint;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangularRegion;
import org.metacsp.multi.spatial.rectangleAlgebra.UnaryRectangleConstraint;
import org.metacsp.multi.spatial.rectangleAlgebraNew.toRemove.OntologicalSpatialProperty;
import org.metacsp.multi.spatioTemporal.SpatialFluent;
import org.metacsp.multi.spatioTemporal.SpatialFluentSolver;
import org.metacsp.sensing.ConstraintNetworkAnimator;
import org.metacsp.sensing.Controllable;
import org.metacsp.spatial.utility.SpatialAssertionalRelation;
import org.metacsp.spatial.utility.SpatialRule;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;
import org.metacsp.utility.timelinePlotting.TimelinePublisher;
import org.metacsp.utility.timelinePlotting.TimelineVisualizer;



public class TestHybridPlanningWithSensingAndDispatching {

	static int pad = 0;    
	static long duration = 1000; //both for activity and location
	static long tick = 1000;
	static HashMap<String, SpatialAssertionalRelation> currentObservation = new HashMap<String, SpatialAssertionalRelation>();

	static MetaOccupiedConstraint metaOccupiedConstraint = null;
	static MetaSpatialAdherenceConstraint metaSpatialAdherence  = null;
	static SensingSchedulable sensingSchedulable = null;
	
	static int counter = 0;
	public static void main(String[] args) {
		
		
		//################################################################################################################
		Controllable contrallableAtLocation = new Controllable();
		
		//Two culprit
		contrallableAtLocation.registerSymbolsFromControllableSensor("atLocation::at_cup1_counter1()--(1,2,3,4)++true");		
		contrallableAtLocation.registerSymbolsFromControllableSensor("atLocation::at_table1_table1()--(0,60,0,99)++false");
		contrallableAtLocation.registerSymbolsFromControllableSensor("atLocation::at_fork1_table1()--(20,26,13,32)++true");
		contrallableAtLocation.registerSymbolsFromControllableSensor("atLocation::at_knife1_table1()--(30,36,10,33)++true");

		//One culprit
//		contrallableAtLocation.registerSymbolsFromControllableSensor("atLocation::at_cup1_counter1()--(1,2,3,4)++true");
//		//contrallableAtLocation.registerSymbolsFromControllableSensor("atLocation::at_cup1_tray1()--(1,2,3,4)++true");
//		contrallableAtLocation.registerSymbolsFromControllableSensor("atLocation::at_table1_table1()--(0,100,0,99)++false");
//		contrallableAtLocation.registerSymbolsFromControllableSensor("atLocation::at_fork1_table1()--(31,37,13,32)++true");
//		contrallableAtLocation.registerSymbolsFromControllableSensor("atLocation::at_knife1_table1()--(40,46,10,33)++true");

		final Vector<String> ctrls = contrallableAtLocation.getContrallbaleSymbols();

		//#######################################################################

		
		//Create planner
		final SimpleHybridPlanner simpleHybridPlanner = new SimpleHybridPlanner(0,100000,0);
		//MetaCSPLogging.setLevel(planner.getClass(), Level.FINEST);

		FluentBasedSimpleDomain.parseDomain(simpleHybridPlanner, "domains/testSensingBeforePickAndPlaceDomain.ddl", FluentBasedSimpleDomain.class);
//		FluentBasedSimpleDomain.parseDomain(simpleHybridPlanner, "domains/withManipulationSpace.ddl", FluentBasedSimpleDomain.class);
		
		
		ConstraintNetworkAnimator animator = new ConstraintNetworkAnimator(simpleHybridPlanner, tick);

		//Most critical conflict is the one with most activities 
		VariableOrderingH varOH = new VariableOrderingH() {
			@Override
			public int compare(ConstraintNetwork arg0, ConstraintNetwork arg1) {
				return arg1.getVariables().length - arg0.getVariables().length;
			}
			@Override
			public void collectData(ConstraintNetwork[] allMetaVariables) { }
		};
		// no value ordering
		ValueOrderingH valOH = new ValueOrderingH() {
			@Override
			public int compare(ConstraintNetwork o1, ConstraintNetwork o2) { return 0; }
		};
		metaSpatialAdherence = new MetaSpatialAdherenceConstraint(varOH, valOH);
		final SpatialFluentSolver groundSolver = (SpatialFluentSolver)simpleHybridPlanner.getConstraintSolvers()[0];

		MetaCSPLogging.setLevel(SimpleHybridPlanner.class, Level.FINEST);
		MetaCSPLogging.setLevel(MetaSpatialAdherenceConstraint.class, Level.FINEST);
		//#################################################################################################################
		//add metaOccupiedConstraint
		metaOccupiedConstraint = new MetaOccupiedConstraint(null, null);
		metaOccupiedConstraint.setPad(pad);
		//#################################################################################################################
		//add sensor schedulable
		sensingSchedulable = new SensingSchedulable(null, null);
		
		//#################################################################################################################
		//add spatial general rule to MetaSpatialFluentConstraint
		Vector<SpatialRule> srules = new Vector<SpatialRule>();
		getSpatialKnowledge(srules);
		metaSpatialAdherence.setSpatialRules(srules.toArray(new SpatialRule[srules.size()]));
		
		//#################################################################################################################
		//set the goals
		metaSpatialAdherence.setInitialGoal(new String[]{"at_cup1_table1"});
		Activity act = getCreatedActivty(groundSolver, "atLocation::at_cup1_table1()--(0,0,0,0)++true");
		act.setMarking(markings.UNJUSTIFIED);

		//################################################################################################
		//Set initial situation
		Activity two = (Activity)groundSolver.getConstraintSolvers()[1].createVariable("atLocation");
		two.setSymbolicDomain("at_robot1_counter1()");
		two.setMarking(markings.JUSTIFIED);

		long releaseTime = 1000;
		releaseActivity(groundSolver, releaseTime, two);
		addDurationToActivity(groundSolver, duration, two);

		
		releaseActivity(groundSolver, releaseTime, getCreatedActivty(groundSolver, ctrls.get(0)));
		//insert assertions 
		Vector<SpatialAssertionalRelation> saRelations = new Vector<SpatialAssertionalRelation>(); 
		for (String st : currentObservation.keySet()) saRelations.add(currentObservation.get(st));
		metaSpatialAdherence.setSpatialAssertionalRelations(saRelations);
		
		//##############################################################################################################
		//add meta constraint to hybrid planner
		
		simpleHybridPlanner.addMetaConstraint(metaOccupiedConstraint);		
		simpleHybridPlanner.addMetaConstraint(metaSpatialAdherence);

		final Vector<Activity> executingActs = new Vector<Activity>();

		Vector<DispatchingFunction> dispatches = new Vector<DispatchingFunction>();
		DispatchingFunction df = new DispatchingFunction("RobotAction") {
			@Override
			public void dispatch(Activity act) {
				System.out.println(">>>>>>>>>>>>>> Dispatched " + act);
//				//#####################################################################################################################
//				ActivityNetworkSolver actSolver = ((ActivityNetworkSolver)((SpatialFluentSolver)simpleHybridPlanner.getConstraintSolvers()[0]).getConstraintSolvers()[1]);
//				//sort Activity based on the start time for debugging purpose
//				HashMap<Activity, Long> starttimes = new HashMap<Activity, Long>();
//				for (int i = 0; i < actSolver.getVariables().length; i++) {
//					starttimes.put((Activity) actSolver.getVariables()[i], ((Activity)actSolver.getVariables()[i]).getTemporalVariable().getStart().getLowerBound());                       
//				}
//
//				//          Collections.sort(starttimes.values());
//				starttimes =  sortHashMapByValuesD(starttimes);
//				for (Activity act0 : starttimes.keySet()) {
//					System.out.println(act0 + " --> " + starttimes.get(act0));
//				}
//				//#####################################################################################################################
				
				executingActs.add(act);

			}
		};
		dispatches.add(df);

		DispatchingFunction dfSense = new DispatchingFunction("RobotSense") {
			@Override
			public void dispatch(Activity act) {
				System.out.println(">>>>>>>>>>>>>> Dispatched " + act);
				executingActs.add(act);
				if(counter == 0){
					counter ++;
				}
				else if(counter == 1){
					releaseActivity(groundSolver, act.getTemporalVariable().getLST() , getCreatedActivty(groundSolver, ctrls.get(1)));
					releaseActivity(groundSolver, act.getTemporalVariable().getLST() , getCreatedActivty(groundSolver, ctrls.get(2)));
					releaseActivity(groundSolver, act.getTemporalVariable().getLST() , getCreatedActivty(groundSolver, ctrls.get(3)));
					//insert assertions 
					Vector<SpatialAssertionalRelation> saRelations = new Vector<SpatialAssertionalRelation>(); 
					for (String st : currentObservation.keySet()) saRelations.add(currentObservation.get(st));
					metaSpatialAdherence.setSpatialAssertionalRelations(saRelations);	
					counter++;
				}
			}
		}; 
		dispatches.add(dfSense);

		animator.addDispatchingFunctions(simpleHybridPlanner, dispatches.toArray(new DispatchingFunction[dispatches.size()]));

		//######################################################################
		//Timeline
		ActivityNetworkSolver actSolver = ((ActivityNetworkSolver)((SpatialFluentSolver)simpleHybridPlanner.getConstraintSolvers()[0]).getConstraintSolvers()[1]);
		TimelinePublisher tp = new TimelinePublisher(actSolver, new Bounds(0,60000), true, "Time","RobotAction","RobotProprioception", "RobotSense","atLocation");
		TimelineVisualizer tv = new TimelineVisualizer(tp);
		tv.startAutomaticUpdate(1000);



		while (true) {
			System.out.println("Executing activities (press <enter> to refresh list):");
			for (int i = 0; i < executingActs.size(); i++) System.out.println(i + ". " + executingActs.elementAt(i));

			System.out.println("--");
			System.out.print("Please enter activity to finish: ");  
			String input = "";  
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));  
			try { input = br.readLine(); }
			catch (IOException e) { e.printStackTrace(); }
			if (!input.trim().equals("")) {
				df.finish(executingActs.elementAt(Integer.parseInt(input)));
				executingActs.remove(Integer.parseInt(input));
			}

		}

	}



	private static Activity getCreatedActivty(SpatialFluentSolver groundSolver, String actString) {

		String component = actString.substring(0, actString.indexOf("::")); //e.g., atLocation
		String actSymbol = actString.substring(actString.indexOf("::")+2, actString.indexOf("--")); //e.g., at_cup1_counter1
		String coordSym = actString.substring(actString.indexOf("--")+3, actString.indexOf("++") - 1); //e.g., 1,2,3,4
		String isMovable = actString.substring(actString.indexOf("++")+2, actString.length());//e.g., false
		String[] coords = coordSym.split(",");


		//		System.out.println("component: " + component);
		//		System.out.println("actSymbols: " + actSymbol);
		//		System.out.println("coord: " + coordSym);
		//		System.out.println("is Movable: " + isMovable);

		Activity act = null;
		for (int i = 0; i < groundSolver.getConstraintSolvers()[1].getVariables().length; i++) {
			//it has to have the same name and it should not be finished yet , i.e, it is on the plan
			
			if(((Activity)groundSolver.getConstraintSolvers()[1].getVariables()[i]).getSymbolicVariable().getSymbols()[0].toString().compareTo(actSymbol) == 0 && 
					((Activity)groundSolver.getConstraintSolvers()[1].getVariables()[i]).getTemporalVariable().getEET() != ((Activity)groundSolver.getConstraintSolvers()[1].getVariables()[i]).getTemporalVariable().getLET()){				
				act = ((Activity)groundSolver.getConstraintSolvers()[1].getVariables()[i]);
			}
				
		}
		
		SpatialFluent sf = (SpatialFluent)groundSolver.createVariable(component);
		String fluentId = actSymbol.substring(0, actSymbol.length() - 2);
		sf.setName(fluentId);//e.g., at_cup1_table1

		((RectangularRegion)sf.getInternalVariables()[0]).setName(fluentId);
		((Activity)sf.getInternalVariables()[1]).setSymbolicDomain(actSymbol);
		((Activity)sf.getInternalVariables()[1]).setMarking(markings.JUSTIFIED);
		

		//if this is already planned so it has to unified with the real observation i.e., spatial fluents
		if(act != null){
			AllenIntervalConstraint unify = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Equals, AllenIntervalConstraint.Type.Equals);
			unify.setFrom(sf.getActivity());
			unify.setTo(act);
			System.out.println("***********************************************************");
			System.out.println(unify);
			System.out.println("***********************************************************");
			groundSolver.getConstraintSolvers()[1].addConstraint(unify);
		}

		
		addDurationToActivity(groundSolver, duration, sf.getActivity());


		//update current observation
		updateObservation(fluentId, coords, isMovable);


		return sf.getActivity();
	}

	private static void addDurationToActivity(SpatialFluentSolver groundSolver, long duration, Activity act) {

		Vector<Constraint> cons = new Vector<Constraint>();
		AllenIntervalConstraint onDuration = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(duration,APSPSolver.INF));
		onDuration.setFrom(act);
		onDuration.setTo(act);
		cons.add(onDuration);
		groundSolver.getConstraintSolvers()[1].addConstraints(cons.toArray(new Constraint[cons.size()]));
	}
	
	private static void releaseActivity(SpatialFluentSolver groundSolver,
			long releaseTime, Activity act) {
		
		Vector<Constraint> cons = new Vector<Constraint>();
		AllenIntervalConstraint releaseHolding = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(releaseTime,releaseTime));
		releaseHolding.setFrom(act);
		releaseHolding.setTo(act);
		cons.add(releaseHolding);
		groundSolver.getConstraintSolvers()[1].addConstraints(cons.toArray(new Constraint[cons.size()]));		
	}

	private static void updateObservation(String fluentId, String[] coords,
			String isMovable) {

		boolean movable = Boolean.parseBoolean(isMovable);
		long xl, xu, yl, yu;
		xl = Long.parseLong(coords[0]);
		xu = Long.parseLong(coords[1]);
		yl = Long.parseLong(coords[2]);
		yu = Long.parseLong(coords[3]);

		//fluentId : at_fork1_table1
		//categoryconcept = fork_table
		String categoryInstace = fluentId.substring(fluentId.indexOf("_") + 1); //
		String categoryConcept = categoryInstace.replaceAll("[0-9]",""); 


		//#########################################################################
		System.out.println("====================================");
		System.out.println(xl + " "+ xu +" "+ yl + " "+ yu);
		System.out.println(movable);
		System.out.println(fluentId);
		System.out.println(categoryConcept);
		System.out.println("====================================");
		//#########################################################################



		if(xl == 0 && xu == 0 && yl == 0 && yu == 0){
			SpatialAssertionalRelation table_assertion = new SpatialAssertionalRelation(fluentId, categoryConcept);
			table_assertion.setUnaryAtRectangleConstraint(new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.At, 
					new Bounds(0, APSPSolver.INF), new Bounds(0, APSPSolver.INF), new Bounds(0, APSPSolver.INF), new Bounds(0, APSPSolver.INF)));
			OntologicalSpatialProperty tableOnto = new OntologicalSpatialProperty();
			tableOnto.setMovable(movable);
			table_assertion.setOntologicalProp(tableOnto);
			currentObservation.put(fluentId, table_assertion);


		}
		else{
			SpatialAssertionalRelation objectAssertion = new SpatialAssertionalRelation(fluentId, categoryConcept);
			objectAssertion.setUnaryAtRectangleConstraint(new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.At, 
					new Bounds(xl, xl), new Bounds(xu, xu), new Bounds(yl, yl), new Bounds(yu, yu)));
			OntologicalSpatialProperty tableOnto = new OntologicalSpatialProperty();
			tableOnto.setMovable(movable);
			objectAssertion.setOntologicalProp(tableOnto);
			currentObservation.put(fluentId, objectAssertion);

		}


	}

	private static LinkedHashMap sortHashMapByValuesD(HashMap passedMap) {
		ArrayList mapKeys = new ArrayList(passedMap.keySet());
		ArrayList mapValues = new ArrayList(passedMap.values());
		Collections.sort(mapValues);
		Collections.sort(mapKeys);

		LinkedHashMap sortedMap = 
				new LinkedHashMap();

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

	
	
	private static void getSpatialKnowledge(Vector<SpatialRule> srules){

		Bounds knife_size_x = new Bounds(4, 8);
		Bounds knife_size_y = new Bounds(18, 24);
		Bounds cup_size_x = new Bounds(4, 7);
		Bounds cup_size_y = new Bounds(4, 7);
		Bounds fork_size_x = new Bounds(4, 8);
		Bounds fork_size_y = new Bounds(18, 24);



		SpatialRule r7 = new SpatialRule("knife_table", "knife_table", 
				new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.Size, knife_size_x, knife_size_y));
		srules.add(r7);

		SpatialRule r8 = new SpatialRule("cup_table", "cup_table", 
				new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.Size, cup_size_x, cup_size_y));
		srules.add(r8);

		SpatialRule r9 = new SpatialRule("fork_table", "fork_table", 
				new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.Size, fork_size_x, fork_size_y));
		srules.add(r9);


		//Every thing should be on the table            
		addOnTableConstraint(srules, "fork_table");
		addOnTableConstraint(srules, "knife_table");
		addOnTableConstraint(srules, "cup_table");



		SpatialRule r2 = new SpatialRule("cup_table", "knife_table", 
				new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, new Bounds(15, 20)),
						new AllenIntervalConstraint(AllenIntervalConstraint.Type.During, AllenIntervalConstraint.Type.During.getDefaultBounds() ))
				);
		srules.add(r2);



		SpatialRule r3 = new SpatialRule("cup_table", "fork_table", 
				new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.After, new Bounds(15, 20)),
						new AllenIntervalConstraint(AllenIntervalConstraint.Type.During , AllenIntervalConstraint.Type.During.getDefaultBounds()))

				);
		srules.add(r3);


	}

	private static void addOnTableConstraint(Vector<SpatialRule> srules, String str){

		Bounds withinReach_y_lower = new Bounds(5, 20);
		Bounds withinReach_y_upper = new Bounds(5, APSPSolver.INF);
		Bounds withinReach_x_lower = new Bounds(5, APSPSolver.INF);
		Bounds withinReach_x_upper = new Bounds(5, APSPSolver.INF);

		SpatialRule r8 = new SpatialRule(str, "table_table", 
				new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.During, withinReach_x_lower,withinReach_x_upper),
						new AllenIntervalConstraint(AllenIntervalConstraint.Type.During, withinReach_y_lower, withinReach_y_upper))
				);
		srules.add(r8);

	}




}

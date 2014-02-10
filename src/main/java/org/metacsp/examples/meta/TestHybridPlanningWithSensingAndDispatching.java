package org.metacsp.examples.meta;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
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
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangleConstraint;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangularRegion;
import org.metacsp.multi.spatial.rectangleAlgebra.UnaryRectangleConstraint;
import org.metacsp.multi.spatioTemporal.SpatialFluent;
import org.metacsp.multi.spatioTemporal.SpatialFluentSolver;
import org.metacsp.sensing.ConstraintNetworkAnimator;
import org.metacsp.sensing.Controllable;
import org.metacsp.sensing.Sensor;
import org.metacsp.spatial.utility.SpatialAssertionalRelation;
import org.metacsp.spatial.utility.SpatialRule;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;
import org.metacsp.utility.timelinePlotting.TimelinePublisher;
import org.metacsp.utility.timelinePlotting.TimelineVisualizer;

public class TestHybridPlanningWithSensingAndDispatching {
	
	static int pad = 2;    
	static long duration = 5;
	public static void main(String[] args) {
		
		System.out.println("IRAN");
		//Create planner
		SimpleHybridPlanner simpleHybridPlanner = new SimpleHybridPlanner(0,100000,0);
		//MetaCSPLogging.setLevel(planner.getClass(), Level.FINEST);

		FluentBasedSimpleDomain.parseDomain(simpleHybridPlanner, "domains/testSensingBeforePickAndPlaceDomain.ddl", FluentBasedSimpleDomain.class);
		

		ConstraintNetworkAnimator animator = new ConstraintNetworkAnimator(simpleHybridPlanner, 1000);
		
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
		MetaSpatialAdherenceConstraint metaSpatialAdherence = new MetaSpatialAdherenceConstraint(varOH, valOH);
		SpatialFluentSolver groundSolver = (SpatialFluentSolver)simpleHybridPlanner.getConstraintSolvers()[0];

		MetaCSPLogging.setLevel(SimpleHybridPlanner.class, Level.FINEST);
		MetaCSPLogging.setLevel(MetaSpatialAdherenceConstraint.class, Level.FINEST);
		//#################################################################################################################
		//add metaOccupiedConstraint
		MetaOccupiedConstraint metaOccupiedConstraint = new MetaOccupiedConstraint(null, null);
		metaOccupiedConstraint.setPad(pad);
		//#################################################################################################################
		//add metaOccupiedConstraint
		SensingSchedulable sensingSchedulable = new SensingSchedulable(null, null);
		//#################################################################################################################
		//this is spatial general and assetional rule
		Vector<SpatialRule> srules = new Vector<SpatialRule>();
		Vector<SpatialAssertionalRelation> saRelations = new Vector<SpatialAssertionalRelation>();
		getSpatialKnowledge(srules);
//		observation = getAssertionalRule(saRelations);
		
		
		setFluentintoNetwork(groundSolver, "atLocation", "cup1", "at_cup1_table1()", markings.UNJUSTIFIED, -1);
		
		//#################################################################################################################
		//add spatial general and assertional rule to MetaSpatialFluentConstraint
		metaSpatialAdherence.setSpatialRules(srules.toArray(new SpatialRule[srules.size()]));
		metaSpatialAdherence.setSpatialAssertionalRelations(saRelations);
		metaSpatialAdherence.setInitialGoal(new String[]{"cup1"});


		//add meta constraint
//		simpleHybridPlanner.addMetaConstraint(sensingSchedulable);
//		simpleHybridPlanner.addMetaConstraint(metaOccupiedConstraint);
//		simpleHybridPlanner.addMetaConstraint(metaSpatialAdherence);
		
		
		//##############################################################################################################
		
		final Vector<Activity> executingActs = new Vector<Activity>();
		Vector<DispatchingFunction> dispatches = new Vector<DispatchingFunction>();
		DispatchingFunction df = new DispatchingFunction("RobotAction") {
			@Override
			public void dispatch(Activity act) {
				System.out.println(">>>>>>>>>>>>>> Dispatched " + act);
				executingActs.add(act);
			}
		};
		dispatches.add(df);
		
		DispatchingFunction dfSense = new DispatchingFunction("RobotSense") {
			@Override
			public void dispatch(Activity act) {
				System.out.println(">>>>>>>>>>>>>> Dispatched " + act);
				executingActs.add(act);
			}
		}; 
		dispatches.add(dfSense);
		
		animator.addDispatchingFunctions(simpleHybridPlanner, dispatches.toArray(new DispatchingFunction[dispatches.size()]));
		
//		Controllable cntlSensorA = new Controllable("RobotProprioception", animator);
		Controllable cntlSensorB = new Controllable("atLocation", animator);
		
//		cntlSensorA.registerControllableSensorTrace("sensorTraces/RobotProprioception.st");
		cntlSensorB.registerControllableSensorTrace("sensorTraces/atLocation.st");
		

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
	
	private static void getSpatialKnowledge(Vector<SpatialRule> srules){

		Bounds knife_size_x = new Bounds(4, 8);
		Bounds knife_size_y = new Bounds(18, 24);
		Bounds cup_size_x = new Bounds(4, 7);
		Bounds cup_size_y = new Bounds(4, 7);
		Bounds fork_size_x = new Bounds(4, 8);
		Bounds fork_size_y = new Bounds(18, 24);



		SpatialRule r7 = new SpatialRule("knife", "knife", 
				new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.Size, knife_size_x, knife_size_y));
		srules.add(r7);

		SpatialRule r8 = new SpatialRule("cup", "cup", 
				new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.Size, cup_size_x, cup_size_y));
		srules.add(r8);

		SpatialRule r9 = new SpatialRule("fork", "fork", 
				new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.Size, fork_size_x, fork_size_y));
		srules.add(r9);


		//Every thing should be on the table            
		addOnTableConstraint(srules, "fork");
		addOnTableConstraint(srules, "knife");
		addOnTableConstraint(srules, "cup");



		SpatialRule r2 = new SpatialRule("cup", "knife", 
				new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, new Bounds(15, 20)),
						new AllenIntervalConstraint(AllenIntervalConstraint.Type.During, AllenIntervalConstraint.Type.During.getDefaultBounds() ))
				);
		srules.add(r2);



		SpatialRule r3 = new SpatialRule("cup", "fork", 
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

		SpatialRule r8 = new SpatialRule(str, "table", 
				new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.During, withinReach_x_lower,withinReach_x_upper),
						new AllenIntervalConstraint(AllenIntervalConstraint.Type.During, withinReach_y_lower, withinReach_y_upper))
				);
		srules.add(r8);

	}


	
	private static void setFluentintoNetwork(SpatialFluentSolver grounSpatialFluentSolver, String component, 
			String name, String symbolicDomain, markings mk, long release){

		SpatialFluent sf = (SpatialFluent)grounSpatialFluentSolver.createVariable(component);
		sf.setName(name);

		((RectangularRegion)sf.getInternalVariables()[0]).setName(name);
		((Activity)sf.getInternalVariables()[1]).setSymbolicDomain(symbolicDomain);
		((Activity)sf.getInternalVariables()[1]).setMarking(mk);

		if(mk.equals(markings.JUSTIFIED)){
			AllenIntervalConstraint onDuration = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(duration,APSPSolver.INF));
			onDuration.setFrom(sf.getActivity());
			onDuration.setTo(sf.getActivity());
			

			AllenIntervalConstraint releaseOn = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(release, release));
			releaseOn.setFrom(sf.getActivity());
			releaseOn.setTo(sf.getActivity());
		}

	}
	

}

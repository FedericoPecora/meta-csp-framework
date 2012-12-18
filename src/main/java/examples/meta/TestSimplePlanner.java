package examples.meta;

import java.util.logging.Level;

import meta.simplePlanner.SimpleDomain;
import meta.simplePlanner.SimpleDomain.markings;
import meta.simplePlanner.SimpleOperator;
import meta.simplePlanner.SimplePlanner;
import meta.symbolsAndTime.Schedulable;
import multi.activity.Activity;
import multi.activity.ActivityNetworkSolver;
import multi.allenInterval.AllenIntervalConstraint;
import time.APSPSolver;
import time.Bounds;
import utility.logging.MetaCSPLogging;
import utility.timelinePlotting.TimelinePublisher;
import utility.timelinePlotting.TimelineVisualizer;
import framework.Constraint;
import framework.ConstraintNetwork;

public class TestSimplePlanner {	
	
	public static void main(String[] args) {

		MetaCSPLogging.setLevel(TimelinePublisher.class, Level.FINEST);

		SimplePlanner planner = new SimplePlanner(0,600,0);		
		// This is a pointer toward the ActivityNetwork solver of the Scheduler
		ActivityNetworkSolver groundSolver = (ActivityNetworkSolver)planner.getConstraintSolvers()[0];

		MetaCSPLogging.setLevel(planner.getClass(), Level.FINEST);
//		MetaCSPLogging.setLevel(Level.FINEST);
//		MetaCSPLogging.setLevel(planner.getClass(), Level.FINE);
				
		SimpleDomain rd = new SimpleDomain(new int[] {6,6,6}, new String[] {"power", "usbport", "serialport"}, "TestDomain");
			
		// Here I create two AllenIntervalConstraint for use in the operator I will define
		AllenIntervalConstraint durationMoveTo = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(5,APSPSolver.INF));
		AllenIntervalConstraint moveToDuringLocalization = new AllenIntervalConstraint(AllenIntervalConstraint.Type.During, AllenIntervalConstraint.Type.During.getDefaultBounds());
		
		// New operator: the first parameter is the name, the second are the constraints,
		// the third are requirement activities, fourth means
		// no usage of resources
		SimpleOperator operator1 = new SimpleOperator("Robot1::MoveTo()",
				new AllenIntervalConstraint[] {moveToDuringLocalization},
				new String[] {"LocalizationService::Localization()"},
				null);
		// We can add constraints to the operator even after it has been created
		// this is useful for adding unary constraints on the head (which has index 0)
		operator1.addConstraint(durationMoveTo, 0, 0);
		rd.addOperator(operator1);

		// We give robot 2 the same capability...
		SimpleOperator operator1a = new SimpleOperator("Robot2::MoveTo()",
				new AllenIntervalConstraint[] {moveToDuringLocalization},
				new String[] {"LocalizationService::Localization()"},
				null);
		operator1a.addConstraint((AllenIntervalConstraint)durationMoveTo, 0, 0);
		rd.addOperator(operator1a);

		// This operator states that the LocalizationService::Localization needs
		// RFIDReader1::On(power, serialport) and it 
		// doesn't consume resources
		AllenIntervalConstraint localizationDuringRFID = new AllenIntervalConstraint(AllenIntervalConstraint.Type.During, AllenIntervalConstraint.Type.During.getDefaultBounds());
		SimpleOperator operator2 = new SimpleOperator("LocalizationService::Localization()",
				new AllenIntervalConstraint[] {localizationDuringRFID},
				new String[] {"RFIDReader1::On(power,usbport)"},
				null);
		rd.addOperator(operator2);
		
		// This operator has the same name of the previous, but different requirements
		AllenIntervalConstraint localizationDuringLaser = new AllenIntervalConstraint(AllenIntervalConstraint.Type.During, AllenIntervalConstraint.Type.During.getDefaultBounds());
		SimpleOperator operator3 = new SimpleOperator("LocalizationService::Localization()",
				new AllenIntervalConstraint[] {localizationDuringLaser},
				new String[] {"LaserScanner1::On(power,serialport)"},
				null);
		rd.addOperator(operator3);
		
		// This operator has no requirement but consumes 5 units of
		// the first resource and one of the second
		SimpleOperator operator4 = new SimpleOperator("RFIDReader1::On(power,usbport)",
				null,
				null,
				new int[] {5,7});
		rd.addOperator(operator4);
		
		
		// Similar to the previous operator
		SimpleOperator operator5 = new SimpleOperator("LaserScanner1::On(power,serialport)",
				null,
				null,
				new int[] {5,1});
		rd.addOperator(operator5);

		//This adds the domain as a meta-constraint of the SimplePlanner
		planner.addMetaConstraint(rd);
		//... and we also add all its resources as separate meta-constraints
		for (Schedulable sch : rd.getSchedulingMetaConstraints()) planner.addMetaConstraint(sch);
		
		// INITIAL AND GOAL STATE DEFS
		Activity one = (Activity)groundSolver.createVariable("Robot1");
		one.setSymbolicDomain("MoveTo()");
		// ... this is a goal (i.e., an activity to justify through the meta-constraint)
		one.setMarking(markings.UNJUSTIFIED);
		//.. let's also give it a minimum duration
		AllenIntervalConstraint durationOne = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(7,APSPSolver.INF));
		durationOne.setFrom(one);
		durationOne.setTo(one);

		Activity two = (Activity)groundSolver.createVariable("Robot2");
		two.setSymbolicDomain("MoveTo()");
		two.setMarking(markings.UNJUSTIFIED);
		AllenIntervalConstraint durationTwo = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(7,APSPSolver.INF));
		durationTwo.setFrom(two);
		durationTwo.setTo(two);

		groundSolver.addConstraints(new Constraint[] {durationOne, durationTwo});
		
		// We can also specify that goals should be related in time somehow...		
//		AllenIntervalConstraint after = new AllenIntervalConstraint(AllenIntervalConstraint.Type.After, AllenIntervalConstraint.Type.After.getDefaultBounds());
//		after.setFrom(two);
//		after.setTo(one);
//		groundSolver.addConstraint(after);

		TimelinePublisher tp = new TimelinePublisher(groundSolver, new Bounds(0,25), "Robot1", "Robot2", "LocalizationService", "RFIDReader1", "LaserScanner1");
		//TimelinePublisher can also be instantiated w/o bounds, in which case the bounds are calculated every time publish is called
//		TimelinePublisher tp = new TimelinePublisher(groundSolver, "Robot1", "Robot2", "LocalizationService", "RFIDReader1", "LaserScanner1");
		TimelineVisualizer viz = new TimelineVisualizer(tp);
		tp.publish(false, false);
		//the following call is marked as "skippable" and will most likely be skipped because the previous call has not finished rendering...
		tp.publish(false, true);
		
		planner.backtrack();
		
		ConstraintNetwork.draw(groundSolver.getConstraintNetwork(), "Constraint Network");
		
		planner.draw();
		tp.publish(true, false);

	}
	
	

}

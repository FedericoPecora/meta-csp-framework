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
package org.metacsp.examples.meta;

import java.util.logging.Level;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.meta.simplePlanner.SimpleDomain;
import org.metacsp.meta.simplePlanner.SimpleDomain.markings;
import org.metacsp.meta.simplePlanner.SimpleOperator;
import org.metacsp.meta.simplePlanner.SimplePlanner;
import org.metacsp.meta.symbolsAndTime.Schedulable;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;
import org.metacsp.utility.timelinePlotting.TimelinePublisher;
import org.metacsp.utility.timelinePlotting.TimelineVisualizer;

public class TestSimplePlanner {	
	
	public static void main(String[] args) {

		MetaCSPLogging.setLevel(TimelinePublisher.class, Level.FINEST);

		SimplePlanner planner = new SimplePlanner(0,600,0);		
		// This is a pointer toward the ActivityNetwork solver of the Scheduler
		ActivityNetworkSolver groundSolver = (ActivityNetworkSolver)planner.getConstraintSolvers()[0];

		MetaCSPLogging.setLevel(planner.getClass(), Level.FINEST);
//		MetaCSPLogging.setLevel(Level.FINEST);
//		MetaCSPLogging.setLevel(planner.getClass(), Level.FINE);
		MetaCSPLogging.setLevel(SimpleDomain.class, Level.FINEST);
				
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
				new String[] {"RFIDReader1::On()"},
				null);
		rd.addOperator(operator2);
		
		// This operator has the same name of the previous, but different requirements
		AllenIntervalConstraint localizationDuringLaser = new AllenIntervalConstraint(AllenIntervalConstraint.Type.During, AllenIntervalConstraint.Type.During.getDefaultBounds());
		SimpleOperator operator3 = new SimpleOperator("LocalizationService::Localization()",
				new AllenIntervalConstraint[] {localizationDuringLaser},
				new String[] {"LaserScanner1::On()"},
				null);
		rd.addOperator(operator3);
		
		// This operator has no requirement but consumes 5 units of
		// the first resource and one of the second
		SimpleOperator operator4 = new SimpleOperator("RFIDReader1::On()",
				null,
				null,
				new int[] {5,7});
		rd.addOperator(operator4);
		
		
		// Similar to the previous operator
		SimpleOperator operator5 = new SimpleOperator("LaserScanner1::On()",
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

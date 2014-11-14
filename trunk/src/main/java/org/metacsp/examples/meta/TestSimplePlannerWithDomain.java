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

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.meta.simplePlanner.SimpleDomain;
import org.metacsp.meta.simplePlanner.SimpleDomain.markings;
import org.metacsp.meta.simplePlanner.SimplePlanner;
import org.metacsp.multi.activity.SymbolicVariableActivity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;
import org.metacsp.utility.timelinePlotting.TimelinePublisher;
import org.metacsp.utility.timelinePlotting.TimelineVisualizer;

public class TestSimplePlannerWithDomain {	
	
	public static void main(String[] args) {

		//Create planner
		SimplePlanner planner = new SimplePlanner(0,600,0);
		
		SimpleDomain.parseDomain(planner, "domains/testSimplePlanner.ddl", SimpleDomain.class);
		
		// This is a pointer toward the ground constraint network of the planner
		ActivityNetworkSolver groundSolver = (ActivityNetworkSolver)planner.getConstraintSolvers()[0];

		// INITIAL AND GOAL STATE DEFS
		SymbolicVariableActivity one = (SymbolicVariableActivity)groundSolver.createVariable("Robot1");
		one.setSymbolicDomain("MoveTo()");
		// ... this is a goal (i.e., an activity to justify through the meta-constraint)
		one.setMarking(markings.UNJUSTIFIED);
		//.. let's also give it a minimum duration
		AllenIntervalConstraint durationOne = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(7,APSPSolver.INF));
		durationOne.setFrom(one);
		durationOne.setTo(one);

		SymbolicVariableActivity two = (SymbolicVariableActivity)groundSolver.createVariable("Robot2");
		two.setSymbolicDomain("MoveTo()");
		two.setMarking(markings.UNJUSTIFIED);
		AllenIntervalConstraint durationTwo = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(7,APSPSolver.INF));
		durationTwo.setFrom(two);
		durationTwo.setTo(two);

		groundSolver.addConstraints(new Constraint[] {durationOne, durationTwo});
		
		// We can also specify that goals should be related in time somehow...		
//		AllenIntervalConstraint after = new AllenIntervalConstraint(AllenIntervalConstraint.Type.After, AllenIntervalConstraint.Type.After.getDefaultBounds());
//		after.setFrom(one);
//		after.setTo(two);
//		groundSolver.addConstraint(after);

		TimelinePublisher tp = new TimelinePublisher(groundSolver, "Robot1", "Robot2", "LocalizationService", "RFIDReader1", "LaserScanner1");
		TimelineVisualizer viz = new TimelineVisualizer(tp);
		tp.publish(false, false);
		
		planner.backtrack();
		
		ConstraintNetwork.draw(groundSolver.getConstraintNetwork(), "Constraint Network");
		
		planner.draw();
		tp.publish(true, false);

	}
	
	

}

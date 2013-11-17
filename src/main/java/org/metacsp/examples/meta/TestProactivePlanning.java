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
import org.metacsp.meta.simplePlanner.ProactivePlanningDomain;
import org.metacsp.meta.simplePlanner.SimpleDomain;
import org.metacsp.meta.simplePlanner.SimpleDomain.markings;
import org.metacsp.meta.simplePlanner.SimplePlanner;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;
import org.metacsp.utility.timelinePlotting.TimelinePublisher;
import org.metacsp.utility.timelinePlotting.TimelineVisualizer;

public class TestProactivePlanning {	
	
	public static void main(String[] args) {

		//Create planner
		SimplePlanner planner = new SimplePlanner(0,600,0);

		ProactivePlanningDomain.parseDomain(planner, "domains/testProactivePlanning.ddl");
		
		MetaCSPLogging.setLevel(planner.getClass(), Level.FINEST);

		// This is a pointer toward the ground constraint network of the planner
		ActivityNetworkSolver groundSolver = (ActivityNetworkSolver)planner.getConstraintSolvers()[0];
		
		// SENSORS: user in kitchen from 1 until at least 20
		Activity s1 = (Activity)groundSolver.createVariable("Location");
		s1.setSymbolicDomain("Kitchen()");
		// ... this is a sensor value (i.e., an activity that is already justified)
		s1.setMarking(markings.JUSTIFIED);
		//.. let's also give it a minimum duration
		AllenIntervalConstraint durationS1 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(20,APSPSolver.INF));
		durationS1.setFrom(s1);
		durationS1.setTo(s1);
		//Let's release it 
		AllenIntervalConstraint relS1 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(1,1));
		relS1.setFrom(s1);
		relS1.setTo(s1);
		
		// SENSORS: stove is on from 4 and lasts 10
		Activity s2 = (Activity)groundSolver.createVariable("Stove");
		s2.setSymbolicDomain("On()");
		// ... this is a sensor value (i.e., an activity that is already justified)
		s2.setMarking(markings.JUSTIFIED);
		//.. let's also give it a minimum duration
		AllenIntervalConstraint durationS2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(10,10));
		durationS2.setFrom(s2);
		durationS2.setTo(s2);
		//Let's release it 
		AllenIntervalConstraint relS2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(4,4));
		relS2.setFrom(s2);
		relS2.setTo(s2);

		groundSolver.addConstraints(new Constraint[] {durationS1,durationS2,relS1,relS2});

		planner.backtrack();
		
		ConstraintNetwork.draw(groundSolver.getConstraintNetwork(), "Constraint Network");
		
		planner.draw();
		TimelinePublisher tp = new TimelinePublisher(groundSolver, "Human", "Location", "Stove", "Robot", "LocalizationService", "LaserScanner", "RFIDReader");
		TimelineVisualizer viz = new TimelineVisualizer(tp);
		tp.publish(true, false);

	}
	
	

}

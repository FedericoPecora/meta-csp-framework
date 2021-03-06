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

import java.util.Calendar;
import java.util.logging.Level;

import org.metacsp.meta.simplePlanner.ProactivePlanningDomain;
import org.metacsp.meta.simplePlanner.SimplePlanner;
import org.metacsp.meta.simplePlanner.SimplePlannerInferenceCallback;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.sensing.ConstraintNetworkAnimator;
import org.metacsp.sensing.Sensor;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;
import org.metacsp.utility.timelinePlotting.TimelinePublisher;
import org.metacsp.utility.timelinePlotting.TimelineVisualizer;

public class TestContextInference {	
	
	public static void main(String[] args) {

		long origin = Calendar.getInstance().getTimeInMillis();
		//Create planner
		SimplePlanner planner = new SimplePlanner(origin,origin+100000,0);
		MetaCSPLogging.setLevel(planner.getClass(), Level.FINE);

		ProactivePlanningDomain.parseDomain(planner, "domains/testContextInference.ddl", ProactivePlanningDomain.class);

		//ConstraintNetworkAnimator animator = new ConstraintNetworkAnimator(planner, 1000);
		ActivityNetworkSolver ans = (ActivityNetworkSolver)planner.getConstraintSolvers()[0];
		SimplePlannerInferenceCallback cb = new SimplePlannerInferenceCallback(planner);
		ConstraintNetworkAnimator animator = new ConstraintNetworkAnimator(ans, 1000, cb);
		
		Sensor sensorA = new Sensor("Location", animator);
		Sensor sensorB = new Sensor("Stove", animator);
		
		sensorA.registerSensorTrace("sensorTraces/location.st",origin);
		sensorB.registerSensorTrace("sensorTraces/stove.st",origin);
		
		//TimelinePublisher tp = new TimelinePublisher(ans, new Bounds(0,60000), true, "Time", "Location", "Stove", "Human", "RFIDReader");
		TimelinePublisher tp = new TimelinePublisher(ans.getConstraintNetwork(), new Bounds(0,60000), true, "Time", "Location", "Stove", "Human", "RFIDReader");
		TimelineVisualizer tv = new TimelineVisualizer(tp);
		tv.startAutomaticUpdate(1000);

	}
	
	

}

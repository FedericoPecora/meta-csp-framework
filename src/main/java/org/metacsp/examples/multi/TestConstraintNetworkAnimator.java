package org.metacsp.examples.multi;

import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.sensing.ConstraintNetworkAnimator;
import org.metacsp.sensing.Sensor;
import org.metacsp.time.Bounds;
import org.metacsp.utility.timelinePlotting.TimelinePublisher;
import org.metacsp.utility.timelinePlotting.TimelineVisualizer;

public class TestConstraintNetworkAnimator {
		
	public static void main(String[] args) {
		ActivityNetworkSolver ans = new ActivityNetworkSolver(0, 100000);
		ConstraintNetworkAnimator animator = new ConstraintNetworkAnimator(ans, 1000);
		
		Sensor sensorA = new Sensor("SensorA", animator);
		Sensor sensorB = new Sensor("SensorB", animator);
		
		sensorA.registerSensorTrace("sensorTraces/sensorA.st");
		sensorB.registerSensorTrace("sensorTraces/sensorB.st");

		//TimelinePublisher tp = new TimelinePublisher(ans, new Bounds(0,60000), true, "Time", "SensorA", "SensorB");
		TimelinePublisher tp = new TimelinePublisher(ans.getConstraintNetwork(), new Bounds(0,60000), true, "Time", "SensorA", "SensorB");
		tp.setTemporalResolution(1);
		TimelineVisualizer tv = new TimelineVisualizer(tp);
		tv.startAutomaticUpdate(1000);
		
	}

}

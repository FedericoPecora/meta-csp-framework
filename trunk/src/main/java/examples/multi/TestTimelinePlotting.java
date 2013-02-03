package examples.multi;

import java.util.logging.Level;

import multi.activity.Activity;
import multi.activity.ActivityNetworkSolver;
import multi.allenInterval.AllenIntervalConstraint;
import symbols.SymbolicValueConstraint;
import time.Bounds;
import utility.logging.MetaCSPLogging;
import utility.timelinePlotting.TimelinePublisher;
import utility.timelinePlotting.TimelineVisualizer;
import framework.Constraint;

public class TestTimelinePlotting {
	
	public static void main(String[] args) {
		
		MetaCSPLogging.setLevel(TimelinePublisher.class, Level.FINEST);
		ActivityNetworkSolver solver = new ActivityNetworkSolver(10,1000);
		Activity act1 = (Activity)solver.createVariable("One Component");
		act1.setSymbolicDomain("A", "B", "C");
		Activity act2 = (Activity)solver.createVariable("Another Component");
		act2.setSymbolicDomain("B", "C");
		
		SymbolicValueConstraint con1 = new SymbolicValueConstraint(SymbolicValueConstraint.Type.EQUALS);
		con1.setFrom(act1);
		con1.setTo(act2);
		//solver.addConstraint(con1);
		
		AllenIntervalConstraint dur1 = new AllenIntervalConstraint( AllenIntervalConstraint.Type.Duration, new Bounds(10, 20));
		dur1.setFrom(act1);
		dur1.setTo(act1);
		//solver.addConstraint(dur1);
		
		AllenIntervalConstraint dur2 = new AllenIntervalConstraint( AllenIntervalConstraint.Type.Duration, new Bounds(10, 20));
		dur2.setFrom(act2);
		dur2.setTo(act2);
		//solver.addConstraint(dur1);
		
		AllenIntervalConstraint con2 = new AllenIntervalConstraint( AllenIntervalConstraint.Type.Before, new Bounds(10, 20));
		con2.setFrom(act1);
		con2.setTo(act2);
		//solver.addConstraint(con2);
		
		Constraint[] cons = new Constraint[]{dur1,dur2,con1,con2};
		solver.addConstraints(cons);
		
		TimelinePublisher tp = new TimelinePublisher(solver, "One Component", "Another Component");
		tp.setTemporalResolution(1000);
		TimelineVisualizer tv = new TimelineVisualizer(tp);
		
		tp.publish(false, true);
		
		AllenIntervalConstraint con3 = null;
		
		for (int i = 0; i < 200; i++) {
			try { Thread.sleep(500); }
			catch (InterruptedException e) { e.printStackTrace(); }
			if (con3 != null) solver.removeConstraint(con3);
			con3 = new AllenIntervalConstraint( AllenIntervalConstraint.Type.Release, new Bounds(7+i, 10+i));
			con3.setFrom(act1);
			con3.setTo(act1);
			solver.addConstraint(con3);
			tp.publish(false, true);
		}		
	}
	

}

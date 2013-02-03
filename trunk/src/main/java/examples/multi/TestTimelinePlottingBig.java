package examples.multi;

import java.util.Random;
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

public class TestTimelinePlottingBig {
	
	public static void main(String[] args) {
		
		MetaCSPLogging.setLevel(TimelinePublisher.class, Level.FINE);
		ActivityNetworkSolver solver = new ActivityNetworkSolver(10,10000,1000);
		Random rand = new Random(12314);
		TimelinePublisher tp = new TimelinePublisher(solver, new Bounds(0,30), true, "aComponent");
		tp.setTemporalResolution(1000);
		TimelineVisualizer tv = new TimelineVisualizer(tp);
		
		long oldEnd = 0;
		int numact = 0;
		while(true) {
			Activity act = (Activity)solver.createVariable("aComponent");
			long v = rand.nextInt(100);
			act.setSymbolicDomain(v + "");
			long l = rand.nextInt(5);
			long u = l+rand.nextInt(5);
			AllenIntervalConstraint dur = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(l,u));
			dur.setFrom(act);
			dur.setTo(act);
			AllenIntervalConstraint release = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(oldEnd,oldEnd));
			release.setFrom(act);
			release.setTo(act);
			solver.addConstraints(new Constraint[]{dur,release});
			tp.publish(false, true);
			oldEnd += u;
			System.out.println(++numact);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	

}

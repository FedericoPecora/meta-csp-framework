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
package examples.multi;

import java.util.Random;
import java.util.logging.Level;

import multi.activity.Activity;
import multi.activity.ActivityNetworkSolver;
import multi.allenInterval.AllenIntervalConstraint;
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
			//rndm duration in [1,8]
			long l = rand.nextInt(5)+1;
			long u = l+rand.nextInt(5);
			AllenIntervalConstraint dur = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(l,u));
			dur.setFrom(act);
			dur.setTo(act);
			AllenIntervalConstraint release = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(oldEnd,oldEnd));
			release.setFrom(act);
			release.setTo(act);
			solver.addConstraints(new Constraint[]{dur,release});
			tp.publish(false, true);
			oldEnd = act.getTemporalVariable().getEET();
			try { Thread.sleep(1000); }
			catch (InterruptedException e) { e.printStackTrace(); }
		}
	}
	

}

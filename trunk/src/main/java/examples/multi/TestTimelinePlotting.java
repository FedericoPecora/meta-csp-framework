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

import java.util.Calendar;
import java.util.logging.Level;

import multi.activity.Activity;
import multi.activity.ActivityNetworkSolver;
import multi.allenInterval.AllenIntervalConstraint;
import multi.symbols.SymbolicValueConstraint;
import time.Bounds;
import utility.logging.MetaCSPLogging;
import utility.timelinePlotting.TimelinePublisher;
import utility.timelinePlotting.TimelineVisualizer;
import framework.Constraint;
import framework.ConstraintNetwork;

public class TestTimelinePlotting {
	
	public static void main(String[] args) {
		
		MetaCSPLogging.setLevel(TimelinePublisher.class, Level.FINEST);
		long timeNow = Calendar.getInstance().getTimeInMillis();
		ActivityNetworkSolver solver = new ActivityNetworkSolver(timeNow,timeNow+1000, new String[] {"A","B","C","D"});
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
		//tp.setTemporalResolution(1000);
		TimelineVisualizer tv = new TimelineVisualizer(tp);
		
		tp.publish(false, true);
		
		ConstraintNetwork.draw(solver.getConstraintSolvers()[0].getConstraintNetwork());
		
		AllenIntervalConstraint con3 = null;
		
		for (int i = 0; i < 200; i++) {
			try { Thread.sleep(500); }
			catch (InterruptedException e) { e.printStackTrace(); }
			if (con3 != null) solver.removeConstraint(con3);
			con3 = new AllenIntervalConstraint( AllenIntervalConstraint.Type.Release, new Bounds(solver.getOrigin()+7+i, solver.getOrigin()+10+i));
			con3.setFrom(act1);
			con3.setTo(act1);
			solver.addConstraint(con3);
			tp.publish(false, true);
		}		
	}
	

}

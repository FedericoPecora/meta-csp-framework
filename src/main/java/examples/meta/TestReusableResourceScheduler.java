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
package examples.meta;



import meta.symbolsAndTime.ReusableResource;
import meta.symbolsAndTime.Scheduler;
import meta.symbolsAndTime.SymbolicTimeline;
import multi.activity.Activity;
import multi.activity.ActivityNetworkSolver;
import multi.allenInterval.AllenIntervalConstraint;
import multi.allenInterval.AllenIntervalConstraint.Type;
import time.APSPSolver;
import time.Bounds;
import utility.UI.Callback;
import utility.timelinePlotting.TimelinePublisher;
import utility.timelinePlotting.TimelineVisualizer;
import framework.Constraint;
import framework.ConstraintNetwork;
import framework.ValueOrderingH;
import framework.VariableOrderingH;

public class TestReusableResourceScheduler {
	
	public static void main(String[] args) {
		
		final Scheduler metaSolver = new Scheduler(0,600,0);
		final ActivityNetworkSolver groundSolver = (ActivityNetworkSolver)metaSolver.getConstraintSolvers()[0];
		
		Activity one = (Activity)groundSolver.createVariable("comp1");
		one.setSymbolicDomain("2");
		Activity two = (Activity)groundSolver.createVariable("comp1");
		two.setSymbolicDomain("1");
		Activity three = (Activity)groundSolver.createVariable("comp1");
		three.setSymbolicDomain("3");
		
		//DURATIONS
		AllenIntervalConstraint dur1 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(35, 55));
		dur1.setFrom(one);
		dur1.setTo(one);
		AllenIntervalConstraint dur2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(35, 55));
		dur2.setFrom(two);
		dur2.setTo(two);
		AllenIntervalConstraint dur3 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(35, 55));
		dur3.setFrom(three);
		dur3.setTo(three);

		//PRECEDENCES
		AllenIntervalConstraint con1 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, Type.Before.getDefaultBounds());
		con1.setFrom(one);
		con1.setTo(two);
		
		//Add the constraints
		Constraint[] cons = new Constraint[]{dur1,dur2,dur3,con1/*,eqOne,eqTwo,eqThree*/};
		groundSolver.addConstraints(cons);
		
		//Most critical conflict is the one with most activities (largest peak)
		VariableOrderingH varOH = new VariableOrderingH() {
			@Override
			public int compare(ConstraintNetwork arg0, ConstraintNetwork arg1) {
				// TODO Auto-generated method stub
				return arg1.getVariables().length - arg0.getVariables().length;
			}

			@Override
			public void collectData(ConstraintNetwork[] allMetaVariables) {
				// TODO Auto-generated method stub
				
			}
		};
		
		ValueOrderingH valOH = new ValueOrderingH() {
			@Override
			public int compare(ConstraintNetwork o1, ConstraintNetwork o2) {
				// TODO Auto-generated method stub
				return 0;
			}
		};
		
		ReusableResource rr1 = new ReusableResource(varOH, valOH, 4);
		ReusableResource rr2 = new ReusableResource(varOH, valOH, 3);
		rr1.setUsage(one,two,three);
		//rr2.setUsage(oneA,twoA,threeA);
		rr2.setUsage(two,three);
		metaSolver.addMetaConstraint(rr1);
		metaSolver.addMetaConstraint(rr2);
		
		//System.out.println(Arrays.toString(sv.getMetaVariables()));
		
//		SymbolicTimeline tl = new SymbolicTimeline(groundSolver,"comp1");
//		tl.draw();
		
		final TimelinePublisher tp = new TimelinePublisher(groundSolver, "comp1");
		TimelineVisualizer viz = new TimelineVisualizer(tp);
		
		tp.publish(true, true);
				
		//System.out.println("SOLVED? " + metaSolver.backtrack());
		Callback cb = new Callback() {
			@Override
			public void performOperation() {
				metaSolver.backtrack();
//				SymbolicTimeline tl1 = new SymbolicTimeline(groundSolver,"comp1");
//				tl1.draw();
				System.out.println(metaSolver.getDescription());
				metaSolver.draw();
				tp.publish(true, true);
			}
		};
		ConstraintNetwork.draw(groundSolver.getConstraintNetwork(),cb);
		
	}

}

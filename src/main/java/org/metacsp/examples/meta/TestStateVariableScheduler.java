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

import org.metacsp.meta.symbolsAndTime.Scheduler;
import org.metacsp.meta.symbolsAndTime.StateVariable;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.Bounds;
import org.metacsp.utility.UI.Callback;
import org.metacsp.utility.timelinePlotting.TimelinePublisher;
import org.metacsp.utility.timelinePlotting.TimelineVisualizer;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.VariableOrderingH;

public class TestStateVariableScheduler {
	
	public static void main(String[] args) {
		
		final Scheduler metaSolver = new Scheduler(0,600,0);
		final ActivityNetworkSolver groundSolver = (ActivityNetworkSolver)metaSolver.getConstraintSolvers()[0];
		
		Activity one = (Activity)groundSolver.createVariable("comp1");
		one.setSymbolicDomain("F", "G");
		Activity oneA = (Activity)groundSolver.createVariable("comp1");
		oneA.setSymbolicDomain("A", "B", "C");
		Activity oneB = (Activity)groundSolver.createVariable("comp1");
		oneB.setSymbolicDomain("D", "E");

		Activity oneAA = (Activity)groundSolver.createVariable("comp1");
		oneAA.setSymbolicDomain("A", "G");
		Activity oneAB = (Activity)groundSolver.createVariable("comp1");
		oneAB.setSymbolicDomain("B", "F");
		Activity oneAC = (Activity)groundSolver.createVariable("comp1");
		oneAC.setSymbolicDomain("C", "E");

		//metaSolver.draw();

		//DURATIONS
		AllenIntervalConstraint dur1 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(35, 55));
		dur1.setFrom(oneA);
		dur1.setTo(oneA);
		AllenIntervalConstraint dur2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(35, 55));
		dur2.setFrom(oneB);
		dur2.setTo(oneB);
		AllenIntervalConstraint dur3 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(35, 55));
		dur3.setFrom(one);
		dur3.setTo(one);
		AllenIntervalConstraint dur4 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(35, 55));
		dur4.setFrom(oneAA);
		dur4.setTo(oneAA);
		AllenIntervalConstraint dur5 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(35, 55));
		dur5.setFrom(oneAB);
		dur5.setTo(oneAB);
		AllenIntervalConstraint dur6 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(10, 25));
		dur6.setFrom(oneAC);
		dur6.setTo(oneAC);


		//PRECEDENCES
		AllenIntervalConstraint con1 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, AllenIntervalConstraint.Type.Before.getDefaultBounds());
		con1.setFrom(one);
		con1.setTo(oneA);
		AllenIntervalConstraint con2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, AllenIntervalConstraint.Type.Before.getDefaultBounds());
		con2.setFrom(one);
		con2.setTo(oneB);
		AllenIntervalConstraint con3 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, AllenIntervalConstraint.Type.Before.getDefaultBounds());
		con3.setFrom(oneA);
		con3.setTo(oneAA);
		AllenIntervalConstraint con4 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, AllenIntervalConstraint.Type.Before.getDefaultBounds());
		con4.setFrom(oneA);
		con4.setTo(oneAB);
		AllenIntervalConstraint con5 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, AllenIntervalConstraint.Type.Before.getDefaultBounds());
		con5.setFrom(oneA);
		con5.setTo(oneAC);
	
		//Add the constraints
		Constraint[] cons = new Constraint[]{dur1,dur2,dur3,dur4,dur5,dur6,con1,con2,con3,con4,con5};
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
		
		StateVariable sv = new StateVariable(varOH, valOH, metaSolver, new String[] {"A", "B", "C", "D", "E", "F", "G"});
		sv.setUsage(one,oneA,oneB,oneAA,oneAB,oneAC);
		metaSolver.addMetaConstraint(sv);
		
		//System.out.println(Arrays.toString(sv.getMetaVariables()));

		final TimelinePublisher tp = new TimelinePublisher(groundSolver, "comp1");
		TimelineVisualizer viz = new TimelineVisualizer(tp);
		
		tp.publish(true, true);
//		SymbolicTimeline tl = new SymbolicTimeline(groundSolver,"comp1");
//		tl.draw();

		Callback cb = new Callback() {
			@Override
			public void performOperation() {
				System.out.println("SOLVED? " + metaSolver.backtrack());				
//				SymbolicTimeline tl1 = new SymbolicTimeline(groundSolver,"comp1");
//				tl1.draw();
				tp.publish(false, true);
			}
		};
		ConstraintNetwork.draw(groundSolver.getConstraintNetwork(),cb);
		
	}

}

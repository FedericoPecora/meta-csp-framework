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
package org.metacsp.examples.multi;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintNetworkChangeEvent;
import org.metacsp.framework.ConstraintNetworkChangeListener;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.symbols.SymbolicValueConstraint;
import org.metacsp.time.Bounds;

public class TestConstraintNetworkListener {
	
	public static void main(String[] args) {
				
		ConstraintNetworkChangeListener cncl = new ConstraintNetworkChangeListener() {
			private int numinvocations = 0;
			@Override
			public void stateChanged(ConstraintNetworkChangeEvent event) {
				System.out.println("Added: " + event.getAdded());
				System.out.println("Removed: " + event.getRemoved());
				System.out.println("Invocation: " + ++numinvocations);
			}
		};

		
		ActivityNetworkSolver solver = new ActivityNetworkSolver(0,500, new String[] {"A","B","C","D","E","F"});
		Activity act1 = (Activity)solver.createVariable();
		act1.setSymbolicDomain("A", "B", "C");
		Activity act2 = (Activity)solver.createVariable();
		act2.setSymbolicDomain("B", "C", "D");
		
		solver.getConstraintNetwork().addConstraintNetworkChangeListener(cncl);
		
		ConstraintNetwork.draw(solver.getConstraintSolvers()[1].getConstraintNetwork());
		
		SymbolicValueConstraint con1 = new SymbolicValueConstraint(SymbolicValueConstraint.Type.EQUALS);
		con1.setFrom(act1);
		con1.setTo(act2);

		SymbolicValueConstraint con1a = new SymbolicValueConstraint(SymbolicValueConstraint.Type.VALUESUBSET);
		con1a.setFrom(act1);
		con1a.setTo(act1);
		con1a.setValue("B","C");

		AllenIntervalConstraint con2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, new Bounds(10, 20));
		con2.setFrom(act1);
		con2.setTo(act2);

		AllenIntervalConstraint con3 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(5, 5));
		con3.setFrom(act1);
		con3.setTo(act1);

		AllenIntervalConstraint con4 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(5, 5));
		con4.setFrom(act2);
		con4.setTo(act2);

		AllenIntervalConstraint con5 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(13, solver.getHorizon()));
		con5.setFrom(act2);
		con5.setTo(act2);

		AllenIntervalConstraint con5a = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(13, solver.getHorizon()));
		con5a.setFrom(act2);
		con5a.setTo(act2);

		solver.addConstraints(con1, con1a, con2);
		
		solver.addConstraints(con3, con4, con5, con5a);

	}

}

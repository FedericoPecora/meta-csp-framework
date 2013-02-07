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

import java.util.logging.Level;

import multi.activity.Activity;
import multi.activity.ActivityNetworkSolver;
import multi.allenInterval.AllenIntervalConstraint;
import symbols.SymbolicValueConstraint;
import time.Bounds;
import utility.logging.MetaCSPLogging;
import utility.timelinePlotting.TimelinePublisher;
import framework.Constraint;
import framework.ConstraintNetwork;

public class TestActivityNetworkSolver {
	
	public void forDebug() {
		ActivityNetworkSolver solver = new ActivityNetworkSolver(0,100);
		Activity act1 = (Activity)solver.createVariable();
		act1.setSymbolicDomain("A", "B", "C");
		Activity act2 = (Activity)solver.createVariable();
		act2.setSymbolicDomain("B", "C");

		//DRAW IT!
		ConstraintNetwork.draw(solver.getConstraintNetwork());
		
		SymbolicValueConstraint con1 = new SymbolicValueConstraint(SymbolicValueConstraint.Type.EQUALS);
		con1.setFrom(act1);
		con1.setTo(act2);
		//solver.addConstraint(con1);
		
		AllenIntervalConstraint con2 = new AllenIntervalConstraint( AllenIntervalConstraint.Type.Before, new Bounds(10, 20));
		con2.setFrom(act1);
		con2.setTo(act2);
		//solver.addConstraint(con2);
		
		Constraint[] cons = new Constraint[]{con1,con2};
		solver.addConstraints(cons);
		
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		
		boolean add = false;
		
		while (true) {
			for (int i = 0; i < cons.length; i++) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (add) solver.addConstraint(cons[i]);
				else solver.removeConstraint(cons[i]);
			}
			add = !add;
		}
		
	}
	
	public static void main(String[] args) {
		ActivityNetworkSolver solver = new ActivityNetworkSolver(0,100);
		Activity act1 = (Activity)solver.createVariable();
		act1.setSymbolicDomain("A", "B", "C");
		Activity act2 = (Activity)solver.createVariable();
		act2.setSymbolicDomain("B", "C");
		
		ConstraintNetwork.draw(solver.getConstraintNetwork());

		MetaCSPLogging.setLevel(Level.FINEST);
//		MetaCSPLogging.setLevel(solver.getClass(), Level.FINEST);
//		MetaCSPLogging.setLevel(solver.getConstraintSolvers()[0].getClass(), Level.FINEST);

		SymbolicValueConstraint con1 = new SymbolicValueConstraint(SymbolicValueConstraint.Type.EQUALS);
		con1.setFrom(act1);
		con1.setTo(act2);
		//solver.addConstraint(con1);
		
		AllenIntervalConstraint con2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, new Bounds(10, 20));
		con2.setFrom(act1);
		con2.setTo(act2);
		//solver.addConstraint(con2);
		
		Constraint[] cons = new Constraint[]{con1,con2};
		solver.addConstraints(cons);
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(solver.getDescription());
		System.out.println(act1.getDescription());		


	}

}

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
package org.metacsp.examples;

import java.util.logging.Level;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.SimpleDistanceConstraint;
import org.metacsp.utility.logging.MetaCSPLogging;

public class TestAPSPSolver {
	
	public static void main(String[] args) {
				
		APSPSolver solver = new APSPSolver(100, 500);
		
		MetaCSPLogging.setLevel(solver.getClass(), Level.FINE);

		//solver.setOptions(framework.ConstraintSolver.OPTIONS.AUTO_PROPAGATE);
		Variable[] vars = solver.createVariables(3);
		Variable one = vars[0];
		Variable two = vars[1];
		Variable three = vars[2];

		ConstraintNetwork.draw(solver.getConstraintNetwork());

		SimpleDistanceConstraint con1 = new SimpleDistanceConstraint();
		con1.setFrom(solver.getVariable(0));
		con1.setTo(one);
		con1.setMinimum(60);
		con1.setMaximum(75);
		
		SimpleDistanceConstraint con2 = new SimpleDistanceConstraint();
		con2.setFrom(one);
		con2.setTo(two);
		con2.setMinimum(7);
		con2.setMaximum(9);
		
		SimpleDistanceConstraint con3 = new SimpleDistanceConstraint();
		con3.setFrom(solver.getVariable(0));
		con3.setTo(two);
		con3.setMinimum(68);
		con3.setMaximum(70);

//		System.out.println(solver.addConstraint(con1)); //O(n^2)
//		System.out.println(solver.addConstraint(con2)); //O(n^2)
//		System.out.println(solver.addConstraint(con3)); //O(n^2)

		System.out.println(solver.addConstraints(new SimpleDistanceConstraint[] {con1,con2,con3}));
		
//		System.out.println("(Domain,Value) of " + one.getID() + ": (" + one.getDomain() + "," + one.getDomain().chooseValue("ET") + ")");
//		System.out.println("(*Domain,Value) of " + two.getID() + ": (" + two.getDomain() + "," + two.getDomain().chooseValue("LT") + ")");		
		
		SimpleDistanceConstraint con4 = new SimpleDistanceConstraint();
		con4.setFrom(two);
		con4.setTo(three);
		con4.setMinimum(56);
		con4.setMaximum(100);

		System.out.println(solver.addConstraint(con4));

		SimpleDistanceConstraint con5 = new SimpleDistanceConstraint();
		con5.setFrom(one);
		con5.setTo(three);
		con5.setMinimum(70);
		con5.setMaximum(100);
		solver.addConstraint(con5);
		
		while (true) {
			solver.addConstraint(con5);
			System.out.println("Rigidity: " + solver.getRMSRigidity());
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			solver.removeConstraint(con2);
			System.out.println("Rigidity: " + solver.getRMSRigidity());
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			solver.removeConstraint(con5);
			System.out.println("Rigidity: " + solver.getRMSRigidity());
	
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
			solver.addConstraint(con2);
			System.out.println("Rigidity: " + solver.getRMSRigidity());
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
		//solver.draw();

	}

}

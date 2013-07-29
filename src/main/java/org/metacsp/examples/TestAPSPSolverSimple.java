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

import org.metacsp.time.APSPSolver;
import org.metacsp.time.SimpleDistanceConstraint;
import org.metacsp.utility.logging.MetaCSPLogging;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;


public class TestAPSPSolverSimple {
	
	public static void main(String[] args) {
				
		APSPSolver solver = new APSPSolver(100, 500);
		
		MetaCSPLogging.setLevel(solver.getClass(), Level.FINE);

		//solver.setOptions(framework.ConstraintSolver.OPTIONS.AUTO_PROPAGATE);
		Variable[] vars = solver.createVariables(2);
		Variable one = vars[0];
		Variable two = vars[1];

		ConstraintNetwork.draw(solver.getConstraintNetwork());

		SimpleDistanceConstraint con1 = new SimpleDistanceConstraint();
		con1.setFrom(solver.getVariable(0));
		con1.setTo(one);
		con1.setMinimum(60);
		con1.setMaximum(65);
				
		SimpleDistanceConstraint con3 = new SimpleDistanceConstraint();
		con3.setFrom(solver.getVariable(0));
		con3.setTo(two);
		con3.setMinimum(70);
		con3.setMaximum(75);

		System.out.println("Adding constraint " + con1 + "...");
		System.out.println(solver.addConstraint(con1));

		System.out.println("Adding constraint " + con3 + "...");
		System.out.println(solver.addConstraint(con3));

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		SimpleDistanceConstraint con2 = new SimpleDistanceConstraint();
		con2.setFrom(two);
		con2.setTo(one);
		con2.setMinimum(2);
		con2.setMaximum(100);
		
		System.out.println("Adding constraint " + con2 + "...");
		System.out.println(solver.addConstraint(con2));

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		solver.removeConstraints(new SimpleDistanceConstraint[] {con1,con3});
		System.out.println("Removed constraints...");
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		System.out.println("Re-adding constraints (1): " + solver.addConstraints(new SimpleDistanceConstraint[] {con1,con3}));

		
	}

}

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


public class TestAPSPSolverIndependentConstraints {
	
	public static void main(String[] args) {
				
		APSPSolver solver = new APSPSolver(0, 100, 10);
		
		MetaCSPLogging.setLevel(solver.getClass(), Level.FINE);

		//solver.setOptions(framework.ConstraintSolver.OPTIONS.AUTO_PROPAGATE);
		Variable[] vars = solver.createVariables(4);

		SimpleDistanceConstraint con1 = new SimpleDistanceConstraint();
		con1.setFrom(vars[0]);
		con1.setTo(vars[1]);
		con1.setMinimum(0);
		con1.setMaximum(APSPSolver.INF);

		SimpleDistanceConstraint con2 = new SimpleDistanceConstraint();
		con2.setFrom(vars[2]);
		con2.setTo(vars[3]);
		con2.setMinimum(0);
		con2.setMaximum(APSPSolver.INF);

		System.out.println(solver.printDist());

		solver.addConstraints(con1,con2);

		System.out.println("-------------------");
		System.out.println(solver.printDist());
		
		System.out.println("AAAAAAAAAAA");
		
		solver = new APSPSolver(0, 100, 10);
		
		MetaCSPLogging.setLevel(solver.getClass(), Level.FINE);

		//solver.setOptions(framework.ConstraintSolver.OPTIONS.AUTO_PROPAGATE);
		vars = solver.createVariables(4);

		con1 = new SimpleDistanceConstraint();
		con1.setFrom(vars[0]);
		con1.setTo(vars[1]);
		con1.setMinimum(0);
		con1.setMaximum(APSPSolver.INF);

		con2 = new SimpleDistanceConstraint();
		con2.setFrom(vars[2]);
		con2.setTo(vars[3]);
		con2.setMinimum(0);
		con2.setMaximum(APSPSolver.INF);

		System.out.println(solver.printDist());

		solver.setAddingIndependentConstraints();
		solver.addConstraints(con1,con2);

		System.out.println("-------------------");
		System.out.println(solver.printDist());
		
	}

}

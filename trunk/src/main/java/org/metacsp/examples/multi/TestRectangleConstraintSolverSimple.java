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

import java.util.Vector;
import java.util.logging.Level;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangleConstraint;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangleConstraintSolver;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangularRegion;
import org.metacsp.multi.spatial.rectangleAlgebra.UnaryRectangleConstraint;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;

public class TestRectangleConstraintSolverSimple {

	/**
	 * @param args
	 */
	
	public static void main(String[] args) {
	
		RectangleConstraintSolver solver = new RectangleConstraintSolver(0,1000);
		Vector<Constraint> allConstraints = new Vector<Constraint>();
		
		//..........................................................
		//T-BOX Variables
		RectangularRegion varA = (RectangularRegion)solver.createVariable();		
		varA.setName("A");
		
		RectangularRegion varB = (RectangularRegion)solver.createVariable();
		varB.setName("B");
		
		RectangularRegion varC = (RectangularRegion)solver.createVariable();
		varC.setName("C");
				
		//..........................................................
		UnaryRectangleConstraint atA = new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.At, new Bounds(50,50), new Bounds(55,55), new Bounds(12,12), new Bounds(26,26));
		atA.setFrom(varA);
		atA.setTo(varA);
		allConstraints.add(atA);
		
		UnaryRectangleConstraint atB = new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.At, new Bounds(5,5), new Bounds(10,10), new Bounds(14,14), new Bounds(24,24));
		atB.setFrom(varB);
		atB.setTo(varB);
		allConstraints.add(atB);
		
//		UnaryRectangleConstraint2 atC = new UnaryRectangleConstraint2(UnaryRectangleConstraint2.Type.At, new Bounds(20,20), new Bounds(28,28), new Bounds(35,35), new Bounds(42,42));
//		atC.setFrom(varC);
//		atC.setTo(varC);
//		allConstraints.add(atC);
		
		RectangleConstraint varAooVarC = new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.Overlaps, AllenIntervalConstraint.Type.Overlaps.getDefaultBounds()), new AllenIntervalConstraint(AllenIntervalConstraint.Type.Overlaps, AllenIntervalConstraint.Type.Overlaps.getDefaultBounds()));
		varAooVarC.setFrom(varA);
		varAooVarC.setTo(varC);
		allConstraints.add(varAooVarC);
		
		Constraint[] allConstraintsArray = allConstraints.toArray(new Constraint[allConstraints.size()]);
		MetaCSPLogging.setLevel(Level.FINEST);
		if (!solver.addConstraints(allConstraintsArray)) { 
			System.out.println("Failed to add constraints!");
			System.exit(0);
		}
		
		ConstraintNetwork.draw(solver.getConstraintNetwork());
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		solver.removeConstraint(varAooVarC);

	}

}

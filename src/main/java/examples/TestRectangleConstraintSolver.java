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
package examples;

import spatial.rectangleAlgebra.QualitativeAllenIntervalConstraint;
import spatial.rectangleAlgebra.RectangleConstraint;
import spatial.rectangleAlgebra.RectangleConstraintSolver;
import spatial.rectangleAlgebra.RectangularRegion;
import spatial.rectangleAlgebra.TwoDimensionsAllenConstraint;
import framework.ConstraintNetwork;
import framework.Variable;

public class TestRectangleConstraintSolver {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		//Region r1 = 
		RectangleConstraintSolver solver = new RectangleConstraintSolver(); 
		Variable[] vars = solver.createVariables(3);

		RectangularRegion re0 = (RectangularRegion)vars[0];
		RectangularRegion re1 = (RectangularRegion)vars[1];
		RectangularRegion re2 = (RectangularRegion)vars[2];
		
		RectangleConstraint con0 = new RectangleConstraint(new TwoDimensionsAllenConstraint(QualitativeAllenIntervalConstraint.Type.Before, 
				QualitativeAllenIntervalConstraint.Type.Before), new TwoDimensionsAllenConstraint(QualitativeAllenIntervalConstraint.Type.After, 
						QualitativeAllenIntervalConstraint.Type.Before));
		con0.setFrom(re0);
		con0.setTo(re1);
		
		RectangleConstraint con1 = new RectangleConstraint(new TwoDimensionsAllenConstraint(QualitativeAllenIntervalConstraint.Type.Before,
				QualitativeAllenIntervalConstraint.Type.Before));
		con1.setFrom(re1);
		con1.setTo(re2);
		
		RectangleConstraint con2 = new RectangleConstraint(new TwoDimensionsAllenConstraint(QualitativeAllenIntervalConstraint.Type.OverlappedBy,
				QualitativeAllenIntervalConstraint.Type.Before));
		con2.setFrom(re0);
		con2.setTo(re2);		
		
		RectangleConstraint[] allConstraints = {con0, con1, con2};
		if (!solver.addConstraints(allConstraints)) { 
			System.out.println("Failed to add constraints!");
			System.exit(0);
		}
		
		ConstraintNetwork.draw(solver.getConstraintNetwork());
	}


}

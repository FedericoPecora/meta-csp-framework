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

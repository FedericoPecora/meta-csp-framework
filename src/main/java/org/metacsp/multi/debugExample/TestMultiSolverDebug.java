package org.metacsp.multi.debugExample;

import org.metacsp.framework.Variable;
import org.metacsp.multi.spatial.DE9IM.DE9IMRelation;
import org.metacsp.multi.spatial.DE9IM.LineStringDomain;

import com.vividsolutions.jts.geom.Coordinate;

public class TestMultiSolverDebug {
	
	public static void main(String[] args) {
		MultiSolverDebug solver = new MultiSolverDebug(0, 1000);
		
		Variable[] vars = solver.createVariables(2, "track");
		
		MultiVarDebug t0 = (MultiVarDebug) vars[0];
		MultiVarDebug t1 = (MultiVarDebug) vars[1];
		
		t0.setDomain(new LineStringDomain(t0, new Coordinate[] {
				new Coordinate(-10,-10),
				new Coordinate(0,0),
				new Coordinate(20,20),
				new Coordinate(30,40)
		}));
		
		t1.setDomain(new LineStringDomain(t1, new Coordinate[] {
				new Coordinate(-1,-1),
				new Coordinate(1,1)
		}));
		
		// It works if we put the relation directly between the GeometricShapeVariables:
		DE9IMRelation relation1 = new DE9IMRelation(DE9IMRelation.Type.Intersects);
		relation1.setFrom(t0.getGeometricShapeVariable());
		relation1.setTo(t1.getGeometricShapeVariable());
		System.out.println("Added " + relation1 + "? " + solver.getSpatialSolver().addConstraints(relation1));
		
		// But if we use the MultiVarDebug direkty, it does not work. (Bug in MultiConstraintSolver???)
		// This problem always occurs if the number of constraints that the MultiConstraintSolver can process and the number of internal variables is not equal.
		// In MultiConstraintSolver line 194 and 198 we first search for index of the matching constraint in constraintTypes and then use that index to retrieve the internal variable. But these might be different!
		DE9IMRelation relation2 = new DE9IMRelation(DE9IMRelation.Type.Overlaps);
		relation2.setFrom(t0);
		relation2.setTo(t1);
		System.out.println("Added " + relation2 + "? " + solver.addConstraints(relation2));
		
	}

}

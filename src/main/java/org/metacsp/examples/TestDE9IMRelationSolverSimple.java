package org.metacsp.examples;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.multi.spatial.DE9IM.DE9IMRelation;
import org.metacsp.multi.spatial.DE9IM.DE9IMRelationSolver;

public class TestDE9IMRelationSolverSimple {

	public static void main(String[] args) {

		DE9IMRelationSolver solver = new DE9IMRelationSolver();
		Variable[] vars = solver.createVariables(3);

		DE9IMRelation relation1 = new DE9IMRelation(DE9IMRelation.Type.Contains);
		relation1.setFrom(vars[0]);
		relation1.setTo(vars[1]);

		DE9IMRelation relation2 = new DE9IMRelation(DE9IMRelation.Type.Contains);
		relation2.setFrom(vars[1]);
		relation2.setTo(vars[2]);

		DE9IMRelation relation3 = new DE9IMRelation(DE9IMRelation.Type.Contains);
		relation3.setFrom(vars[2]);
		relation3.setTo(vars[0]);

		System.out.println(solver.addConstraints(relation1));
		
		ConstraintNetwork.draw(solver.getConstraintNetwork());
		
	}

}

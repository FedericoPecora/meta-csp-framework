package org.metacsp.examples;
import java.util.Vector;

import org.metacsp.framework.Variable;
import org.metacsp.spatial.geometry.GeometricConstraint;
import org.metacsp.spatial.geometry.GeometricConstraintSolver;
import org.metacsp.spatial.geometry.Polygon;
import org.metacsp.spatial.geometry.Vec2;
import org.metacsp.utility.UI.PolygonFrame;


public class CopyOfTestGeometricConstraintSolver2 {

	public static void main(String[] args) {

		GeometricConstraintSolver solver = new GeometricConstraintSolver();
		Variable[] vars = solver.createVariables(2, "Component");
		
		Polygon p1 = (Polygon)vars[0];
		Vector<Vec2> vecs1 = new Vector<Vec2>();
		vecs1.add(new Vec2(0,0));
		vecs1.add(new Vec2(1,0));
		vecs1.add(new Vec2(0,1));
		vecs1.add(new Vec2(0.6f,0.6f));
		p1.setDomain(vecs1.toArray(new Vec2[vecs1.size()]));
		p1.setMovable(false);

		Polygon p2 = (Polygon)vars[1];
		Vector<Vec2> vecs2 = new Vector<Vec2>();
		vecs2.add(new Vec2(0+2,0));
		vecs2.add(new Vec2(1+2,0));
		vecs2.add(new Vec2(0+2,1));
		vecs2.add(new Vec2(0.6f+2,0.6f+2));
		p2.setDomain(vecs2.toArray(new Vec2[vecs2.size()]));
		p2.setMovable(false);
		
		System.out.println(solver.getRelation(p1, p2));
				
//		GeometricConstraint inside = new GeometricConstraint(GeometricConstraint.Type.INSIDE);
//		inside.setFrom(vars[0]);
//		inside.setTo(vars[1]);
//		System.out.println("Added? " + solver.addConstraint(inside));

	}

}

package org.metacsp.examples;
import java.util.Vector;

import org.metacsp.framework.Variable;
import org.metacsp.spatial.geometry.GeometricConstraint;
import org.metacsp.spatial.geometry.GeometricConstraintSolver;
import org.metacsp.spatial.geometry.Polygon;
import org.metacsp.spatial.geometry.Vec2;
import org.metacsp.utility.UI.PolygonFrame;


public class TestGeometricConstraintSolver2 {

	public static void main(String[] args) {

		GeometricConstraintSolver solver = new GeometricConstraintSolver();
		Variable[] vars = solver.createVariables(2, "pol");
		
		Polygon p1 = (Polygon)vars[0];
		Vector<Vec2> vecs1 = new Vector<Vec2>();
		vecs1.add(new Vec2(2,2));
		vecs1.add(new Vec2(8,2));
		vecs1.add(new Vec2(8,8));
		vecs1.add(new Vec2(2,8));
		p1.setDomain(vecs1.toArray(new Vec2[vecs1.size()]));
		p1.setMovable(false);
		
		Polygon p2 = (Polygon)vars[1];		
		Vector<Vec2> vecs = new Vector<Vec2>();
		vecs.add(new Vec2(1,1));
		vecs.add(new Vec2(9,1));
		vecs.add(new Vec2(9,9));
		vecs.add(new Vec2(1,9));
		p2.setDomain(vecs.toArray(new Vec2[vecs.size()]));
		p2.setMovable(false);
		
		PolygonFrame pf = new PolygonFrame("Polygon Constraint Network", solver.getConstraintNetwork());
		
//		Polygon p3 = (Polygon)vars[1];		
//		Vector<Vec2> vecs2 = new Vector<Vec2>();
//		vecs2.add(new Vec2(180,190));
//		vecs2.add(new Vec2(100,50));
//		vecs2.add(new Vec2(240,138));
//		vecs2.add(new Vec2(190,225));
//		p3.setDomain(vecs2.toArray(new Vec2[vecs2.size()]));
//		p3.setMovable(true);
												
		try { Thread.sleep(4000); }
		catch (InterruptedException e) { e.printStackTrace(); }
		
		GeometricConstraint inside = new GeometricConstraint(GeometricConstraint.Type.INSIDE);
		inside.setFrom(vars[0]);
		inside.setTo(vars[1]);
		System.out.println("Added? " + solver.addConstraint(inside));

	}

}

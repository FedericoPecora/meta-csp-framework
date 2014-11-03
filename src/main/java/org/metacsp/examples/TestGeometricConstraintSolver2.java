package org.metacsp.examples;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
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
		Variable[] vars = solver.createVariables(3);
		
		Polygon p1 = (Polygon)vars[0];
		Vector<Vec2> vecs1 = new Vector<Vec2>();
		vecs1.add(new Vec2(100,87));
		vecs1.add(new Vec2(60,30));
		vecs1.add(new Vec2(220,60));
		vecs1.add(new Vec2(180,120));
		p1.setDomain(vecs1.toArray(new Vec2[vecs1.size()]));
		p1.setMovable(true);
		
		Polygon p2 = (Polygon)vars[1];		
		Vector<Vec2> vecs = new Vector<Vec2>();
		vecs.add(new Vec2(180,90));
		vecs.add(new Vec2(100,350));
		vecs.add(new Vec2(340,350));
		vecs.add(new Vec2(290,125));
		p2.setDomain(vecs.toArray(new Vec2[vecs.size()]));
		p2.setMovable(true);
		
		Polygon p3 = (Polygon)vars[2];		
		Vector<Vec2> vecs2 = new Vector<Vec2>();
		vecs2.add(new Vec2(180,190));
		vecs2.add(new Vec2(100,50));
		vecs2.add(new Vec2(240,138));
		vecs2.add(new Vec2(190,225));
		p3.setDomain(vecs2.toArray(new Vec2[vecs2.size()]));
		p3.setMovable(true);
										
		PolygonFrame pf = new PolygonFrame("Polygon Constraint Network", solver.getConstraintNetwork());
		
		try { Thread.sleep(2000); }
		catch (InterruptedException e) { e.printStackTrace(); }
		
		GeometricConstraint inside = new GeometricConstraint(GeometricConstraint.Type.INSIDE);
		inside.setFrom(vars[0]);
		inside.setTo(vars[1]);
		System.out.println("Added? " + solver.addConstraint(inside));
		pf.updatePolygonFrame();
		
		try { Thread.sleep(2000); }
		catch (InterruptedException e) { e.printStackTrace(); }
		
		//The following should either not be added or it should "drag" vars[0] with it!
		GeometricConstraint dc1 = new GeometricConstraint(GeometricConstraint.Type.DC);
		dc1.setFrom(vars[1]);
		dc1.setTo(vars[2]);
		System.out.println("Added? " + solver.addConstraint(dc1));
		pf.updatePolygonFrame();

	}

}

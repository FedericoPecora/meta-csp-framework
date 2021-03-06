package org.metacsp.examples;

import java.util.Arrays;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.multi.spatial.DE9IM.DE9IMRelation;
import org.metacsp.multi.spatial.DE9IM.DE9IMRelationSolver;
import org.metacsp.multi.spatial.DE9IM.GeometricShapeVariable;
import org.metacsp.multi.spatial.DE9IM.LineStringDomain;
import org.metacsp.multi.spatial.DE9IM.PointDomain;
import org.metacsp.multi.spatial.DE9IM.PolygonalDomain;
import org.metacsp.utility.UI.JTSDrawingPanel;

import com.vividsolutions.jts.geom.Coordinate;

public class TestDE9IMRelationSolver {

	public static void main(String[] args) {

		DE9IMRelationSolver solver = new DE9IMRelationSolver();
		Variable[] vars = solver.createVariables(4);

		Coordinate[] coord1 = new Coordinate[] {
				new Coordinate(0,0),
				new Coordinate(10,0),
				new Coordinate(10,10),
				new Coordinate(0,10)
		};
		GeometricShapeVariable g1 = (GeometricShapeVariable)vars[0];
		g1.setDomain(new PolygonalDomain(g1,coord1));

//		Coordinate[] coord2 = new Coordinate[] {
//			new Coordinate(8,2),
//			new Coordinate(6,15),
//			new Coordinate(-3,-3),
//			new Coordinate(-2,-5)
//		};
//		GeometricShapeVariable g2 = (GeometricShapeVariable)vars[1];
//		g2.setDomain(new PolygonalDomain(g2,coord2));


		Coordinate[] coord2 = new Coordinate[] {
			new Coordinate(0,0),
			new Coordinate(2,0),
			new Coordinate(2,2),
			new Coordinate(0,2)
		};
		GeometricShapeVariable g2 = (GeometricShapeVariable)vars[1];
		g2.setDomain(new PolygonalDomain(g2,coord2));

//		Coordinate[] coord2 = new Coordinate[] {
//			new Coordinate(1,1),
//			new Coordinate(2,1),
//			new Coordinate(2,2),
//			new Coordinate(1,2)
//		};
//		GeometricShapeVariable g2 = (GeometricShapeVariable)vars[1];
//		g2.setDomain(new PolygonalDomain(g2,coord2));

//		Coordinate[] coord3 = new Coordinate[] {
//				new Coordinate(-40,0),
//				new Coordinate(-40,40),
//				new Coordinate(-80,40),
//				new Coordinate(-80,0)
//		};
//		GeometricShapeVariable g3 = (GeometricShapeVariable)vars[2];
//		g3.setDomain(new PolygonalDomain(g3,coord3));

//		Coordinate[] coord3 = new Coordinate[] {
//				new Coordinate(-40,0),
//				new Coordinate(0,0),
//				new Coordinate(0,40),
//				new Coordinate(-40,40)
//		};
//		GeometricShapeVariable g3 = (GeometricShapeVariable)vars[2];
//		g3.setDomain(new PolygonalDomain(g3,coord3));

		Coordinate[] coord3 = new Coordinate[] {
				new Coordinate(-40,-40),
				new Coordinate(0,0),
				new Coordinate(10,5),
				new Coordinate(11,200)
		};
		GeometricShapeVariable g3 = (GeometricShapeVariable)vars[2];
		g3.setDomain(new LineStringDomain(g3,coord3));

		GeometricShapeVariable g4 = (GeometricShapeVariable)vars[3];
		g4.setDomain(new PointDomain(g4,new Coordinate(-2,-2)));
		
		Constraint[] implicitRelations = solver.getAllImplicitRelations();
		System.out.println("All implicit relations:\n" + Arrays.toString(implicitRelations));

		Constraint[] rcc8ImplicitRelations = solver.getAllImplicitRCC8Relations();
		System.out.println("All implicit RCC8 relations:\n" + Arrays.toString(rcc8ImplicitRelations));

		DE9IMRelation relation = new DE9IMRelation(DE9IMRelation.Type.Disjoint);
		relation.setFrom(g1);
		relation.setTo(g2);
		System.out.println(solver.addConstraints(relation));
		
		ConstraintNetwork.draw(solver.getConstraintNetwork());
		
		for (Variable var : vars) {
			GeometricShapeVariable gsv = (GeometricShapeVariable)var;
			System.out.println(var + ": " + gsv.getShapeType());
		}
		
		JTSDrawingPanel panel = JTSDrawingPanel.makeEmpty("Spatial variables");
		panel.addGeometry(g1.toString(), ((PolygonalDomain)g1.getDomain()).getGeometry());
		panel.addGeometry(g2.toString(), ((PolygonalDomain)g2.getDomain()).getGeometry());
		panel.addGeometry(g3.toString(), ((LineStringDomain)g3.getDomain()).getGeometry());
//		panel.addGeometry(g4.toString(), ((PointDomain)g4.getDomain()).getGeometry());

	}

}

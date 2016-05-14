package org.metacsp.examples.multi;
import java.util.Arrays;

import org.metacsp.framework.Variable;
import org.metacsp.multi.spatial.DE9IM.DE9IMRelation;
import org.metacsp.multi.spatial.DE9IM.DE9IMRelationSolver;
import org.metacsp.multi.spatial.DE9IM.LineStringDomain;
import org.metacsp.multi.spatial.DE9IM.PolygonalDomain;
import org.metacsp.multi.spatioTemporal.SpatioTemporalVariable;
import org.metacsp.multi.spatioTemporal.SpatioTemporalVariableSolver;
import org.metacsp.utility.UI.JTSDrawingPanel;

import com.vividsolutions.jts.geom.Coordinate;

public class TestSpatioTemporalVariableSolver {

	public static void main(String[] args) {
		
		SpatioTemporalVariableSolver solver = new SpatioTemporalVariableSolver(0, 1000);
		Variable[] vars = solver.createVariables(3);
		SpatioTemporalVariable var0 = (SpatioTemporalVariable)vars[0];
		SpatioTemporalVariable var1 = (SpatioTemporalVariable)vars[1];

		//0
		var0.setDomain(new PolygonalDomain(var0,new Coordinate[] {
				new Coordinate(0,0),
				new Coordinate(10,0),
				new Coordinate(10,10),
				new Coordinate(0,10)
		}));
		
		//1
		var1.setDomain(new LineStringDomain(var1,new Coordinate[] {
				new Coordinate(-10,-10),
				new Coordinate(0,0),
				new Coordinate(20,20),
				new Coordinate(30,40)
		}));


		DE9IMRelation relation = new DE9IMRelation(DE9IMRelation.Type.Overlaps);
		relation.setFrom(var0);
		relation.setTo(var1);
		
		System.out.println("Added " + relation + "? " + solver.addConstraints(relation));
		
		System.out.println(Arrays.toString(((DE9IMRelationSolver)solver.getConstraintSolvers()[1]).getAllImplicitRelations()));
		
		JTSDrawingPanel.drawVariables("Geometries",var0.getSpatialVariable(), var1.getSpatialVariable());

	}
}

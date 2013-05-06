package sandbox.spatial.rectangleAlgebra2;

import multi.activity.ActivityNetwork;
import multi.activity.ActivityNetworkSolver;
import framework.Constraint;
import framework.ConstraintNetwork;
import framework.ConstraintSolver;
import framework.Variable;

public class SpatialFluentNetwork extends ConstraintNetwork {

	public SpatialFluentNetwork(ConstraintSolver sol) {
		super(sol);
		// TODO Auto-generated constructor stub
	}
	
	
	public SpatialFluentNetwork clone() {
		SpatialFluentNetwork c = new SpatialFluentNetwork(super.solver);
		
		for ( Variable v : super.g.getVertices() ) {
			c.g.addVertex(v);
		}
		for ( Constraint e : super.g.getEdges() ) {
			c.g.addEdge(e, g.getEndpoints(e));
		}
		return c;
	}
}




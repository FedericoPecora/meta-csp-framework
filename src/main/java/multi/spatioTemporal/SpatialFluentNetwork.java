package multi.spatioTemporal;

import framework.Constraint;
import framework.ConstraintNetwork;
import framework.ConstraintSolver;
import framework.Variable;

public class SpatialFluentNetwork extends ConstraintNetwork {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5964280041479603125L;


	public SpatialFluentNetwork(ConstraintSolver sol) {
		super(sol);
		// TODO Auto-generated constructor stub
	}
	
	
//	public SpatialFluentNetwork clone() {
//		SpatialFluentNetwork c = new SpatialFluentNetwork(super.solver);
//		
//		for ( Variable v : super.g.getVertices() ) {
//			c.g.addVertex(v);
//		}
//		for ( Constraint e : super.g.getEdges() ) {
//			c.g.addEdge(e, g.getEndpoints(e));
//		}
//		return c;
//	}
	
}




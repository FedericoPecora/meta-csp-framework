package multi.allenInterval;

import multi.activity.ActivityNetwork;
import framework.Constraint;
import framework.ConstraintNetwork;
import framework.ConstraintSolver;
import framework.Variable;

public class AllenIntervalNetwork extends ConstraintNetwork {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4419257353551196430L;

	public AllenIntervalNetwork(ConstraintSolver sol) {
		super(sol);
		// TODO Auto-generated constructor stub
	}

	public AllenIntervalNetwork clone() {
		AllenIntervalNetwork c = new AllenIntervalNetwork(this.solver);
		
		for ( Variable v : super.g.getVertices() ) {
			c.g.addVertex(v);
		}
		for ( Constraint e : super.g.getEdges() ) {
			c.g.addEdge(e, super.g.getEndpoints(e));
		}
		return c;
	}
}

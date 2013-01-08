package time;

import java.util.HashMap;

import multi.allenInterval.AllenIntervalNetwork;
import framework.Constraint;
import framework.ConstraintNetwork;
import framework.ConstraintSolver;
import framework.Variable;

public class SimpleTemporalNetwork extends ConstraintNetwork {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6006895701929725047L;

	public SimpleTemporalNetwork(ConstraintSolver sol) {
		super(sol);
		// TODO Auto-generated constructor stub
	}
	
	public long getOrigin() {
		return ((APSPSolver)this.solver).getO();
	}
	
	public long getHorizon() {
		return ((APSPSolver)this.solver).getH();
	}
	
	public SimpleTemporalNetwork clone() {
		SimpleTemporalNetwork c = new SimpleTemporalNetwork(this.solver);
		
//		HashMap<Variable,TimePoint> tpMap = new HashMap<Variable, TimePoint>();
		
		for ( Variable v : g.getVertices() ) {
			TimePoint clone = ((TimePoint)v);
//			tpMap.put(v,clone);
//			c.g.addVertex(clone);
		}
		for ( Constraint e : super.g.getEdges() ) {
			c.g.addEdge(((SimpleDistanceConstraint)e), g.getEndpoints(e).getFirst(), g.getEndpoints(e).getSecond());
		}
		return c;

	}
}

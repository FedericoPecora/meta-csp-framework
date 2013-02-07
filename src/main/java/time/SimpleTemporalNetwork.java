/*******************************************************************************
 * Copyright (c) 2010-2013 Federico Pecora <federico.pecora@oru.se>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
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

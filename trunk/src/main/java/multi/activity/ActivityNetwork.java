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
package multi.activity;

import edu.uci.ics.jung.graph.ObservableGraph;
import framework.Constraint;
import framework.ConstraintNetwork;
import framework.ConstraintSolver;
import framework.Variable;

public class ActivityNetwork extends ConstraintNetwork {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1628887638592525043L;

	public ActivityNetwork(ConstraintSolver sol) {
		super(sol);
		// TODO Auto-generated constructor stub
	}
		
	public long getOrigin() {
		return ((ActivityNetworkSolver)this.solver).getOrigin();
	}
	
	public long getHorizon() {
		return ((ActivityNetworkSolver)this.solver).getHorizon();
	}
	
	public ActivityNetwork clone() {
		ActivityNetwork c = new ActivityNetwork(super.solver);
		
		for ( Variable v : super.g.getVertices() ) {
			c.g.addVertex(v);
		}
		for ( Constraint e : super.g.getEdges() ) {
			c.g.addEdge(e, g.getEndpoints(e));
		}
		return c;
	}
}

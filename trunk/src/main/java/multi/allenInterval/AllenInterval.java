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
package multi.allenInterval;

import time.Bounds;
import time.TimePoint;
import framework.Constraint;
import framework.ConstraintSolver;
import framework.Domain;
import framework.Variable;
import framework.multi.MultiVariable;

public class AllenInterval extends MultiVariable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4302592731389782557L;
	private String name = "";
	
//	public AllenInterval(ConstraintSolver cs, int id, ConstraintSolver[] internalSolvers) {
//		super(cs, id, internalSolvers);
//		// TODO Auto-generated constructor stub
//	}
	
	public AllenInterval(ConstraintSolver cs, int id, ConstraintSolver[] internalSolvers, Variable[] internalVars) {
		super(cs,id,internalSolvers,internalVars);
	}
	
//	@Override
//	protected Variable[] createInternalVariables() {
//		Variable[] tps = internalSolvers[0].createVariables(2);
//		return tps;
//	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	protected Constraint[] createInternalConstraints(Variable[] variables) {
		AllenIntervalConstraint dur = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, AllenIntervalConstraint.Type.Duration.getDefaultBounds());
		dur.setFrom(this);
		dur.setTo(this);
		dur.setAutoRemovable(true);
		return new Constraint[] {dur};
	}

	@Override
	public void setDomain(Domain d) {
		// TODO Auto-generated method stub
	}
	
	public TimePoint getStart() {
		return (TimePoint)this.variables[0];
	}

	public TimePoint getEnd() {
		return (TimePoint)this.variables[1];
	}
	
	public void setStart( TimePoint s ) {
		this.variables[0] = s;
	}

	public void setEnd( TimePoint e ) {
		this.variables[1] = e;
	}
	
	
	public long getEST() {
		return (Long)this.getStart().getDomain().chooseValue("ET");
	}

	public long getLST() {
		return (Long)this.getStart().getDomain().chooseValue("LT");
	}
	
	public long getEET() {
		return (Long)this.getEnd().getDomain().chooseValue("ET");
	}

	public long getLET() {
		return (Long)this.getEnd().getDomain().chooseValue("LT");
	}

	public Bounds getDuration() {
		long minDur = (Long)this.getEnd().getDomain().chooseValue("ET")-(Long)this.getStart().getDomain().chooseValue("LT");
		long maxDur = (Long)this.getEnd().getDomain().chooseValue("LT")-(Long)this.getStart().getDomain().chooseValue("ET");
		return new Bounds(minDur,maxDur);
	}

	@Override
	public String toString() {
		String s="";
		if(name == ""){
			s+=this.getClass().getSimpleName() + " " + this.id +" "+ "(I-TP: ";
			for(Variable x: this.variables){
				s+= x.getID()+" ";
			}
			s+=") "+ this.getDomain();
		}
		else
			s+=this.name + " " + this.id + " " + this.getDomain();
		return s;

	}

	@Override
	public int compareTo(Variable arg0) {
		return this.getID() - arg0.getID();
	}

}

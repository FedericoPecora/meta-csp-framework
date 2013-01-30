package multi.allenInterval;

import time.APSPSolver;
import time.Bounds;
import time.SimpleDistanceConstraint;
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
	
	AllenInterval(ConstraintSolver cs, int id, ConstraintSolver[] internalSolvers) {
		super(cs, id, internalSolvers);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected Variable[] createInternalVariables() {
		Variable[] tps = internalSolvers[0].createVariables(2);
		return tps;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	protected Constraint[] createInternalConstraints(Variable[] variables) {
//		TimePoint ts = (TimePoint)variables[0];
//		TimePoint te = (TimePoint)variables[1];
//		SimpleDistanceConstraint sdc = new SimpleDistanceConstraint();
//		sdc.setFrom(ts);
//		sdc.setTo(te);
//		sdc.setMinimum(0);
//		sdc.setMaximum(APSPSolver.INF);
//		Constraint[] cons = new Constraint[1];
//		cons[0] = sdc;
//		return cons;	
		return null;
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
//		s="\n<KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK>\n";

		if(name == ""){
			s+=this.getClass().getSimpleName() + " " + this.id +" "+ "(I-TP: ";
			for(Variable x: this.variables){
				s+= x.getID()+" ";
			}
			s+=") "+ this.getDomain();
		}
		else
			s+=this.name + " " + this.id + " " + this.getDomain();
////		System.out.println("\n\tXXXXX");
//		for(Variable v: this.variables){
//			s+=("\n"+v);
//		}
//		s+="\n";
////		s+="\n<ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ>\n";
		return s;

	}

	@Override
	public int compareTo(Variable arg0) {
		return this.getID() - arg0.getID();
	}

}
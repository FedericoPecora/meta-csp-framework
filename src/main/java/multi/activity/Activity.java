package multi.activity;

import multi.allenInterval.AllenInterval;
import symbols.SymbolicVariable;
import framework.Constraint;
import framework.ConstraintSolver;
import framework.Domain;
import framework.Variable;
import framework.multi.MultiVariable;

public class Activity extends MultiVariable {
	
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 4709760631961797060L;
	private String[] symbols;
	private transient SymbolicVariable symbolicVariable;

	private AllenInterval temporalVariable;
	
	
	protected Activity(ConstraintSolver cs, int id, ConstraintSolver[] internalSolvers) {
		super(cs, id, internalSolvers);
	}
	
	public void setSymbolicDomain(String... symbols) {
		this.symbols = symbols;
		symbolicVariable.setDomain(this.symbols);
	}

	@Override
	protected Constraint[] createInternalConstraints(Variable[] variables) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Variable[] createInternalVariables() {
		AllenInterval temporalVariable = (AllenInterval)internalSolvers[0].createVariable();
		SymbolicVariable symbolicVariable = (SymbolicVariable)internalSolvers[1].createVariable();
		this.symbolicVariable = symbolicVariable;
		this.temporalVariable = temporalVariable;
		return new Variable[]{temporalVariable,symbolicVariable};
	}

	@Override
	public void setDomain(Domain d) {
		// TODO Auto-generated method stub

	}

	@Override
	public String toString() {
		String ret="";
//		ret+="\n<===================================>\n";
		ret += this.getComponent() + "::<" + this.symbolicVariable.toString() + ">U<" + this.temporalVariable.toString() + ">";
		if (this.getMarking() != null) ret += "/" + this.getMarking()+"\n";
//		ret+=("\tLLLLLL");
//		for(Variable v: this.variables){
//			ret+="\n"+v.toString();
//		}
//		ret+=("\n\tMMMMM");
//		ret+="\n<_____________________________________>\n"; 
		return ret;
	}
	
	/**
	 * @return The {@link SymbolicVariable} representing the symbolic value of this {@link Activity}.
	 */
	public SymbolicVariable getSymbolicVariable() {
		return symbolicVariable;
	}

	/**
	 * @return The {@link AllenInterval} representing the temporal value of this {@link Activity}.
	 */
	public AllenInterval getTemporalVariable() {
		return temporalVariable;
	}

	@Override
	public int compareTo(Variable arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	
}

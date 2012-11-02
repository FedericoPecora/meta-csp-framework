package spatial.RCC;

import framework.ConstraintSolver;
import framework.Domain;
import framework.Variable;

public class Region extends Variable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2345630958881665335L;
	private Domain dom;


	
	protected Region(ConstraintSolver cs, int id) {
		super(cs, id);
		
	}

	@Override
	public void setDomain(Domain d) {
		this.dom = d;
	}
	

	
	@Override
	public String toString() {
		
		return this.getClass().getSimpleName() + " " + this.id + " " + this.getDomain();
	}

	@Override
	public int compareTo(Variable arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	

	@Override
	public Domain getDomain() {
		// TODO Auto-generated method stub
		return dom;
	}

	
}

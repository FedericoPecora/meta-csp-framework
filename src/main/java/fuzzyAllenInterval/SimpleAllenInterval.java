package fuzzyAllenInterval;

import multi.allenInterval.AllenInterval;
import multi.allenInterval.AllenIntervalNetworkSolver;
import framework.ConstraintSolver;
import framework.Domain;
import framework.Variable;
import framework.multi.MultiVariable;

/**
 * An implementation of the Allen interval variable.  This implementation is
 * different from {@link AllenInterval}, which is a {@link MultiVariable} representing
 * an interval as two timepoints.  This implementation is used for qualitative temporal
 * reasoning (e.g., with the {@link FuzzyAllenIntervalNetworkSolver}), whereas the {@link AllenInterval}
 * is used for quantitative temporal reasoning (e.g., by the {@link AllenIntervalNetworkSolver}).
 *   
 * @author Federico Pecora
 *
 */
public class SimpleAllenInterval extends Variable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6378743562708507485L;
	private Domain dom;
	
	SimpleAllenInterval(ConstraintSolver cs, int id) {
		super(cs, id);
		setDomain(new SimpleInterval(this));
		// TODO Auto-generated constructor stub
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

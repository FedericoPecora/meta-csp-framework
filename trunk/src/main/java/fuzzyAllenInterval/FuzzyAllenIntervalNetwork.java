package fuzzyAllenInterval;

import multi.fuzzyActivity.FuzzyActivity;
import framework.ConstraintNetwork;
import framework.ConstraintSolver;

/**
 * Class representing networks of {@link FuzzyAllenIntervalConstraint}s among
 * {@link FuzzyActivity} or {@link SimpleAllenInterval} variables.
 * 
 * @author Federico Pecora
 *
 */
public class FuzzyAllenIntervalNetwork extends ConstraintNetwork {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2472080157593086040L;

	public FuzzyAllenIntervalNetwork(ConstraintSolver sol) {
		super(sol);
		// TODO Auto-generated constructor stub
	}

}

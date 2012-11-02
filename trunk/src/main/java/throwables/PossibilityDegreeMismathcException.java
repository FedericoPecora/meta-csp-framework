package throwables;

import symbols.fuzzySymbols.FuzzySymbolicDomain;
import cern.colt.Arrays;

public class PossibilityDegreeMismathcException extends Exception {
	
	private static final long serialVersionUID = 4997721032469646382L;

	public PossibilityDegreeMismathcException(FuzzySymbolicDomain v, double[] vals) {
		super("Symbols  " + Arrays.toString(v.getSymbols()) + " do not match possibility degrees " + Arrays.toString(vals));
	}

}

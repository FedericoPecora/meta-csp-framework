package throwables;

import symbols.fuzzySymbols.FuzzySymbolicDomain;
import cern.colt.Arrays;

public class SymbolNotFoundException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5775910304153823149L;

	public SymbolNotFoundException(FuzzySymbolicDomain v, String val) {
		super("Symbol " + val + " not found in domain " + Arrays.toString(v.getSymbols()));
	}

}

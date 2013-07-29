package org.metacsp.throwables;

import org.metacsp.multi.symbols.SymbolicVariable;

public class NoSymbolsException extends Error {
	
	private static final long serialVersionUID = 7842928988562536373L;

	public NoSymbolsException(SymbolicVariable var) {
		super("The solver of " + var + " has no registered symbols it can reason upon - please provide vocabulary in call to solver constructor");
	}

}

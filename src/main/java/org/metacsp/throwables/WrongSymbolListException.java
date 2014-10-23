package org.metacsp.throwables;

public class WrongSymbolListException extends Error {

	private static final long serialVersionUID = -4615008994590108902L;

	public WrongSymbolListException(int lengthSeen, int lengthExpected) {
		super("Cannot impose unary value for " + lengthSeen + " symbols (expecting " + lengthExpected + " symbols)");
	}

}

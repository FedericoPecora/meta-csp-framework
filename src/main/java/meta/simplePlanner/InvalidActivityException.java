package meta.simplePlanner;


public class InvalidActivityException extends Error {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8002697960151422281L;

	public InvalidActivityException(String a) {
		super("Cannot state head of rule (" + a + ") as required activity");
	}
}

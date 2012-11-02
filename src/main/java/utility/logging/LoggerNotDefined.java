package utility.logging;

public class LoggerNotDefined extends Error {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5223298283015216319L;

	public LoggerNotDefined(Class<?> c) {
		super("Class " + c.getName() + " does not have a logger.");
	}
}

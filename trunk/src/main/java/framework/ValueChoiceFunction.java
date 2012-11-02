package framework;

/**
 * Basic abstract class for defining {@link ValueChoiceFunction}s of {@link Variable} {@link Domain}s.
 * @author Federico Pecora
 *
 */
public interface ValueChoiceFunction {
	
	/**
	 * Method to be implemented by the {@link Domain} developer to choose a value. 
	 * @param dom The domain from which to choose a value.
	 * @return A value chosen from the given {@link Domain} according to this {@link ValueChoiceFunction}.
	 */
	abstract Object getValue(Domain dom);
	
}

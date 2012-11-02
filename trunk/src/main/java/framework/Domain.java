package framework;

import java.util.HashMap;

import throwables.IllegalValueChoiceFunction;

/**
 * This class is used to represent domains of variables.  Its main capability is to provide functionality for choosing values
 * from a {@link Variable}'s domain.  This is done through {@link ValueChoiceFunction}s, which have to be registered with the
 * domain. 
 * @author Federico Pecora
 *
 */
public abstract class Domain implements Comparable<Object> {

	private static HashMap<String,ValueChoiceFunction> valueChoiceFunctions = new HashMap<String,ValueChoiceFunction>();

	protected Variable myVariable;
	
	protected String privateValueChoiceFunction;
	
	protected abstract void registerValueChoiceFunctions();
		
	//So that extending classes must invoke 2-arg constructor
	protected Domain() {}
	
	protected Domain(Variable v) { 
		this.myVariable = v;
		registerValueChoiceFunctions();
		privateValueChoiceFunction = null;
	}
	
	protected final void registerValueChoiceFunction(ValueChoiceFunction vcf, String name) {
		valueChoiceFunctions.put(name, vcf);
//		logger.finest("Registered value choice function " + name);
	}
	
	public Object chooseValue(String vcf) throws IllegalValueChoiceFunction {
		ValueChoiceFunction vcfunc = valueChoiceFunctions.get(vcf);
		if (vcfunc != null)
			return vcfunc.getValue(this);
		throw new IllegalValueChoiceFunction(vcf, this.getClass().getSimpleName());
	}
	
	/**
	 * Choose a value from this {@link Domain} according to the private {@link ValueChoiceFunction}.
	 * @return A value chosen according to the {@link Domain}'s private {@link ValueChoiceFunction}.
	 */
	public Object chooseValue() {
		if (privateValueChoiceFunction == null)
			return this.chooseValue((String)valueChoiceFunctions.keySet().toArray()[0]);
		return this.chooseValue(privateValueChoiceFunction);
	}
	
	/**
	 * Set this {@link Domain}'s private {@link ValueChoiceFunction}.
	 * @param vcf A {@link ValueChoiceFunction}.
	 */
	public void setPrivateValueChoiceFunction(String vcf) {
		this.privateValueChoiceFunction = vcf;
	}
	
	/**
	 * Get a string representaiton of this domain (must be implemented by the {@link Domain} developer.
	 */
	public abstract String toString();
	
	/**
	 * Get all the {@link ValueChoiceFunction}s associated with this {@link Domain}.
	 * @return The {@link ValueChoiceFunction}s associated with this {@link Domain}.
	 */
	public HashMap<String,ValueChoiceFunction> getValueChoiceFunctions() {
		return valueChoiceFunctions;
	}
}

/*******************************************************************************
 * Copyright (c) 2010-2013 Federico Pecora <federico.pecora@oru.se>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
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

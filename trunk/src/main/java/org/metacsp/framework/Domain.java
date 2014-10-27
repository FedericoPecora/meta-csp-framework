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
package org.metacsp.framework;

import java.io.Serializable;
import java.util.HashMap;

import org.metacsp.throwables.IllegalValueChoiceFunction;

/**
 * This class is used to represent domains of variables.  Its main capability is to provide functionality for choosing values
 * from a {@link Variable}'s domain.  This is done through {@link ValueChoiceFunction}s, which have to be registered with the
 * domain. 
 * @author Federico Pecora
 *
 */
public abstract class Domain implements Comparable<Object>, Serializable {

	private static final long serialVersionUID = -1526153338750435200L;

	private static HashMap<Class<?>,HashMap<String,ValueChoiceFunction>> valueChoiceFunctions = new HashMap<Class<?>, HashMap<String,ValueChoiceFunction>>();

	protected Variable myVariable;
	
	protected String defaultValueChoiceFunction;
	
	//So that extending classes must invoke 2-arg constructor
	@SuppressWarnings("unused")
	private Domain() {}
	
	protected Domain(Variable v) {
		this.myVariable = v;
		//registerValueChoiceFunctions();
		defaultValueChoiceFunction = null;
	}
	
	public static void removeValueChoiceFunctions(Class<?> specificDomain) {
		valueChoiceFunctions.put(specificDomain, null);
	}
	public static void registerValueChoiceFunction(Class<?> specificDomain, ValueChoiceFunction vcf, String name) {
		HashMap<String,ValueChoiceFunction> oneClassVcfs = valueChoiceFunctions.get(specificDomain);
		if (oneClassVcfs == null) {
			oneClassVcfs = new HashMap<String, ValueChoiceFunction>();
			valueChoiceFunctions.put(specificDomain, oneClassVcfs);
		}
		oneClassVcfs.put(name, vcf);
		//MetaCSPLogging.getLogger(Domain.class).finest("Registered value choice function " + name);
	}
	
	/**
	 * Choose a value from this {@link Domain} according to the given {@link ValueChoiceFunction} identifier.
	 * @param vcf A {@link ValueChoiceFunction} function identifier.
	 * @return A value chosen according to the given {@link ValueChoiceFunction}.
	 * @throws IllegalValueChoiceFunction
	 */
	public Object chooseValue(String vcf) throws IllegalValueChoiceFunction {
		if (!valueChoiceFunctions.containsKey(this.getClass())) throw new Error ("No value choice function defined for domains fo type " + this.getClass().getSimpleName());
		HashMap<String,ValueChoiceFunction> vcfs = valueChoiceFunctions.get(this.getClass());
		if (vcfs == null) throw new Error ("No value choice function defined for domains of type " + this.getClass().getSimpleName()); 
		ValueChoiceFunction vcfunc = vcfs.get(vcf);
		if (vcfunc == null) throw new IllegalValueChoiceFunction(vcf, this.getClass().getSimpleName());
		return vcfunc.getValue(this);
	}

	/**
	 * Choose a value from this {@link Domain} according to the default {@link ValueChoiceFunction}
	 * (or the {@link ValueChoiceFunction} which was first registered if no default is set).
	 * @return A value chosen according to the {@link Domain}'s default {@link ValueChoiceFunction}.
	 */
	public Object chooseValue() {
		if (!valueChoiceFunctions.containsKey(this.getClass())) throw new Error ("No value choice function defined for domains fo type " + this.getClass().getSimpleName());
		if (defaultValueChoiceFunction == null)
			return this.chooseValue((String)valueChoiceFunctions.keySet().toArray()[0]);
		return this.chooseValue(defaultValueChoiceFunction);
	}
	
	/**
	 * Set this {@link Domain}'s default {@link ValueChoiceFunction}.
	 * @param vcf A {@link ValueChoiceFunction} identifier.
	 */
	public void setDefaultValueChoiceFunction(String vcf) {
		this.defaultValueChoiceFunction = vcf;
	}
	
	/**
	 * Get a string representation of this domain (must be implemented by the {@link Domain} developer.
	 */
	public abstract String toString();
	
	/**
	 * Get all the {@link ValueChoiceFunction}s associated with this {@link Domain}.
	 * @return The {@link ValueChoiceFunction}s associated with this {@link Domain}.
	 */
	public HashMap<String,ValueChoiceFunction> getValueChoiceFunctions(Class<?> specifiDomain) {
		return valueChoiceFunctions.get(specifiDomain);
	}
	
	/**
	 * Get the {@link Variable} of which this object is the {@link Domain}.
	 * @return The {@link Variable} of which this object is the {@link Domain}.
	 */
	public Variable getVariable() { return myVariable; }
}

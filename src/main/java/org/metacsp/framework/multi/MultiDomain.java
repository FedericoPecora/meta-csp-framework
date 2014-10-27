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
package org.metacsp.framework.multi;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import org.metacsp.framework.Domain;
import org.metacsp.framework.Variable;

/**
 * A multi-domain is a domain composed of many elementary domains.  This is used to represent
 * the domains of {@link MultiVariable}s. 
 * 
 * @author Federico Pecora
 *
 */
public class MultiDomain extends Domain {

	private static final long serialVersionUID = 6363060554334754226L;
	protected Domain[] domains;
	
	/**
	 * All extensions of {@link MultiDomain} must call this constructor.
	 * @param v The {@link MultiVariable} associated to this {@link MultiDomain}.
	 * @param domains The elementary domains composing this {@link MultiDomain}.
	 */
	protected MultiDomain(MultiVariable v, Domain ...domains) {
		super(v);
		this.domains = domains;
	}

	//disallow use of value choice function for only one domain 
	/**
	 * This method return <code>null</code>, reflecting the fact that it does not make
	 * sence to choose one value from a {@link MultiDomain} (use chooseValues() instead).
	 * @return <code>null</code>
	 */
	public Object chooseValue(String vcf) {
		return null;
	}
	
	//
	/**
	 * Chooses values for all internal domains according to their own value choice functions. 
	 * @return A value for each internal domain.
	 */
	public HashMap<Variable,Object> chooseValues() {
		MultiVariable mv = (MultiVariable)myVariable;
		Variable[] internalVars = mv.getInternalVariables();
		HashMap<Variable,Object> values = new HashMap<Variable,Object>();
		for (Variable v: internalVars) {
			if (v instanceof MultiVariable) {
				MultiVariable nestedMv = (MultiVariable)v;
				MultiDomain nestedMd = nestedMv.getDomain();
				HashMap<Variable,Object> nested = nestedMd.chooseValues();
				for (Entry<Variable,Object> e : nested.entrySet()) {
					values.put(e.getKey(), e.getValue());
				}
			}
			else {
				values.put(v,v.getDomain().chooseValue());
			}
		}
		return values;
	}
	
	@Override
	public String toString() {
		return Arrays.toString(domains);
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

//	@Override
//	protected void registerValueChoiceFunctions() {
//		// TODO Auto-generated method stub
//	}

}

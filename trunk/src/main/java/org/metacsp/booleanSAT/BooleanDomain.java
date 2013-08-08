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

package org.metacsp.booleanSAT;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Domain;
import org.metacsp.framework.ValueChoiceFunction;

/**
 * Represents domains for {@link BooleanVariable}s, namely the set {T,F}.  {@link ValueChoiceFunction}s
 * for this domain are dynamically managed by the {@link BooleanSatisfiabilitySolver}, which
 * guarantees the existence of a {@link ValueChoiceFunction} named "modelX" for each model X
 * in {0 ... maxModels-1} of the current {@link ConstraintNetwork}.  Note that the {@link ConstraintNetwork}
 * is never left in an inconsistent state, therefore the {@link ValueChoiceFunction}
 * called "model0" is always defined.  
 * 
 * @author Federico Pecora
 *
 */
public class BooleanDomain extends Domain {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1037950902891239792L;
	
	//domain is {TRUE, FALSE}
	private boolean[] domain;
	
	/**
	 * Create a {@link BooleanDomain} for a {@link BooleanVariable}.  The domain is
	 * set to {T,F}.
	 * @param v The variable of which this is a domain.
	 */
	public BooleanDomain(BooleanVariable v) {
		super(v);
		this.domain = new boolean[] {true,true};
		this.setDefaultValueChoiceFunction("model0");
	}

	/**
	 * Create a {@link BooleanDomain} for a {@link BooleanVariable} given a
	 * specification of its allowed values.  The specification must be of size 2.
	 * @param v The variable of which this is a domain.
	 * @param values The initial allowed values for this domain.
	 */
	public BooleanDomain(BooleanVariable v, boolean... values) {
		super(v);
		if (values.length != 2) throw new Error("Invalid values for a Boolean domain (two values must be supplied)");
		this.domain = new boolean[2];
		for (int i = 0; i < values.length; i++)
			this.domain[i] = values[i];
		this.setDefaultValueChoiceFunction("model0");
	}
	
	/**
	 * Unmask the value T for this domain.
	 */
	public void allowTrue() {
		this.domain[0] = true;
	}

	/**
	 * Unmask the value F for this domain.
	 */
	public void allowFalse() {
		this.domain[1] = true;
	}
	
	/**
	 * Assess whether the domain contains the value T.
	 * @return <code>true</code> iff the domain contains the value T.
	 */
	public boolean canBeTrue() {
		return this.domain[0];
	}

	/**
	 * Assess whether the domain contains the value F.
	 * @return <code>true</code> iff the domain contains the value F.
	 */
	public boolean canBeFalse() {
		return this.domain[1];
	}

	@Override
	public int compareTo(Object o) {
		if (!(o instanceof BooleanDomain)) return 0;
		BooleanDomain that = (BooleanDomain)o;
		int counterThis = 0;
		int counterThat = 0;
		if (this.domain[0]) counterThis++;
		if (this.domain[1]) counterThis++;
		if (that.domain[0]) counterThat++;
		if (that.domain[1]) counterThat++;
		return counterThat - counterThis;
	}

	@Override
	public String toString() {
		String ret = "[";
		if (this.domain[0] && this.domain[1]) ret += "T,F";
		else if (this.domain[0] && !this.domain[1]) ret += "T";
		else if (!this.domain[0] && this.domain[1]) ret += "F";
		ret += "]";
		return ret;
	}

}

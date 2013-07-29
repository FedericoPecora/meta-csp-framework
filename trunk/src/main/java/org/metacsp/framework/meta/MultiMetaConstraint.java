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
package org.metacsp.framework.meta;

import java.util.Arrays;
import java.util.HashMap;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintOrderingH;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.VariableOrderingH;

public abstract class MultiMetaConstraint extends MetaConstraint {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7319855771259677256L;
	private MetaConstraint[] myMetaCons;
	private ConstraintOrderingH myConstraintOrderingH;
		
	public MultiMetaConstraint(VariableOrderingH varOH, ValueOrderingH valOH, ConstraintOrderingH consOH, MetaConstraint ... metacons) {
		super(varOH, valOH);
		myMetaCons = metacons;
	}

	public ConstraintOrderingH getConOrderingH() {
		return myConstraintOrderingH;
	}
	
	public ConstraintNetwork getMetaVariable() {
		HashMap<MetaConstraint,ConstraintNetwork> subMetaCons = new HashMap<MetaConstraint, ConstraintNetwork>();
		for (MetaConstraint mc : myMetaCons) {
			ConstraintNetwork newMv = null;
			if ((newMv = mc.getMetaVariable()) != null) {
				subMetaCons.put(mc, newMv);
			}
		}
		
		MetaConstraint[] mcsArray = subMetaCons.keySet().toArray(new MetaConstraint[subMetaCons.keySet().size()]);
		Arrays.sort(mcsArray,this.getConOrderingH());
		return subMetaCons.get(mcsArray[0]);				
	}


}

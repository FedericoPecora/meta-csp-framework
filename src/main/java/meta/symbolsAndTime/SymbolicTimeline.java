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
package meta.symbolsAndTime;

import multi.activity.Activity;
import multi.activity.ActivityNetworkSolver;
import multi.activity.Timeline;
import symbols.SymbolicDomain;
import framework.Variable;

public class SymbolicTimeline extends Timeline {

	private Object[] values = null;
	
	public SymbolicTimeline(ActivityNetworkSolver ans, String component) {
		super(ans, component);
		cacheValues();
		// TODO Auto-generated constructor stub
	}

	private void cacheValues() {
		SymbolicDomain[] ret = new SymbolicDomain[getPulses().length];
		for (int i = 0; i < getPulses().length-1; i++) {
			for (Variable var : getAn().getVariables(component)) {
				Activity act = (Activity)var;
				if (act.getTemporalVariable().getEST() <= getPulses()[i] && act.getTemporalVariable().getEET() >= getPulses()[i+1]) {
					SymbolicDomain dom = (SymbolicDomain)act.getSymbolicVariable().getDomain();
					if (ret[i] == null) ret[i] = dom;
					//else ret[i] = SymbolicDomain.intersection(ret[i], dom);
					else ret[i] = SymbolicDomain.union(ret[i], dom);
				}
			}
		}
		values = ret;		
	}
	
	@Override
	public Object[] getValues()  { return values; }

	@Override
	public boolean isUndetermined(Object o) {
		return (o == null);
	}

	@Override
	public boolean isCritical(Object o) {
		if (o instanceof SymbolicDomain) return (((SymbolicDomain)o).getSymbols().length == 1);
		return false;
	}

	@Override
	public boolean isInconsistent(Object o) {
		if (o instanceof SymbolicDomain) return (((SymbolicDomain)o).getSymbols().length == 0);
		return false;
	}

}
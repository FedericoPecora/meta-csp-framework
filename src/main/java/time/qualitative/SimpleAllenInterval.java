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
package time.qualitative;

import multi.allenInterval.AllenInterval;
import multi.allenInterval.AllenIntervalNetworkSolver;
import framework.ConstraintSolver;
import framework.Domain;
import framework.Variable;
import framework.multi.MultiVariable;
import fuzzyAllenInterval.FuzzyAllenIntervalNetworkSolver;

/**
 * An implementation of the Allen interval variable.  This implementation is
 * different from {@link AllenInterval}, which is a {@link MultiVariable} representing
 * an interval as two timepoints.  This implementation is used for qualitative temporal
 * reasoning (e.g., with the {@link FuzzyAllenIntervalNetworkSolver}), whereas the {@link AllenInterval}
 * is used for quantitative temporal reasoning (e.g., by the {@link AllenIntervalNetworkSolver}).
 *   
 * @author Federico Pecora
 *
 */
public class SimpleAllenInterval extends Variable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6378743562708507485L;
	private Domain dom;
	
	public SimpleAllenInterval(ConstraintSolver cs, int id) {
		super(cs, id);
		setDomain(new SimpleInterval(this));
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void setDomain(Domain d) {
		this.dom = d;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " " + this.id + " " + this.getDomain();
	}

	@Override
	public int compareTo(Variable arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Domain getDomain() {
		// TODO Auto-generated method stub
		return dom;
	}

}

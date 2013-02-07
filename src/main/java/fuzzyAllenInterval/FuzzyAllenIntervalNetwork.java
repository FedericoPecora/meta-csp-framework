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
package fuzzyAllenInterval;

import multi.fuzzyActivity.FuzzyActivity;
import framework.ConstraintNetwork;
import framework.ConstraintSolver;

/**
 * Class representing networks of {@link FuzzyAllenIntervalConstraint}s among
 * {@link FuzzyActivity} or {@link SimpleAllenInterval} variables.
 * 
 * @author Federico Pecora
 *
 */
public class FuzzyAllenIntervalNetwork extends ConstraintNetwork {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2472080157593086040L;

	public FuzzyAllenIntervalNetwork(ConstraintSolver sol) {
		super(sol);
		// TODO Auto-generated constructor stub
	}

}

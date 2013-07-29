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
package org.metacsp.meta.symbolsAndTime;

import org.metacsp.multi.activity.Activity;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.VariableOrderingH;

public class EarliestStartTimeVarOH extends VariableOrderingH {

	@Override
	public int compare(ConstraintNetwork arg0, ConstraintNetwork arg1) {
		// TODO Auto-generated method stub
		long time1 = ((Activity)arg0.getVariables()[0]).getTemporalVariable().getEST();
		long time2 = ((Activity)arg1.getVariables()[0]).getTemporalVariable().getEST();
		if (time1 > time2) return 1;
		else if (time1 < time2) return -1;
		return 0;
	}

	@Override
	public void collectData(ConstraintNetwork[] allMetaVariables) {
		// TODO Auto-generated method stub
		
	}

}

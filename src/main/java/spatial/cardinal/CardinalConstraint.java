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
package spatial.cardinal;

import java.util.Arrays;

import spatial.RCC.RCCConstraint.Type;

import framework.BinaryConstraint;
import framework.Constraint;

public class CardinalConstraint extends BinaryConstraint{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected Type[] types;
	public static enum Type {
		
		North, 
		West,
		South,
		East, 
		NorthEast,
		NorthWest,
		SouthEast,
		SouthWest,
		EQUAL,
		NO
	};
	
	//has to be completed
	public static Float[] CardinalRelationToMetricOrientation = {
		
		(float) -1.57, //North			
		(float) 0, //West
		(float) 1.57, //south
		(float) 3.14, //East
		(float) 0, //NorthEast
		(float) 0, //NorthWest
		(float) 0, //SouthEast
		(float) 0, //SouthWest
		(float) 0, //Equal
		(float) 0, //NO
	};

	
	@Override
	public String getEdgeLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEquivalent(Constraint c) {
		// TODO Auto-generated method stub
		return false;
	}

}

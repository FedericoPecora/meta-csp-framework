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
package spatial.rectangleAlgebra;

import multi.allenInterval.AllenIntervalConstraint;

/**
 * This class augment the rectangle Algebra class with Quantified knowledge namely Duration of interval and bounded Allen
 * @author iran
 *
 */
public class AugmentedRectangleConstraint extends RectangleConstraint{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4291140350801794890L;
	private AllenIntervalConstraint boundedAllenX;
	private AllenIntervalConstraint boundedAllenY;

	

	public AllenIntervalConstraint getBoundedConstraintX() {
		return boundedAllenX;
	}
	
	public AllenIntervalConstraint getBoundedConstraintY() {
		return boundedAllenY;
	}
	
	
	
	public AugmentedRectangleConstraint(TwoDimensionsAllenConstraint... c) {
		super(c);
	}
	
	public AugmentedRectangleConstraint(AllenIntervalConstraint cx, AllenIntervalConstraint cy) {
		this.boundedAllenX = cx;
		this.boundedAllenY = cy;
	}
	
	
	
	
	

}

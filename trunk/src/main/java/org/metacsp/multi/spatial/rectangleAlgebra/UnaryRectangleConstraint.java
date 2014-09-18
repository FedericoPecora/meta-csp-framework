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
package org.metacsp.multi.spatial.rectangleAlgebra;


import java.util.Arrays;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiBinaryConstraint;
import org.metacsp.framework.multi.MultiVariable;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.Bounds;

public class UnaryRectangleConstraint extends MultiBinaryConstraint {

	
	private static final long serialVersionUID = 304977081496019725L;
	private Type type;
	public static enum Type {Size, At};
	private Bounds[] bounds;
	
	public UnaryRectangleConstraint(Type t, Bounds ... bounds) {
		this.type = t;
		this.bounds = bounds;
	}

	@Override
	protected Constraint[] createInternalConstraints(Variable from, Variable to) {
		//Do something here!!
		if (this.type.equals(Type.Size)) {
			AllenIntervalConstraint durationX = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, bounds[0]);
			durationX.setFrom(((RectangularRegion)from).getInternalVariables()[0]);
			durationX.setTo(((RectangularRegion)from).getInternalVariables()[0]);
			AllenIntervalConstraint durationY = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, bounds[1]);
			durationY.setFrom(((RectangularRegion)from).getInternalVariables()[1]);
			durationY.setTo(((RectangularRegion)from).getInternalVariables()[1]);
			//xConstraint should not be processed by Y solver		
			durationX.skipSolver(((MultiVariable)from).getInternalConstraintSolvers()[1]);
			//yConstraint should not be processed by X solver
			durationY.skipSolver(((MultiVariable)from).getInternalConstraintSolvers()[0]);
			return new Constraint[] {durationX, durationY};
		}
		else if (this.type.equals(Type.At)) {
			AllenIntervalConstraint atX = new AllenIntervalConstraint(AllenIntervalConstraint.Type.At, bounds[0], bounds[1]);
			atX.setFrom(((RectangularRegion)from).getInternalVariables()[0]);
			atX.setTo(((RectangularRegion)from).getInternalVariables()[0]);
			AllenIntervalConstraint atY = new AllenIntervalConstraint(AllenIntervalConstraint.Type.At, bounds[2], bounds[3]);
			atY.setFrom(((RectangularRegion)from).getInternalVariables()[1]);
			atY.setTo(((RectangularRegion)from).getInternalVariables()[1]);
			//xConstraint should not be processed by Y solver		
			atX.skipSolver(((MultiVariable)from).getInternalConstraintSolvers()[1]);
			//yConstraint should not be processed by X solver
			atY.skipSolver(((MultiVariable)from).getInternalConstraintSolvers()[0]);
			return new Constraint[] {atX, atY};
		}
		return null;
	}

	@Override
	public Object clone() {
		return new UnaryRectangleConstraint(this.type, this.bounds);
	}

	@Override
	public String getEdgeLabel() {
		return this.type + Arrays.toString(this.bounds);
	}

	@Override
	public boolean isEquivalent(Constraint c) {
		if (!(c instanceof UnaryRectangleConstraint) || !(((UnaryRectangleConstraint)c).getType().equals(this.type))) return false;
		for (int i = 0; i < ((UnaryRectangleConstraint)c).bounds.length; i++) {
			for (int j = 0; j < this.bounds.length; j++) {
				if (((UnaryRectangleConstraint)c).bounds[i].equals(bounds[j])) continue;
				else if (i == ((UnaryRectangleConstraint)c).bounds.length-1) return false;
			}
		}
		return true;
	}
	
	public Type getType() { return type; }
	public Bounds[] getBounds(){
		return this.bounds;
	}
}

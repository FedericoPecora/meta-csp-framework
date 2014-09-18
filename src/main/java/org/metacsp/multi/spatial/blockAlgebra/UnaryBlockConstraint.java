package org.metacsp.multi.spatial.blockAlgebra;

import java.util.Arrays;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiBinaryConstraint;
import org.metacsp.framework.multi.MultiVariable;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.Bounds;

public class UnaryBlockConstraint extends MultiBinaryConstraint {

	
	private static final long serialVersionUID = 304977081496019725L;
	private Type type;
	public static enum Type {Size, At};
	private Bounds[] bounds;
	
	public UnaryBlockConstraint(Type t, Bounds ... bounds) {
		this.type = t;
		this.bounds = bounds;
	}

	@Override
	protected Constraint[] createInternalConstraints(Variable from, Variable to) {
		//Do something here!!
		if (this.type.equals(Type.Size)) {
			AllenIntervalConstraint durationX = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, bounds[0]);
			durationX.setFrom(((RectangularCuboidRegion)from).getInternalVariables()[0]);
			durationX.setTo(((RectangularCuboidRegion)from).getInternalVariables()[0]);
			AllenIntervalConstraint durationY = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, bounds[1]);
			durationY.setFrom(((RectangularCuboidRegion)from).getInternalVariables()[1]);
			durationY.setTo(((RectangularCuboidRegion)from).getInternalVariables()[1]);
			AllenIntervalConstraint durationZ = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, bounds[2]);
			durationZ.setFrom(((RectangularCuboidRegion)from).getInternalVariables()[2]);
			durationZ.setTo(((RectangularCuboidRegion)from).getInternalVariables()[2]);
			//xConstraint should not be processed by Y and Z solver		
			durationX.skipSolver(((MultiVariable)from).getInternalConstraintSolvers()[1], ((MultiVariable)from).getInternalConstraintSolvers()[2]);
			//yConstraint should not be processed by X and Z solver
			durationY.skipSolver(((MultiVariable)from).getInternalConstraintSolvers()[0], ((MultiVariable)from).getInternalConstraintSolvers()[2]);
			//zConstraint should not be processed by X and Y solver
			durationZ.skipSolver(((MultiVariable)from).getInternalConstraintSolvers()[0], ((MultiVariable)from).getInternalConstraintSolvers()[1]);
			return new Constraint[] {durationX, durationY, durationZ};
		}
		else if (this.type.equals(Type.At)) {
			AllenIntervalConstraint atX = new AllenIntervalConstraint(AllenIntervalConstraint.Type.At, bounds[0], bounds[1]);
			atX.setFrom(((RectangularCuboidRegion)from).getInternalVariables()[0]);
			atX.setTo(((RectangularCuboidRegion)from).getInternalVariables()[0]);
			AllenIntervalConstraint atY = new AllenIntervalConstraint(AllenIntervalConstraint.Type.At, bounds[2], bounds[3]);
			atY.setFrom(((RectangularCuboidRegion)from).getInternalVariables()[1]);
			atY.setTo(((RectangularCuboidRegion)from).getInternalVariables()[1]);
			AllenIntervalConstraint atZ = new AllenIntervalConstraint(AllenIntervalConstraint.Type.At, bounds[4], bounds[5]);
			atZ.setFrom(((RectangularCuboidRegion)from).getInternalVariables()[2]);
			atZ.setTo(((RectangularCuboidRegion)from).getInternalVariables()[2]);
			//xConstraint should not be processed by Y and Z solver		
			atX.skipSolver(((MultiVariable)from).getInternalConstraintSolvers()[1], ((MultiVariable)from).getInternalConstraintSolvers()[2]);
			//yConstraint should not be processed by X and Z solver
			atY.skipSolver(((MultiVariable)from).getInternalConstraintSolvers()[0], ((MultiVariable)from).getInternalConstraintSolvers()[2]);
			//zConstraint should not be processed by X and Y solver
			atZ.skipSolver(((MultiVariable)from).getInternalConstraintSolvers()[0], ((MultiVariable)from).getInternalConstraintSolvers()[1]);
			return new Constraint[] {atX, atY, atZ};
		}
		return null;
	}

	@Override
	public Object clone() {
		return new UnaryBlockConstraint(this.type, this.bounds);
	}

	@Override
	public String getEdgeLabel() {
		return this.type + Arrays.toString(this.bounds);
	}

	@Override
	public boolean isEquivalent(Constraint c) {
		if (!(c instanceof UnaryBlockConstraint) || !(((UnaryBlockConstraint)c).getType().equals(this.type))) return false;
		for (int i = 0; i < ((UnaryBlockConstraint)c).bounds.length; i++) {
			for (int j = 0; j < this.bounds.length; j++) {
				if (((UnaryBlockConstraint)c).bounds[i].equals(bounds[j])) continue;
				else if (i == ((UnaryBlockConstraint)c).bounds.length-1) return false;
			}
		}
		return true;
	}
	
	public Type getType() { return type; }
	public Bounds[] getBounds(){
		return this.bounds;
	}
}

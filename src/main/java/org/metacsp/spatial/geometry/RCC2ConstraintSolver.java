package org.metacsp.spatial.geometry;
import java.util.Arrays;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;


public class RCC2ConstraintSolver extends ConstraintSolver {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	
	public RCC2ConstraintSolver() {
		super(new Class[]{GeometricConstraint.class}, Polygon.class);
		this.setOptions(OPTIONS.AUTO_PROPAGATE);
	}

	@Override
	public boolean propagate() {
		Variable[] vars = this.getConstraintNetwork().getVariables();
		for (int k = 0; k < vars.length; k++) {
			for (int i = 0; i < vars.length; i++) {
				if (i != k) {
					for (int j = 0; j < vars.length; j++) {
						if (j != k && j != i) {
							GeometricConstraint r_ij = (GeometricConstraint)this.getConstraintNetwork().getConstraint(vars[i], vars[j]);
							if(r_ij == null) continue;
							GeometricConstraint r_ik = (GeometricConstraint)this.getConstraintNetwork().getConstraint(vars[i], vars[k]);
							if(r_ik == null) continue;
							GeometricConstraint r_kj = (GeometricConstraint)this.getConstraintNetwork().getConstraint(vars[k], vars[j]);
							if(r_kj == null) continue;
							//comp = R_ik * R_kj
							GeometricConstraint.Type[] comp = getComposition(r_ik.getType(), r_kj.getType());
							//inters = R_ij ^ comp
							if(!Arrays.asList(comp).contains(r_ij.getType())) return false;
						}
					}	
				}
			}				
		}
		return true;
	}
	

	private GeometricConstraint.Type[] getComposition(GeometricConstraint.Type o1, GeometricConstraint.Type o2){
		return GeometricConstraint.transitionTable[o1.ordinal()][o2.ordinal()];
	}

	@Override
	protected void removeConstraintsSub(Constraint[] c) {

	}

	@Override
	protected Variable[] createVariablesSub(int num) {
		Polygon[] ret = new Polygon[num];
		for (int i = 0; i < num; i++) ret[i] = new Polygon(this, IDs++);
			return ret;
	}

	@Override
	protected void removeVariablesSub(Variable[] v) {
		
	}

	@Override
	public void registerValueChoiceFunctions() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean addConstraintsSub(Constraint[] c) {
		// TODO Auto-generated method stub
		return true;
	}
	

}

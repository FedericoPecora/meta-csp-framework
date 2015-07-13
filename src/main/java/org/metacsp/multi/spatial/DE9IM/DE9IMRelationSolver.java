package org.metacsp.multi.spatial.DE9IM;

import java.util.ArrayList;
import java.util.HashSet;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;
import org.metacsp.multi.spatial.DE9IM.DE9IMRelation.Type;

public class DE9IMRelationSolver extends ConstraintSolver {

	protected DE9IMRelationSolver(Class<?>[] constraintTypes, Class<?> variableType) {
		super(constraintTypes, variableType);
	}

	public DE9IMRelationSolver() {
		super(new Class<?>[] {DE9IMRelation.class}, GeometricShapeVariable.class);
		this.setOptions(OPTIONS.DOMAINS_AUTO_INSTANTIATED, OPTIONS.AUTO_PROPAGATE);
	}
	
	private static final long serialVersionUID = 2872228080625654304L;

	public Constraint[] getAllImplicitRelations() {
		return this.getAllImplicitRelations(false);
	}
	
	public Constraint[] getAllImplicitRCC8Relations() {
		return this.getAllImplicitRelations(true);
	}

	private Constraint[] getAllImplicitRelations(boolean rcc8relations) {
		ArrayList<Constraint> cons = new ArrayList<Constraint>();
		Variable[] vars = this.getVariables();
		for (int i = 0; i < vars.length; i++) {
			GeometricShapeVariable g1 = (GeometricShapeVariable)vars[i];
			for (int j = 0; j < vars.length; j++) {
				if (i != j) {
					GeometricShapeVariable g2 = (GeometricShapeVariable)vars[j];
					Type[] rels = null;
					if (!rcc8relations) rels = DE9IMRelation.getRelations(g1, g2);
					else rels = DE9IMRelation.getRCC8Relations(g1, g2);
					DE9IMRelation con = new DE9IMRelation(rels);
					con.setFrom(g1);
					con.setTo(g2);
					cons.add(con);
				}
			}			
		}
		return cons.toArray(new Constraint[cons.size()]);
	}

	

	
	@Override
	public boolean propagate() {
		Variable[] vars = this.getVariables();
		for (int i = 0; i < vars.length; i++) {
			GeometricShapeVariable g1 = (GeometricShapeVariable)vars[i];
			for (int j = 0; j < vars.length; j++) {
				if (i != j) {
					GeometricShapeVariable g2 = (GeometricShapeVariable)vars[j];
					//Are explicit (given, RCC8) relations compatible with implicit ones?
					Type[] implicitRelsA = DE9IMRelation.getRCC8Relations(g1, g2);
					HashSet<Type> implicitRels = new HashSet<Type>();
					for (Type t : implicitRelsA) implicitRels.add(t);
					Constraint[] cons = this.getConstraints(g1, g2);
					for (Constraint c : cons) {
						for (Type t : ((DE9IMRelation)c).getTypes()) {
							if (!implicitRels.contains(t)) return false;
						}
					}
					//... yes!
				}
			}			
		}
		return true;
	}

	@Override
	protected boolean addConstraintsSub(Constraint[] c) {
		return true;
	}

	@Override
	protected void removeConstraintsSub(Constraint[] c) {
		// TODO Auto-generated method stub
	}

	@Override
	protected Variable[] createVariablesSub(int num) {
		Variable[] ret = new Variable[num];
		for (int i = 0; i < num; i++) {
			ret[i] = new GeometricShapeVariable(this, IDs++);
		}
		return ret;
	}

	@Override
	protected void removeVariablesSub(Variable[] v) {
		// TODO Auto-generated method stub
	}

	@Override
	public void registerValueChoiceFunctions() {
		// TODO Auto-generated method stub
	}

}

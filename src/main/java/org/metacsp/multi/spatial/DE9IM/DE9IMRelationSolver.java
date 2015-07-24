package org.metacsp.multi.spatial.DE9IM;

import java.util.ArrayList;
import java.util.HashSet;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;
import org.metacsp.multi.spatial.DE9IM.DE9IMRelation.Type;

/**
 * A solver for constraint networks of {@link DE9IMRelation}s. This solver handles variables of type {@link GeometricShapeVariable}, which represent
 * points, line strings, or polygons. The latter are not necessarily convex.
 * 
 * @author Federico Pecora
 *
 */
public class DE9IMRelationSolver extends ConstraintSolver {

	protected DE9IMRelationSolver(Class<?>[] constraintTypes, Class<?> variableType) {
		super(constraintTypes, variableType);
	}

	/**
	 * Creates a new solver for {@link DE9IMRelation}s.
	 */
	public DE9IMRelationSolver() {
		super(new Class<?>[] {DE9IMRelation.class}, GeometricShapeVariable.class);
		this.setOptions(OPTIONS.DOMAINS_AUTO_INSTANTIATED, OPTIONS.AUTO_PROPAGATE);
	}
	
	private static final long serialVersionUID = 2872228080625654304L;

	/**
	 * Get all the implicit {@link DE9IMRelation}s that exist among the variables in this solver's {@link ConstraintNetwork}.
	 * @return All the implicit {@link DE9IMRelation}s that exist among the variables in this solver's {@link ConstraintNetwork}.
	 */
	public Constraint[] getAllImplicitRelations() {
		return this.getAllImplicitRelations(false);
	}

	/**
	 * Get all the implicit {@link DE9IMRelation}s that exist among the variables in this solver's {@link ConstraintNetwork}. These
	 * are limited to the eight Jointly Exclusive, Pairwise Disjoint relations<br>
	 * <lu>
	 * <li>Contains</li>
	 * <li>Within</li>
	 * <li>Covers</li>
	 * <li>CoveredBy</li>
	 * <li>Disjoint</li>
	 * <li>Overlaps</li>
	 * <li>Touches</li>
	 * <li>Equals</li>
	 * </lu>
	 * These eight relations are equivalent to te basic RCC8 relations (Cohn et al., 1997).
	 * @return All the implicit {@link DE9IMRelation}s that are also RCC8 relations existing among the variables in this solver's {@link ConstraintNetwork}.
	 */
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
		return propagateFull();
	}
	
	private boolean propagateFull() {
		Variable[] vars = this.getVariables();
		for (int i = 0; i < vars.length; i++) {
			GeometricShapeVariable g1 = (GeometricShapeVariable)vars[i];
			for (int j = 0; j < vars.length; j++) {
				if (i != j) {
					GeometricShapeVariable g2 = (GeometricShapeVariable)vars[j];
					//Are explicit (given) relations compatible with implicit ones?
					Type[] implicitRelsA = DE9IMRelation.getRelations(g1, g2);
					HashSet<Type> implicitRels = new HashSet<Type>();
					for (Type t : implicitRelsA) implicitRels.add(t);
					Constraint[] cons = this.getConstraints(g1, g2);
					for (Constraint c : cons) {
						for (Type t : ((DE9IMRelation)c).getTypes()) {
							//System.out.println(implicitRels + " contains " + t + "?");
							if (!implicitRels.contains(t)) {
								//System.out.println("... no!");
								return false;
							}
						}
					}
					//... yes!
				}
			}			
		}
		return true;
	}

	private boolean propagateRCC8() {
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
							if (!DE9IMRelation.isRCC8Relation(t)) {
								//System.out.println(t + " is not RCC8, skipping...");
								continue;
							}
							//System.out.println(implicitRels + " contains " + t + "?");
							if (!implicitRels.contains(t)) {
								//System.out.println("... no!");
								return false;
							}
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

package org.metacsp.spatial.geometry;

import java.util.Vector;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;


public class GeometricConstraintSolver extends ConstraintSolver{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1610027841057830885L;

	public GeometricConstraintSolver() {
		super(new Class[]{GeometricConstraint.class}, Polygon.class);
		this.setOptions(OPTIONS.AUTO_PROPAGATE);
	}

	@Override
	public boolean propagate() {
		Constraint[] cons = this.getConstraints();		
		for (int i = 0; i < cons.length; i++) {
			if(((GeometricConstraint)cons[i]).getType().equals(GeometricConstraint.Type.DC)){	
				System.out.println((Polygon)((GeometricConstraint)cons[i]).getFrom());				
				appltPolygonSeparation((Polygon)((GeometricConstraint)cons[i]).getFrom(), (Polygon)((GeometricConstraint)cons[i]).getTo());
			}else if(((GeometricConstraint)cons[i]).getType().equals(GeometricConstraint.Type.PP)){
				applyCliping((Polygon)((GeometricConstraint)cons[i]).getFrom(), (Polygon)((GeometricConstraint)cons[i]).getTo());
			}
		}		
		return true;
	}

	private void applyCliping(Polygon from, Polygon to) {
		// TODO Auto-generated method stub
		
	}

	private void appltPolygonSeparation(Polygon p1, Polygon p2) {
		Manifold manifold = new Manifold(p1, p2);
		manifold.solve();
		manifold.positionalCorrection();
		Vector<Vec2> vecnew = p1.getShiftedPolygon();		
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
		Polygon[] ret = new Polygon[num];
		for (int i = 0; i < num; i++) ret[i] = new Polygon(this, IDs++);
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

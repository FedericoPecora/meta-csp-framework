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
//				System.out.println((Polygon)((GeometricConstraint)cons[i]).getFrom());				
				appltPolygonSeparation((Polygon)((GeometricConstraint)cons[i]).getFrom(), (Polygon)((GeometricConstraint)cons[i]).getTo());
			}else if(((GeometricConstraint)cons[i]).getType().equals(GeometricConstraint.Type.INSIDE)){
//				System.out.println((Polygon)((GeometricConstraint)cons[i]).getFrom());
				applyInside((Polygon)((GeometricConstraint)cons[i]).getFrom(), (Polygon)((GeometricConstraint)cons[i]).getTo());
			}
		}		
		return true;
	}

	private void applyInside(Polygon p1, Polygon p2) {
		Manifold manifold = new Manifold(p1, p2);
		//if p1 and p2 are separated change the domain of p1 equal to p2		
		if(!manifold.solve()){			
			p1.setDomain(p2.getFullSpaceRepresentation().toArray(new Vec2[p2.getFullSpaceRepresentation().size()]));			
		}else{ 		//else if they are intersected p1 has to the result of intersection
			SutherlandHodgman slh = new SutherlandHodgman(p1, p2);
			p1.setDomain(slh.getClippedResult());
		}
	}

	private void appltPolygonSeparation(Polygon p1, Polygon p2) {
		Manifold manifold = new Manifold(p1, p2);
		if(manifold.solve()){
			manifold.positionalCorrection();
		}
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
	
	public static String drawPolygons(Vector<Vector<Vec2>> toPlots, long horizon){
		String ret = "";
		int j = 1;
		ret = "set xrange [-102:" + horizon +"]"+ "\n";
		ret += "set yrange [-102:" + horizon +"]" + "\n";
		int i = 0;		
		for (Vector<Vec2> vector : toPlots) {
			ret += "set obj " + j + " polygon from ";
			for (int k = 0; k < vector.size() - 1; k++) {
				ret += vector.get(k).x + "," + vector.get(k).y + " to ";
			}
			ret += vector.lastElement().x + "," + vector.lastElement().y + " front fs transparent solid 0.0 border " + (i+1) +" lw 2" + "\n";;		
			j++;
			i++;
		}
		ret += "plot " + "NaN" + "\n";
		ret += "pause -1";
		return ret;
	}
	

}

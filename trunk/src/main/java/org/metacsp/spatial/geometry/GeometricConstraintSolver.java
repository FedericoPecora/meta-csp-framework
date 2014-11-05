package org.metacsp.spatial.geometry;
import java.util.HashMap;
import java.util.Vector;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;


public class GeometricConstraintSolver extends ConstraintSolver{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1610027841057830885L;
	private HashMap<GeometricConstraint, HashMap<Polygon, Vec2[]>> constraintTrack = new HashMap<GeometricConstraint, HashMap<Polygon,Vec2[]>>();
	
	public GeometricConstraintSolver() {
		super(new Class[]{GeometricConstraint.class}, Polygon.class);
		this.setOptions(OPTIONS.AUTO_PROPAGATE);
	}

	@Override
	public boolean propagate() {
		Constraint[] cons = this.getConstraints();	
		for (int i = 0; i < cons.length; i++) {
			if(!cons[i].isMasked()){
				if(((GeometricConstraint)cons[i]).getType().equals(GeometricConstraint.Type.DC)){
					Manifold manifold = new Manifold((Polygon)((GeometricConstraint)cons[i]).getFrom(), (Polygon)((GeometricConstraint)cons[i]).getTo());
					if(manifold.isCollided()){
						System.out.println("PROPAGATED DC between Polygon " + ((Polygon)((GeometricConstraint)cons[i]).getFrom()).getID() + " Polygon " + ((Polygon)((GeometricConstraint)cons[i]).getTo()).getID());
						appltPolygonSeparation((Polygon)((GeometricConstraint)cons[i]).getFrom(), (Polygon)((GeometricConstraint)cons[i]).getTo());
						
					}				
				}else if(((GeometricConstraint)cons[i]).getType().equals(GeometricConstraint.Type.INSIDE)){
					Manifold manifold = new Manifold((Polygon)((GeometricConstraint)cons[i]).getFrom(), (Polygon)((GeometricConstraint)cons[i]).getTo());
					//if(!manifold.isCollided()){
						applyInside((Polygon)((GeometricConstraint)cons[i]).getFrom(), (Polygon)((GeometricConstraint)cons[i]).getTo());
						System.out.println("PROPAGATED INSIDE between Polygon " + ((Polygon)((GeometricConstraint)cons[i]).getFrom()).getID() + " Polygon " + ((Polygon)((GeometricConstraint)cons[i]).getTo()).getID());
					//}
				}				
			}
		}		
		return true;
	}

	private void applyInside(Polygon p1, Polygon p2) {
//		Manifold manifold = new Manifold(p1, p2);
		//if p1 and p2 are separated change the domain of p1 equal to p2		
//		if(!manifold.solve()){			
			float difx = p2.getPosition().x - p1.getPosition().x ;
			float dify = p2.getPosition().y - p1.getPosition().y ;
			Vec2[] p1Domain = new Vec2[p1.getFullSpaceRepresentation().size()];
			for (int i = 0; i < p1.getFullSpaceRepresentation().size(); i++) {
				p1Domain[i] = new Vec2(p1.getFullSpaceRepresentation().get(i).x + difx,
						p1.getFullSpaceRepresentation().get(i).y + dify);
			}
			p1.setDomain(p1Domain);
			SutherlandHodgman slh = new SutherlandHodgman(p1, p2);
			p1.setDomain(slh.getClippedResult());			
//		}else{ 		//else if they are intersected p1 has to the result of intersection
//			SutherlandHodgman slh = new SutherlandHodgman(p1, p2);
//			p1.setDomain(slh.getClippedResult());
//		}
	}

	private void appltPolygonSeparation(Polygon p1, Polygon p2) {
		Manifold manifold = new Manifold(p1, p2);
		if(manifold.solve()){
			manifold.positionalCorrection();
		}
	}

	@Override
	protected boolean addConstraintsSub(Constraint[] c) {
		Constraint[] cons = c;		
		for (int i = 0; i < cons.length; i++) {
			//handling movable property
			if(!((Polygon)((GeometricConstraint)cons[i]).getFrom()).isMovable())
				return false;			
			//keep track of added constraint for removing. i.e, taking snapshot of current domain of the vars
			HashMap<Polygon, Vec2[]> poly2Domain = new HashMap<Polygon, Vec2[]>();
			for (int j = 0; j < this.getVariables().length; j++) {
				poly2Domain.put((Polygon)this.getVariables()[j], ((Polygon)this.getVariables()[j]).getFullSpaceRepresentation()
						.toArray(new Vec2[((Polygon)this.getVariables()[j]).getFullSpaceRepresentation().size()]));
			}
			constraintTrack.put((GeometricConstraint)c[i], poly2Domain);
			//if there is already any constraint between scopes of the added constraint removes the constraint
			
			if(this.getConstraints((Polygon)((GeometricConstraint)cons[i]).getFrom(), (Polygon)((GeometricConstraint)cons[i]).getTo()).length > 0){
				this.getConstraints((Polygon)((GeometricConstraint)cons[i]).getFrom(), (Polygon)((GeometricConstraint)cons[i]).getTo())[0].mask();
				System.out.println("MAKSED CONSTRAINT: " + this.getConstraints((Polygon)((GeometricConstraint)cons[i]).getFrom(), (Polygon)((GeometricConstraint)cons[i]).getTo())[0]);
			}
			//adding constraint
			if(((GeometricConstraint)cons[i]).getType().equals(GeometricConstraint.Type.DC)){	
				appltPolygonSeparation((Polygon)((GeometricConstraint)cons[i]).getFrom(), (Polygon)((GeometricConstraint)cons[i]).getTo());
				System.out.println("added DC between Polygon " + ((Polygon)((GeometricConstraint)cons[i]).getFrom()).getID() + " Polygon " + ((Polygon)((GeometricConstraint)cons[i]).getTo()).getID());			}
			else if(((GeometricConstraint)cons[i]).getType().equals(GeometricConstraint.Type.INSIDE)){
				applyInside((Polygon)((GeometricConstraint)cons[i]).getFrom(), (Polygon)((GeometricConstraint)cons[i]).getTo());
				System.out.println("Added INSIDE between Polygon " + ((Polygon)((GeometricConstraint)cons[i]).getFrom()).getID() + " Polygon " + ((Polygon)((GeometricConstraint)cons[i]).getTo()).getID());
			}
		}		
		return true;
	}

	@Override
	protected void removeConstraintsSub(Constraint[] c) {
		for (int i = 0; i < c.length; i++) {
			System.out.println("remove constraint between Polygon " + ((Polygon)((GeometricConstraint)c[i]).getFrom()).getID() + " Polygon " + ((Polygon)((GeometricConstraint)c[i]).getTo()).getID());
			HashMap<Polygon, Vec2[]> temp = constraintTrack.get((GeometricConstraint)c[i]);
			for (Polygon p : temp.keySet()) {
				p.setDomain(temp.get(p));
			}
			constraintTrack.remove(c);
		}
		
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

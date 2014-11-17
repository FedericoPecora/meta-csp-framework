package org.metacsp.spatial.geometry;
import java.util.HashMap;
import java.util.Vector;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;


public class GeometricConstraintSolver extends RCC2ConstraintSolver{

	private static final long serialVersionUID = -1610027841057830885L;

	//Min and max dimensions of the Eucledian space
	public static final float MAX_X = 10000.0f;
	public static final float MAX_Y = 10000.0f;
	public static final float MIN_X = -10000.0f;
	public static final float MIN_Y = -10000.0f;

	private HashMap<GeometricConstraint, HashMap<Polygon, Vec2[]>> constraintTrack = new HashMap<GeometricConstraint, HashMap<Polygon,Vec2[]>>();

	public GeometricConstraintSolver() {
		//super(new Class[]{GeometricConstraint.class}, Polygon.class);
		super();
		this.setOptions(OPTIONS.AUTO_PROPAGATE);
	}

	@Override
	public boolean propagate() {
		if(!super.propagate()) return false;
		Constraint[] cons = this.getConstraints();	
		for (int i = 0; i < cons.length; i++) {
			if(!super.propagate()) return false;
			if(((GeometricConstraint)cons[i]).getType().equals(GeometricConstraint.Type.DC)){
				Manifold manifold = new Manifold((Polygon)((GeometricConstraint)cons[i]).getFrom(), (Polygon)((GeometricConstraint)cons[i]).getTo());
				if(manifold.isCollided()){
					System.out.println("PROPAGATED DC between Polygon " + ((Polygon)((GeometricConstraint)cons[i]).getFrom()).getID() + " Polygon " + ((Polygon)((GeometricConstraint)cons[i]).getTo()).getID());
					applyPolygonSeparation((Polygon)((GeometricConstraint)cons[i]).getFrom(), (Polygon)((GeometricConstraint)cons[i]).getTo());
				}				
			}else if(((GeometricConstraint)cons[i]).getType().equals(GeometricConstraint.Type.INSIDE)){
				Manifold manifold = new Manifold((Polygon)((GeometricConstraint)cons[i]).getFrom(), (Polygon)((GeometricConstraint)cons[i]).getTo());
				//if(!manifold.isCollided()){
				applyInside((Polygon)((GeometricConstraint)cons[i]).getFrom(), (Polygon)((GeometricConstraint)cons[i]).getTo());
				System.out.println("PROPAGATED INSIDE between Polygon " + ((Polygon)((GeometricConstraint)cons[i]).getFrom()).getID() + " Polygon " + ((Polygon)((GeometricConstraint)cons[i]).getTo()).getID());
				//}
			}				
		}		
		return true;
	}

	
	private boolean checkInsideWithoutUpdate(Polygon p1, Polygon p2) {
		float EPSILON = 0.0003f;
		SutherlandHodgman slh = new SutherlandHodgman(p1, p2);
		for (int i = 0; i < p1.getFullSpaceRepresentation().size(); i++) {
			if(Math.abs(p1.getFullSpaceRepresentation().get(i).x - slh.getClippedResult()[i].x) < EPSILON &&
					Math.abs(p1.getFullSpaceRepresentation().get(i).y - slh.getClippedResult()[i].y) < EPSILON)
				return true;
		}
		return false;			
	}
	
	private void applyInside(Polygon p1, Polygon p2) {
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
	}

	private void applyPolygonSeparation(Polygon p1, Polygon p2) {
		Manifold manifold = new Manifold(p1, p2);
		if(manifold.solve()){
			manifold.positionalCorrection();
		}
	}

	@Override
	protected boolean addConstraintsSub(Constraint[] c) {
		//if(!super.propagate()) return false;
		Constraint[] cons = c;		
		for (int i = 0; i < cons.length; i++) {
			//handling movable property
			if(!((Polygon)((GeometricConstraint)cons[i]).getFrom()).isMovable())
				return verifySituation(cons[i]);
			//if there is already any constraint between scopes of the added constraint reject the constraint			
			if(this.getConstraints((Polygon)((GeometricConstraint)cons[i]).getFrom(), (Polygon)((GeometricConstraint)cons[i]).getTo()).length > 0){
				if(((GeometricConstraint)(this.getConstraints((Polygon)((GeometricConstraint)cons[i]).getFrom(), (Polygon)((GeometricConstraint)cons[i]).getTo())[0])).getType().equals((GeometricConstraint)c[i]))
					return true;
				else
					return false;
				//				this.getConstraints((Polygon)((GeometricConstraint)cons[i]).getFrom(), (Polygon)((GeometricConstraint)cons[i]).getTo())[0].mask();
				//				System.out.println("MAKSED CONSTRAINT: " + this.getConstraints((Polygon)((GeometricConstraint)cons[i]).getFrom(), (Polygon)((GeometricConstraint)cons[i]).getTo())[0]);
			}
			//keep track of added constraint for removing. i.e, taking snapshot of current domain of the vars
			HashMap<Polygon, Vec2[]> poly2Domain = new HashMap<Polygon, Vec2[]>();
			for (int j = 0; j < this.getVariables().length; j++) {
				poly2Domain.put((Polygon)this.getVariables()[j], ((Polygon)this.getVariables()[j]).getFullSpaceRepresentation()
						.toArray(new Vec2[((Polygon)this.getVariables()[j]).getFullSpaceRepresentation().size()]));
			}
			constraintTrack.put((GeometricConstraint)c[i], poly2Domain);
			//adding constraint
			if(((GeometricConstraint)cons[i]).getType().equals(GeometricConstraint.Type.DC)){	
				applyPolygonSeparation((Polygon)((GeometricConstraint)cons[i]).getFrom(), (Polygon)((GeometricConstraint)cons[i]).getTo());
				System.out.println("added DC between Polygon " + ((Polygon)((GeometricConstraint)cons[i]).getFrom()).getID() + " Polygon " + ((Polygon)((GeometricConstraint)cons[i]).getTo()).getID());			}
			else if(((GeometricConstraint)cons[i]).getType().equals(GeometricConstraint.Type.INSIDE)){
				applyInside((Polygon)((GeometricConstraint)cons[i]).getFrom(), (Polygon)((GeometricConstraint)cons[i]).getTo());
				System.out.println("Added INSIDE between Polygon " + ((Polygon)((GeometricConstraint)cons[i]).getFrom()).getID() + " Polygon " + ((Polygon)((GeometricConstraint)cons[i]).getTo()).getID());
			}
		}		
		return true;
	}

	private boolean verifySituation(Constraint c) {
			if(((GeometricConstraint)c).getType().equals(GeometricConstraint.Type.DC)){
				Manifold manifold = new Manifold((Polygon)((GeometricConstraint)c).getFrom(), (Polygon)((GeometricConstraint)c).getTo());
				if(manifold.isCollided()){
					return false;
				}				
			}else if(((GeometricConstraint)c).getType().equals(GeometricConstraint.Type.INSIDE)){
				Manifold manifold = new Manifold((Polygon)((GeometricConstraint)c).getFrom(), (Polygon)((GeometricConstraint)c).getTo());
//				if(manifold.isCollided())
//					return false;
				return checkInsideWithoutUpdate((Polygon)((GeometricConstraint)c).getFrom(), (Polygon)((GeometricConstraint)c).getTo());
			}				
		return false;
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

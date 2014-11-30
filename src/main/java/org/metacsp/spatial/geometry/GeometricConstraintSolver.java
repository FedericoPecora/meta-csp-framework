package org.metacsp.spatial.geometry;
import java.util.HashMap;
import java.util.Vector;

import org.metacsp.framework.Constraint;



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
//		if(!super.propagate()) return false;
//		Constraint[] cons = this.getConstraints();	
//		for (int i = 0; i < cons.length; i++) {
//			if(!super.propagate()) return false;
//			if(((GeometricConstraint)cons[i]).getType().equals(GeometricConstraint.Type.DC)){
//				Manifold manifold = new Manifold((Polygon)((GeometricConstraint)cons[i]).getFrom(), (Polygon)((GeometricConstraint)cons[i]).getTo());
//				if(manifold.isCollided()){
//					System.out.println("PROPAGATED DC between Polygon " + ((Polygon)((GeometricConstraint)cons[i]).getFrom()).getID() + " Polygon " + ((Polygon)((GeometricConstraint)cons[i]).getTo()).getID());
//					applyDCcliping((Polygon)((GeometricConstraint)cons[i]).getFrom(), (Polygon)((GeometricConstraint)cons[i]).getTo());
//				}				
//			}else if(((GeometricConstraint)cons[i]).getType().equals(GeometricConstraint.Type.INSIDE)){
//				applyInside((Polygon)((GeometricConstraint)cons[i]).getFrom(), (Polygon)((GeometricConstraint)cons[i]).getTo());
//				System.out.println("PROPAGATED INSIDE between Polygon " + ((Polygon)((GeometricConstraint)cons[i]).getFrom()).getID() + " Polygon " + ((Polygon)((GeometricConstraint)cons[i]).getTo()).getID());
//				//}
//			}				
//		}		
		return true;
	}


	public static GeometricConstraint.Type getRelation(Polygon p1, Polygon p2) {
		if (checkInside(p1, p2)) return GeometricConstraint.Type.INSIDE;
		return GeometricConstraint.Type.DC;
	}
	
	private static boolean checkInside(Polygon p1, Polygon p2) {
		float EPSILON = 0.003f;
		//arg 2 will be clipped, arg 1 is clipper
		SutherlandHodgman slh = new SutherlandHodgman(p2, p1);
		for (int i = 0; i < p1.getFullSpaceRepresentation().size(); i++) {
			boolean found = false;
			for (int j = 0; j < slh.getClippedResult().length; j++) {				
				if(Math.abs(p1.getFullSpaceRepresentation().get(i).x - slh.getClippedResult()[j].x) < EPSILON &&
						Math.abs(p1.getFullSpaceRepresentation().get(i).y - slh.getClippedResult()[j].y) < EPSILON) found = true;						
			}
			if(!found) return false;
//			System.out.println("p1: " + p1.getFullSpaceRepresentation().get(i).x + " " + p1.getFullSpaceRepresentation().get(i).y);
//			System.out.println("getClippedResult: " + slh.getClippedResult()[i].x + " " + slh.getClippedResult()[i].y);				
		}
		return true;			
	}
	
	private Vector<Vec2> getInsideVertices(Polygon p1, Polygon p2){
		
		Vector<Vec2> ret = new Vector<Vec2>();
		int reoslution = 10000;
		int[] xpoints = new int[p2.getFullSpaceRepresentation().size()];
		int[] ypoints = new int[p2.getFullSpaceRepresentation().size()];
		for (int j = 0; j < p2.getFullSpaceRepresentation().size(); j++) {
			xpoints[j] = (int)(p2.getFullSpaceRepresentation().get(j).x * reoslution);
			ypoints[j] = (int)(p2.getFullSpaceRepresentation().get(j).y * reoslution);
		}

		java.awt.Polygon newp2 = new  java.awt.Polygon(xpoints, ypoints, p2.getFullSpaceRepresentation().size());
		for (int i = 0; i < p1.getFullSpaceRepresentation().size(); i++) {
			if(newp2.contains(p1.getFullSpaceRepresentation().get(i).x*reoslution, p1.getFullSpaceRepresentation().get(i).y*reoslution))
				ret.add(p1.getFullSpaceRepresentation().get(i));
		}
		
		return ret;
	}

	private boolean applyInside(Polygon p1, Polygon p2) {
		float difx = p2.getPosition().x - p1.getPosition().x ;
		float dify = p2.getPosition().y - p1.getPosition().y ;
		Vec2[] p1Copy = new Vec2[p1.getFullSpaceRepresentation().size()];
		Vec2[] p1Domain = new Vec2[p1.getFullSpaceRepresentation().size()];
		for (int i = 0; i < p1.getFullSpaceRepresentation().size(); i++) {
			p1Copy[i] = new Vec2(p1.getFullSpaceRepresentation().get(i).x, p1.getFullSpaceRepresentation().get(i).y);
			p1Domain[i] = new Vec2(p1.getFullSpaceRepresentation().get(i).x + difx, p1.getFullSpaceRepresentation().get(i).y + dify);
		}
		p1.setDomain(p1Domain);
		if(checkInside(p1, p2)) return true;
		else{
			p1.setDomain(p1Copy);
			return false;
		}

//		SutherlandHodgman slh = new SutherlandHodgman(p1, p2);
//		p1.setDomain(slh.getClippedResult());			
	}
	
	private boolean applyDCcliping(Polygon p1, Polygon p2) {
		
		Vector<Vec2> toBeAdded = new Vector<Vec2>();
		Vector<Vec2> toBeRemoved = new Vector<Vec2>();
		Vector<Vec2> newDomain = new Vector<Vec2>();

		SutherlandHodgman slh = new SutherlandHodgman(p1, p2);
		toBeAdded = slh.getContactPoints();
		
		toBeRemoved = getInsideVertices(p1, p2);
	
		//remove the Vertex Inside the other polygon
		float EPSILON = 0.003f;
		boolean found = false;
		for (int i = 0; i < p1.getFullSpaceRepresentation().size(); i++) {
			found = false;
			for (int j = 0; j < toBeRemoved.size(); j++) {		
				if(Math.abs(p1.getFullSpaceRepresentation().get(i).x - toBeRemoved.get(j).x) < EPSILON &&
						Math.abs(p1.getFullSpaceRepresentation().get(i).y - toBeRemoved.get(j).y) < EPSILON) found = true;						
			}
			if(!found) newDomain.add(p1.getFullSpaceRepresentation().get(i));	
		}
		
		//add To be contact vertices
		for (int i = 0; i < toBeAdded.size(); i++) {
			newDomain.add(toBeAdded.get(i));
		}
//		for (int i = 0; i < newDomain.size(); i++) {
//			System.out.println("---" + newDomain.get(i).x + " " + newDomain.get(i).y);
//		}
		
		p1.setDomain(newDomain.toArray(new Vec2[newDomain.size()]));
		
		Manifold manifold = new Manifold(p1, p2);
		if(!manifold.isCollided()){
			return true;
		}
		return false;
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
				else return false;
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
				return applyDCcliping((Polygon)((GeometricConstraint)cons[i]).getFrom(), (Polygon)((GeometricConstraint)cons[i]).getTo());
				//				System.out.println("added DC between Polygon " + ((Polygon)((GeometricConstraint)cons[i]).getFrom()).getID() + " Polygon " + ((Polygon)((GeometricConstraint)cons[i]).getTo()).getID());			}
			}
			else if(((GeometricConstraint)cons[i]).getType().equals(GeometricConstraint.Type.INSIDE)){
				if(!applyInside((Polygon)((GeometricConstraint)cons[i]).getFrom(), (Polygon)((GeometricConstraint)cons[i]).getTo()))
					return false;
//				System.out.println("Added INSIDE between Polygon " + ((Polygon)((GeometricConstraint)cons[i]).getFrom()).getID() + " Polygon " + ((Polygon)((GeometricConstraint)cons[i]).getTo()).getID());
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
			return checkInside((Polygon)((GeometricConstraint)c).getFrom(), (Polygon)((GeometricConstraint)c).getTo());
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

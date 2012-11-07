package spatial.rectangleAlgebra;

import java.util.Vector;

import multi.allenInterval.AllenInterval;
import multi.allenInterval.AllenIntervalConstraint;
import multi.allenInterval.AllenIntervalNetworkSolver;
import time.APSPSolver;
import time.Bounds;

/**
 * 
 * @author iran
 *
 */

public class AugmentedRectangleConstraintSolver extends RectangleConstraintSolver {

	/**
	 * 
	 */
	private long horizon = 100;
	private AllenIntervalNetworkSolver solverX;
	private AllenIntervalNetworkSolver solverY;
	private Bounds xLB;
	private Bounds xUB;
	private Bounds yLB;
	private Bounds yUB;

	
	public AugmentedRectangleConstraintSolver() {
		super();
		this.setOptions(OPTIONS.AUTO_PROPAGATE);
	}
	
	@Override
	public boolean propagate() {
		if(super.getConstraints().length == 0) 
			return true;
		if(super.propagate())
			convertRectangleTo2DimensionSTP(super.getCompleteRARelations());
		
		return true;
	}
	
	public AllenIntervalNetworkSolver[] getInternalSolver(){
		return new AllenIntervalNetworkSolver[]{solverX, solverY};
	}
	
	
	private boolean convertRectangleTo2DimensionSTP(Vector<Vector<RectangleConstraint>> consrels){

		solverX = new AllenIntervalNetworkSolver(0, horizon);
		solverY = new AllenIntervalNetworkSolver(0, horizon);

		
		Vector<AllenIntervalConstraint> xAllenConstraint = new Vector<AllenIntervalConstraint>();
		Vector<AllenIntervalConstraint> yAllenConstraint = new Vector<AllenIntervalConstraint>();
		
		AllenInterval[] intervalsx = (AllenInterval[])solverX.createVariables(consrels.size());
		AllenInterval[] intervalsy = (AllenInterval[])solverY.createVariables(consrels.size());

		Bounds xLB, xUB,yLB, yUB;
		for (int i = 0; i < this.getVariables().length; i++) {
			intervalsx[i].setName(((RectangularRegion)this.getVariables()[i]).getName());
			intervalsy[i].setName(((RectangularRegion)this.getVariables()[i]).getName());
			if(((RectangularRegion)this.getVariables()[i]).getBoundingbox() != null){
				xLB = ((RectangularRegion)this.getVariables()[i]).getBoundingbox().getxLB();
				xUB = ((RectangularRegion)this.getVariables()[i]).getBoundingbox().getxUB();
				
				yLB = ((RectangularRegion)this.getVariables()[i]).getBoundingbox().getyLB();
				yUB = ((RectangularRegion)this.getVariables()[i]).getBoundingbox().getyUB();
	

				
				AllenIntervalConstraint releaseX = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, 
						xLB);
				releaseX.setFrom(intervalsx[i]);
				releaseX.setTo(intervalsx[i]);
				xAllenConstraint.add(releaseX);
				
				AllenIntervalConstraint deadlineX = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, 
						xUB);
				deadlineX.setFrom(intervalsx[i]);
				deadlineX.setTo(intervalsx[i]);
				xAllenConstraint.add(deadlineX);
				
				
				AllenIntervalConstraint releaseY = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, 
						yLB);
				releaseY.setFrom(intervalsy[i]);
				releaseY.setTo(intervalsy[i]);
				yAllenConstraint.add(releaseY);
				
				
				AllenIntervalConstraint deadlineY = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, 
						yUB);
				deadlineY.setFrom(intervalsy[i]);
				deadlineY.setTo(intervalsy[i]);
				yAllenConstraint.add(deadlineY);
			}
			else{
				
				AllenIntervalConstraint releaseX = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, 
						new Bounds(0, APSPSolver.INF));
				releaseX.setFrom(intervalsx[i]);
				releaseX.setTo(intervalsx[i]);
				xAllenConstraint.add(releaseX);
				
				AllenIntervalConstraint deadlineX = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, 
						new Bounds(0, APSPSolver.INF));
				deadlineX.setFrom(intervalsx[i]);
				deadlineX.setTo(intervalsx[i]);
				xAllenConstraint.add(deadlineX);
				
				
				AllenIntervalConstraint releaseY = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, 
						new Bounds(0, APSPSolver.INF));
				releaseY.setFrom(intervalsy[i]);
				releaseY.setTo(intervalsy[i]);
				yAllenConstraint.add(releaseY);
				
				AllenIntervalConstraint deadlineY = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, 
						new Bounds(0, APSPSolver.INF));
				deadlineY.setFrom(intervalsy[i]);
				deadlineY.setTo(intervalsy[i]);
				yAllenConstraint.add(deadlineY);
			}
		}
		
		for (int i = 0; i < consrels.size(); i++) {
			for (int j = i + 1; j < consrels.size(); j++) {
				
				//create convexity
				TwoDimensionsAllenConstraint[] convex2DAllen = RectangleConstraint.getRAConvexClosure(consrels.get(i).get(j).getTypes());
				
				Vector<AllenIntervalConstraint.Type> xtp = new Vector<AllenIntervalConstraint.Type>(); 
				Vector<AllenIntervalConstraint.Type> ytp = new Vector<AllenIntervalConstraint.Type>();
				for (int j2 = 0; j2 < convex2DAllen.length; j2++) {
					if(!xtp.contains(AllenIntervalConstraint.Type.fromString(convex2DAllen[j2].getAllenType()[0].name())))
						xtp.add(AllenIntervalConstraint.Type.fromString(convex2DAllen[j2].getAllenType()[0].name()));
					if(!ytp.contains(AllenIntervalConstraint.Type.fromString(convex2DAllen[j2].getAllenType()[1].name())))
						ytp.add(AllenIntervalConstraint.Type.fromString(convex2DAllen[j2].getAllenType()[1].name()));
				}
				
				AllenIntervalConstraint btwintervalx = new AllenIntervalConstraint(xtp.toArray(new AllenIntervalConstraint.Type[xtp.size()]));
				btwintervalx.setFrom(intervalsx[i]);
				btwintervalx.setTo(intervalsx[j]);
				xAllenConstraint.add(btwintervalx);
				
				AllenIntervalConstraint btwintervaly = new AllenIntervalConstraint(ytp.toArray(new AllenIntervalConstraint.Type[ytp.size()]));
				btwintervaly.setFrom(intervalsy[i]);
				btwintervaly.setTo(intervalsy[j]);
				yAllenConstraint.add(btwintervaly);				
			}
		}
		
		if(super.getBoundedConstraint() != null){
			for (int i = 0; i < super.getBoundedConstraint().size(); i++) {
				AllenIntervalConstraint boundedIntervalX = new AllenIntervalConstraint(super.getBoundedConstraint().get(i).getBoundedConstraintX().getType(), 
						super.getBoundedConstraint().get(i).getBoundedConstraintX().getBounds());
				boundedIntervalX.setFrom(intervalsx[super.getBoundedConstraint().get(i).getFrom().getID()]);
				boundedIntervalX.setTo(intervalsx[super.getBoundedConstraint().get(i).getTo().getID()]);
				xAllenConstraint.add(boundedIntervalX);
				
				AllenIntervalConstraint boundedIntervalY = new AllenIntervalConstraint(super.getBoundedConstraint().get(i).getBoundedConstraintY().getType(), 
						super.getBoundedConstraint().get(i).getBoundedConstraintY().getBounds());
				boundedIntervalY.setFrom(intervalsy[super.getBoundedConstraint().get(i).getFrom().getID()]);
				boundedIntervalY.setTo(intervalsy[super.getBoundedConstraint().get(i).getTo().getID()]);
				yAllenConstraint.add(boundedIntervalY);
			}
		}
		
		AllenIntervalConstraint[] consX = xAllenConstraint.toArray(new AllenIntervalConstraint[xAllenConstraint.size()]);	
		if (!solverX.addConstraints(consX)) { 
			System.out.println("Failed to add constraints in X dimension! ");
			return false;
			
		}
		//ConstraintNetwork.draw(solverX.getConstraintNetwork(), "X");
		AllenIntervalConstraint[] consY = yAllenConstraint.toArray(new AllenIntervalConstraint[yAllenConstraint.size()]);
		if (!solverY.addConstraints(consY)) { 
			System.out.println("Failed to add constraints in Y dimension! ");
			return false;
		}
		
		
		
		//ConstraintNetwork.draw(solverY.getConstraintNetwork(), "Y"); 		
		return true;
		
	}
	
	/**
	 *  it computes a bounding box which with two bounds on both sides resulted from 2 extreme bounded 2D STP solution 
	 * @param name of the rectangle region
	 * @return a bounding box 
	 */
	public BoundingBox extractBoundingBoxesFromSTPs(String name){
		for (int i = 0; i < solverX.getVariables().length; i++) {
			if(((AllenInterval)solverX.getVariables()[i]).getName().compareTo(name) == 0){
				this.xLB = new Bounds(((AllenInterval)solverX.getVariables()[i]).getEST(), ((AllenInterval)solverX.getVariables()[i]).getLST());
				this.xUB = new Bounds(((AllenInterval)solverX.getVariables()[i]).getEET(), ((AllenInterval)solverX.getVariables()[i]).getLET());
				this.yLB = new Bounds(((AllenInterval)solverY.getVariables()[i]).getEST(), ((AllenInterval)solverY.getVariables()[i]).getLST());
				this.yUB = new Bounds(((AllenInterval)solverY.getVariables()[i]).getEET(), ((AllenInterval)solverY.getVariables()[i]).getLET());
				return new BoundingBox(xLB, xUB, yLB, yUB);
			}
		}
		return null;
	}
	
	/**
	 * make script readable for gnuplot
	 * @param st
	 */	
	public String drawMinMaxRectangle(long horizon, String... st){
		int j = 1;
		String ret = "";
		ret = "set xrange [0:" + horizon +"]" + "\n";
		ret += "set yrange [0:" + horizon +"]" + "\n";
		for (int i = 0; i < st.length; i++) {
			//rec min
			ret += "set obj " + j + " rect from " + extractBoundingBoxesFromSTPs(st[i]).getMinRectabgle().getMinX() + "," + extractBoundingBoxesFromSTPs(st[i]).getMinRectabgle().getMinY() 
					+" to " + extractBoundingBoxesFromSTPs(st[i]).getMinRectabgle().getMaxX() + "," +extractBoundingBoxesFromSTPs(st[i]).getMinRectabgle().getMaxY() + 
					" front fs transparent solid 0.0 border " + (i+1) +" lw 0.5" + "\n";
			j++;
			//label of rec min
			ret += "set label " + "\""+ st[i]+"-min" +"\""+" at "+ extractBoundingBoxesFromSTPs(st[i]).getMinRectabgle().getMinX() +"," 
			+ extractBoundingBoxesFromSTPs(st[i]).getMinRectabgle().getCenterY() + " textcolor lt " + (i+1) + " font \"9\"" + "\n";
			//rec max
			ret += "set obj " + j + " rect from " + extractBoundingBoxesFromSTPs(st[i]).getMaxRectabgle().getMinX() + "," + extractBoundingBoxesFromSTPs(st[i]).getMaxRectabgle().getMinY() 
					+" to " + extractBoundingBoxesFromSTPs(st[i]).getMaxRectabgle().getMaxX() + "," + extractBoundingBoxesFromSTPs(st[i]).getMaxRectabgle().getMaxY() + 
					" front fs transparent solid 0.0 border " + (i+1) +" lw 0.5" + "\n";
			j++;
			//label of rec max			
			ret += "set label " + "\""+ st[i]+"-max" +"\"" +" at "+ extractBoundingBoxesFromSTPs(st[i]).getMaxRectabgle().getMinX() +"," 
			+ extractBoundingBoxesFromSTPs(st[i]).getMaxRectabgle().getCenterY() + " textcolor lt " + (i+1) + " font \"9\"" + "\n";
			
			//point of the solution
			ret += "set object " + j +" circle at "+ extractBoundingBoxesFromSTPs(st[i]).getACentrePointSolution().x +","
			+ extractBoundingBoxesFromSTPs(st[i]).getACentrePointSolution().y + " size 0.15 fc lt " + (i+1) + "\n";
			
			//label of solution
			ret += "set label " + "\""+ st[i]+"-C" +"\"" +" at "+ extractBoundingBoxesFromSTPs(st[i]).getACentrePointSolution().x +"," 
					+ extractBoundingBoxesFromSTPs(st[i]).getACentrePointSolution().y + " textcolor lt " + (i+1) + " font \"9\"" + "\n";
			j++;
		}
		ret += "plot NaN" + "\n";
		ret += "pause -1" + "\n";
		return ret;
	}
	
	
	/**
	 * make script readable for gnuplot
	 * @param st
	 */
	public String drawAlmostCentreRectangle(long horizon, String... st){
		String ret = "";
		int j = 1;
		ret = "set xrange [0:" + horizon +"]"+ "\n";
		ret += "set yrange [0:" + horizon +"]" + "\n";
		for (int i = 0; i < st.length; i++) {
			//rec 
			ret += "set obj " + j + " rect from " + extractBoundingBoxesFromSTPs(st[i]).getAlmostCentreRectangle().getMinX() + "," + extractBoundingBoxesFromSTPs(st[i]).getAlmostCentreRectangle().getMinY() 
					+" to " + extractBoundingBoxesFromSTPs(st[i]).getAlmostCentreRectangle().getMaxX() + "," +extractBoundingBoxesFromSTPs(st[i]).getAlmostCentreRectangle().getMaxY() + 
					" front fs transparent solid 0.0 border " + (i+1) +" lw 0.5" + "\n";
			j++;
			//label of centre Rec
			ret += "set label " + "\""+ st[i]+"-c" +"\""+" at "+ extractBoundingBoxesFromSTPs(st[i]).getAlmostCentreRectangle().getCenterX() +"," 
					+ extractBoundingBoxesFromSTPs(st[i]).getAlmostCentreRectangle().getCenterY() + " textcolor lt " + (i+1) + " font \"9\"" + "\n";
			j++;
		}
		ret += "plot NaN" + "\n";
		ret += "pause -1";
		return ret;
	}
	
//	public void getCentredBoundingRectangle(String name){
//		
//		extractBoundingBoxesFromSTPs(name);
//		
//
//		Point cmExRec = getExtremeRectangleCM1();
//		Point cmUpTri = getUpperTriangleCM(name);
//		//Point cmLowTri = getLowerTriangleCM(name);
//
////		Point cmExRec = getExtremeRectangleCM();
////		Point cmUpTri = getUpperTriangleCM();
////		Point cmLowTri = getLowerTriangleCM();
//
//	
//	}
//
//
//	private Point getLowerTriangleCM(String name, Dimension dim) {
//		getDurationConstraintByVariableName(name, dim).
//		double x, y;
//		if(dim == Dimension.X){
//			x = (2/3 * (xLB.max - (xUB.min - dl)) + (xUB.min - dl));
//		}
//		
//		return null;
//	}
//
//	private Point getUpperTriangleCM(String name) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	private Point getExtremeRectangleCM1() {
//		return new Point(xLB.max - xLB.min, xUB.max - xUB.min);
//	}
//
//	private Point getLowerTriangleCM() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	private Point getUpperTriangleCM() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	private Point getExtremeRectangleCM() {
//		return new Point(xLB.max - xLB.min, xUB.max - xUB.min);
//	}

	
	

}
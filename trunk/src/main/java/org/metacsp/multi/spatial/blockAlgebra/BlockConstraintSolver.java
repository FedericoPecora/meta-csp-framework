package org.metacsp.multi.spatial.blockAlgebra;

import java.util.HashMap;
import java.util.Vector;

import org.metacsp.multi.allenInterval.AllenInterval;
import org.metacsp.multi.allenInterval.AllenIntervalNetworkSolver;
import org.metacsp.multi.spatial.rectangleAlgebra.BoundingBox;
import org.metacsp.time.Bounds;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiConstraintSolver;

/**
 * This class represents Block Algebra constraints. Each constraint represents three dimension Allen relations between spatial entities.    
 * In rectangle Algebra, each spatial entity is restricted to be an axis parallel bounding box.
 * 
 * @author Iran Mansouri
 *
 */

public class BlockConstraintSolver extends MultiConstraintSolver {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6694598836324746725L;
	private int IDs = 0;
	
	private HashMap<Integer, Variable> getVaribaleById = new HashMap<Integer, Variable>();
	public enum Dimension  {X, Y};

	public BlockConstraintSolver(long origin, long horizon) {
		super(new Class[] {BlockAlgebraConstraint.class, UnaryBlockConstraint.class}, RectangularCuboidRegion.class, createConstraintSolvers(origin, horizon, -1), new int[] {1,1,1});
	}

	public BlockConstraintSolver(long origin, long horizon, int maxRectangles) {
		super(new Class[] {BlockAlgebraConstraint.class, UnaryBlockConstraint.class}, RectangularCuboidRegion.class, createConstraintSolvers(origin, horizon, maxRectangles), new int[] {1,1,1});
	}

	private static ConstraintSolver[] createConstraintSolvers(long origin, long horizon, int maxRectangles) {
		ConstraintSolver[] ret = new ConstraintSolver[3];
		//X
		ret[0] = (maxRectangles != -1 ? new AllenIntervalNetworkSolver(origin, horizon, maxRectangles) : new AllenIntervalNetworkSolver(origin, horizon));
		//Y
		ret[1] = (maxRectangles != -1 ? new AllenIntervalNetworkSolver(origin, horizon, maxRectangles) : new AllenIntervalNetworkSolver(origin, horizon));
		//Z
		ret[2] = (maxRectangles != -1 ? new AllenIntervalNetworkSolver(origin, horizon, maxRectangles) : new AllenIntervalNetworkSolver(origin, horizon));
		return ret;
	}
	
//	@Override
//	protected Variable[] createVariablesSub(int num) {
//		Variable[] ret = new Variable[num];
//		for (int i = 0; i < num; i++) ret[i] = new RectangularRegion2(this, this.IDs++, this.constraintSolvers);
//		return ret;
//	}

	@Override
	public boolean propagate() {
		// Do nothing, AllenIntervalNetworkSolver takes care of propagation...
		return true;
	}


	/**
	 *  Extracts a specific {@link BoundingBox} from the domain of a {@link RectangularRegion}. 
	 * @param rect The {@link RectangularRegion} from to extract the {@link BoundingBox}.
	 * @return A specific {@link BoundingBox} from the domain of a {@link RectangularRegion}.
	 */
	public BoundingBox extractBoundingBoxesFromSTPs(RectangularCuboidRegion rect){
		Bounds xLB, xUB, yLB, yUB, zLB, zUB;
		xLB = new Bounds(((AllenInterval)rect.getInternalVariables()[0]).getEST(),((AllenInterval)rect.getInternalVariables()[0]).getLST()); 
		xUB = new Bounds(((AllenInterval)rect.getInternalVariables()[0]).getEET(),((AllenInterval)rect.getInternalVariables()[0]).getLET()); 
		yLB = new Bounds(((AllenInterval)rect.getInternalVariables()[1]).getEST(),((AllenInterval)rect.getInternalVariables()[1]).getLST()); 
		yUB = new Bounds(((AllenInterval)rect.getInternalVariables()[1]).getEET(),((AllenInterval)rect.getInternalVariables()[1]).getLET());
		zLB = new Bounds(((AllenInterval)rect.getInternalVariables()[2]).getEST(),((AllenInterval)rect.getInternalVariables()[2]).getLST()); 
		zUB = new Bounds(((AllenInterval)rect.getInternalVariables()[2]).getEET(),((AllenInterval)rect.getInternalVariables()[2]).getLET());
		return new BoundingBox(xLB, xUB, yLB, yUB, zLB, zUB);
	}
	
	public BoundingBox extractBoundingBoxesFromSTPs(String name){
		
		Bounds xLB, xUB, yLB, yUB, zLB, zUB;
		for (int i = 0; i < this.getConstraintSolvers()[0].getVariables().length; i++) {
			if(((RectangularCuboidRegion)this.getConstraintNetwork().getVariables()[i]).getName().compareTo(name) == 0){
				xLB = new Bounds(((AllenInterval)this.getConstraintSolvers()[0].getVariables()[i]).getEST(), ((AllenInterval)this.getConstraintSolvers()[0].getVariables()[i]).getLST());
				xUB = new Bounds(((AllenInterval)this.getConstraintSolvers()[0].getVariables()[i]).getEET(), ((AllenInterval)this.getConstraintSolvers()[0].getVariables()[i]).getLET());
				yLB = new Bounds(((AllenInterval)this.getConstraintSolvers()[1].getVariables()[i]).getEST(), ((AllenInterval)this.getConstraintSolvers()[1].getVariables()[i]).getLST());
				yUB = new Bounds(((AllenInterval)this.getConstraintSolvers()[1].getVariables()[i]).getEET(), ((AllenInterval)this.getConstraintSolvers()[1].getVariables()[i]).getLET());
				zLB = new Bounds(((AllenInterval)this.getConstraintSolvers()[2].getVariables()[i]).getEST(), ((AllenInterval)this.getConstraintSolvers()[2].getVariables()[i]).getLST());
				zUB = new Bounds(((AllenInterval)this.getConstraintSolvers()[2].getVariables()[i]).getEET(), ((AllenInterval)this.getConstraintSolvers()[2].getVariables()[i]).getLET());
				return new BoundingBox(xLB, xUB, yLB, yUB, zLB, zUB);
			}
		}
		return null;
	}
	
	public BoundingBox[] extractAllBoundingBoxesFromSTPs(){
		
		Vector<BoundingBox> bbs = new Vector<BoundingBox>();
		Bounds xLB, xUB, yLB, yUB, zLB, zUB;
		for (int i = 0; i < this.getConstraintSolvers()[0].getVariables().length; i++) {
			xLB = new Bounds(((AllenInterval)this.getConstraintSolvers()[0].getVariables()[i]).getEST(), ((AllenInterval)this.getConstraintSolvers()[0].getVariables()[i]).getLST());
			xUB = new Bounds(((AllenInterval)this.getConstraintSolvers()[0].getVariables()[i]).getEET(), ((AllenInterval)this.getConstraintSolvers()[0].getVariables()[i]).getLET());
			yLB = new Bounds(((AllenInterval)this.getConstraintSolvers()[1].getVariables()[i]).getEST(), ((AllenInterval)this.getConstraintSolvers()[1].getVariables()[i]).getLST());
			yUB = new Bounds(((AllenInterval)this.getConstraintSolvers()[1].getVariables()[i]).getEET(), ((AllenInterval)this.getConstraintSolvers()[1].getVariables()[i]).getLET());
			zLB = new Bounds(((AllenInterval)this.getConstraintSolvers()[2].getVariables()[i]).getEST(), ((AllenInterval)this.getConstraintSolvers()[2].getVariables()[i]).getLST());
			zUB = new Bounds(((AllenInterval)this.getConstraintSolvers()[2].getVariables()[i]).getEET(), ((AllenInterval)this.getConstraintSolvers()[2].getVariables()[i]).getLET());
			bbs.add(new BoundingBox(xLB, xUB, yLB, yUB, zLB, zUB));
		}
		return bbs.toArray(new BoundingBox[bbs.size()]);
	}
	
	public Vector<BoundingBox> extractBoundingBoxesFromSTPsByName(String name){
		
		Vector<BoundingBox> ret = new Vector<BoundingBox>();
		for (int i = 0; i < this.getConstraintSolvers()[0].getVariables().length; i++) {
			Bounds xLB, xUB, yLB, yUB, zLB, zUB;
			if(((RectangularCuboidRegion)this.getConstraintNetwork().getVariables()[i]).getName().compareTo(name) == 0){
				xLB = new Bounds(((AllenInterval)this.getConstraintSolvers()[0].getVariables()[i]).getEST(), ((AllenInterval)this.getConstraintSolvers()[0].getVariables()[i]).getLST());
				xUB = new Bounds(((AllenInterval)this.getConstraintSolvers()[0].getVariables()[i]).getEET(), ((AllenInterval)this.getConstraintSolvers()[0].getVariables()[i]).getLET());
				yLB = new Bounds(((AllenInterval)this.getConstraintSolvers()[1].getVariables()[i]).getEST(), ((AllenInterval)this.getConstraintSolvers()[1].getVariables()[i]).getLST());
				yUB = new Bounds(((AllenInterval)this.getConstraintSolvers()[1].getVariables()[i]).getEET(), ((AllenInterval)this.getConstraintSolvers()[1].getVariables()[i]).getLET());
				zLB = new Bounds(((AllenInterval)this.getConstraintSolvers()[2].getVariables()[i]).getEST(), ((AllenInterval)this.getConstraintSolvers()[2].getVariables()[i]).getLST());
				zUB = new Bounds(((AllenInterval)this.getConstraintSolvers()[2].getVariables()[i]).getEET(), ((AllenInterval)this.getConstraintSolvers()[2].getVariables()[i]).getLET());
				ret.add(new BoundingBox(xLB, xUB, yLB, yUB, zLB, zUB));
			}
		}
		return ret;
	}


}

package spatial.rectangleAlgebra;

import multi.allenInterval.AllenIntervalConstraint;

/**
 * This class augment the rectangle Algebra class with Quantified knowledge namely Duration of interval and bounded Allen
 * @author iran
 *
 */
public class AugmentedRectangleConstraint extends RectangleConstraint{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4291140350801794890L;
	private AllenIntervalConstraint boundedAllenX;
	private AllenIntervalConstraint boundedAllenY;

	

	public AllenIntervalConstraint getBoundedConstraintX() {
		return boundedAllenX;
	}
	
	public AllenIntervalConstraint getBoundedConstraintY() {
		return boundedAllenY;
	}
	
	
	
	public AugmentedRectangleConstraint(TwoDimensionsAllenConstraint... c) {
		super(c);
	}
	
	public AugmentedRectangleConstraint(AllenIntervalConstraint cx, AllenIntervalConstraint cy) {
		this.boundedAllenX = cx;
		this.boundedAllenY = cy;
	}
	
	
	
	
	

}

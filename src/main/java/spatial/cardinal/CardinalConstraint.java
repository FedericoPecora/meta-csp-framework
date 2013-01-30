package spatial.cardinal;

import java.util.Arrays;

import spatial.RCC.RCCConstraint.Type;

import framework.BinaryConstraint;
import framework.Constraint;

public class CardinalConstraint extends BinaryConstraint{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected Type[] types;
	public static enum Type {
		
		North, 
		West,
		South,
		East, 
		NorthEast,
		NorthWest,
		SouthEast,
		SouthWest,
		EQUAL,
		NO
	};
	
	//has to be completed
	public static Float[] CardinalRelationToMetricOrientation = {
		
		(float) -1.57, //North			
		(float) 0, //West
		(float) 1.57, //south
		(float) 3.14, //East
		(float) 0, //NorthEast
		(float) 0, //NorthWest
		(float) 0, //SouthEast
		(float) 0, //SouthWest
		(float) 0, //Equal
		(float) 0, //NO
	};

	
	@Override
	public String getEdgeLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEquivalent(Constraint c) {
		// TODO Auto-generated method stub
		return false;
	}

}

package org.metacsp.spatial.geometry;

import org.metacsp.framework.BinaryConstraint;
import org.metacsp.framework.Constraint;


public class GeometricConstraint extends BinaryConstraint{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8113896844860210411L;
	public static enum Type {DC, INSIDE};
	
	private Type type;
	
	public GeometricConstraint(Type type){
		this.type = type;
	}

	public Type getType(){
		return type;
	}
	
	@Override
	public String getEdgeLabel() {
		return this.type.toString();
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

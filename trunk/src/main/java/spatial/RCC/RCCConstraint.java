package spatial.RCC;

import java.util.Arrays;

import framework.BinaryConstraint;
import framework.Constraint;

public class RCCConstraint extends BinaryConstraint{

	//protected Type type;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1948378025485351843L;
	protected Type[] types;

	public RCCConstraint(Type... types) {
		this.types = types;
	}
	
	
	public Type[] getTypes() {
		return types;
	}

	
	public void setTypes(Type[] types) {
		this.types = types;
	}
	
//	public RCCConstraint(Type type){
//		
//		this.type = type;
//	}
	
	@Override
	public String getEdgeLabel() {
		// TODO Auto-generated method stub
		return Arrays.toString(this.types);
	}

	@Override
	public Object clone() {
		return new RCCConstraint(this.types);
	}

	@Override
	public boolean isEquivalent(Constraint c) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public String toString() {
		String ret = "[";
		for (int i = 0; i < types.length; i++) {
			ret +="(" + this.getFrom() + ") --" + this.types[i] + "--> (" + this.getTo() + ")"; 
		}
		ret += "]";
		return ret;
	}
	
	
	public static enum Type {
		
		DC, //DisConnected
		EC, //Externally Connected
		PO, //Partially Overlapping
		TPP, //Tangential Proper Part
		NTPP, //Non-Tangential Proper Part
		TPPI, //Inverse of Tangential Proper Part
		NTPPI, //inverse of Non-Tangential Proper Part
		EQ, //EQual
	};
	//Type.DC, Type.EC, Type.PO, Type.TPP, Type.NTPP, Type.TPPI, Type.NTPPI, Type.EQ
	public static Type[][][] transitionTable = {
		
		{
			{Type.DC, Type.EC, Type.PO, Type.TPP, Type.NTPP, Type.TPPI, Type.NTPPI, Type.EQ},
			{Type.DC, Type.EC, Type.PO, Type.TPP, Type.NTPP},
			{Type.DC, Type.EC, Type.PO, Type.TPP, Type.NTPP},
			{Type.DC, Type.EC, Type.PO, Type.TPP, Type.NTPP},
			{Type.DC, Type.EC, Type.PO, Type.TPP, Type.NTPP},
			{Type.DC},
			{Type.DC},
			{Type.DC}
			
		},
		
		{
			{Type.DC, Type.EC, Type.PO, Type.TPPI, Type.NTPPI},
			{Type.DC, Type.EC, Type.PO, Type.TPP, Type.TPPI, Type.EQ},
			{Type.DC, Type.EC, Type.PO, Type.TPP, Type.NTPP},
			{Type.EC, Type.PO, Type.TPP, Type.NTPP},
			{Type.PO, Type.TPP, Type.NTPP},
			{Type.DC, Type.EC},
			{Type.DC},
			{Type.EC}
			
		},
		
		{
			{Type.DC, Type.EC, Type.PO, Type.TPPI, Type.NTPPI},
			{Type.DC, Type.EC, Type.PO, Type.TPPI, Type.NTPPI},
			{Type.DC, Type.EC, Type.PO, Type.TPP, Type.NTPP, Type.TPPI, Type.NTPPI, Type.EQ},
			{Type.PO, Type.TPP, Type.NTPP},
			{Type.PO, Type.TPP, Type.NTPP},
			{Type.DC, Type.EC, Type.PO, Type.TPPI, Type.NTPPI},
			{Type.DC, Type.EC, Type.PO, Type.TPPI, Type.NTPPI},
			{Type.PO}
			
		},
		
		{
			{Type.DC},
			{Type.DC, Type.EC},
			{Type.DC, Type.EC, Type.PO, Type.TPP, Type.NTPP},
			{Type.TPP, Type.NTPP},
			{Type.NTPP},
			{Type.DC, Type.EC, Type.PO, Type.TPPI, Type.NTPPI, Type.EQ},
			{Type.DC, Type.EC, Type.PO, Type.TPPI, Type.NTPPI},
			{Type.TPP}
			
		},
		
		{
			{Type.DC},
			{Type.DC},
			{Type.DC, Type.EC, Type.PO, Type.TPP, Type.NTPP},
			{Type.NTPP},
			{Type.NTPP},
			{Type.DC, Type.EC, Type.PO, Type.TPP, Type.NTPP},
			{Type.DC, Type.EC, Type.PO, Type.TPP, Type.NTPP, Type.TPPI, Type.NTPPI, Type.EQ},
			{Type.NTPP}
		},
		
		{
			{Type.DC, Type.EC, Type.PO, Type.TPPI, Type.NTPPI},
			{Type.EC, Type.PO, Type.TPPI, Type.NTPPI},
			{Type.PO, Type.TPPI, Type.NTPPI},
			{Type.PO, Type.EQ, Type.TPP, Type.TPPI},
			{Type.PO, Type.TPP, Type.NTPP},
			{Type.TPPI, Type.NTPPI},
			{Type.NTPPI},
			{Type.TPPI}
		},
		
		{
			{Type.DC, Type.EC, Type.PO, Type.TPPI, Type.NTPPI},
			{Type.PO, Type.TPPI, Type.NTPPI},
			{Type.PO, Type.TPPI, Type.NTPPI},
			{Type.PO, Type.TPPI, Type.NTPPI},
			{Type.PO, Type.TPPI, Type.TPP, Type.NTPP, Type.NTPPI, Type.EQ},
			{Type.NTPPI},
			{Type.NTPPI},
			{Type.NTPPI}
		},
		
		{
			{Type.DC},
			{Type.EC},
			{Type.PO},
			{Type.TPP},
			{Type.NTPP},
			{Type.TPPI},
			{Type.NTPPI},
			{Type.EQ}
		},


	};//3 by 3 composition Table

	
	public static Type getInverseRelation(Type t)
	{		
		if (t.equals(Type.TPP))
			return Type.TPPI;
		else if (t.equals(Type.NTPP))
			return Type.NTPPI;
		else
			return t;
	}
	

}

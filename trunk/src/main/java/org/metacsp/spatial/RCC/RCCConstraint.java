/*******************************************************************************
 * Copyright (c) 2010-2013 Federico Pecora <federico.pecora@oru.se>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package org.metacsp.spatial.RCC;

import java.util.Arrays;

import org.metacsp.framework.BinaryConstraint;
import org.metacsp.framework.Constraint;

/**
 * Implementation of RCC-8 (Region Connection Calculus) constraints.
 * @author Masoumeh Mansouri
 *
 */
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
		
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A DISCONNECTED B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp<img src=../../../../img/rcc8-dc.png> 
		 */
		DC, //DisConnected

		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A EXTERNALLY-CONNECTED B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp<img src=../../../../img/rcc8-ec.png> 
		 */
		EC, //Externally Connected
		
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A PARTIALLY-OVERLAPPING B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp<img src=../../../../img/rcc8-po.png> 
		 */
		PO, //Partially Overlapping

		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A TANGENTIAL-PROPER-PART B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp<img src=../../../../img/rcc8-tpp.png> 
		 */
		TPP, //Tangential Proper Part

		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A NON-TANGENTIAL-PROPER-PART B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp<img src=../../../../img/rcc8-ntpp.png> 
		 */
		NTPP, //Non-Tangential Proper Part
		
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A INVERSE-OF-TANGENTIAL-PROPER-PART B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp<img src=../../../../img/rcc8-tppi.png> 
		 */
		TPPI, //Inverse of Tangential Proper Part
		
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A INVERSE-OF-NON-TANGENTIAL-PROPER-PART B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp<img src=../../../../img/rcc8-ntppi.png> 
		 */
		NTPPI, //inverse of Non-Tangential Proper Part
		
		/**
		 * <br>&nbsp;&nbsp;&nbsp;Semantics: A EQUALS B<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp<img src=../../../../img/rcc8-e.png> 
		 */
		EQ //EQual
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

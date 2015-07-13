package org.metacsp.multi.spatial.DE9IM;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.metacsp.framework.BinaryConstraint;
import org.metacsp.framework.Constraint;


import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.IntersectionMatrix;

public class DE9IMRelation extends BinaryConstraint {
	
	private static final long serialVersionUID = 5511066094460985805L;

	public static enum Type {Contains, Within, Covers, CoveredBy, Intersects, Disjoint, Crosses, Overlaps, Touches, Equals};

	//Subset for polygons, equivalent to RCC8 (Cohn, 2007)
	private static HashSet<String> RCC8Types = new HashSet<String>(Arrays.asList("Contains", "Within", "Covers", "CoveredBy", "Disjoint", "Overlaps", "Touches", "Equals"));

	private Type[] types = null;
	
	public static Type[] getRelation(GeometricShapeVariable gv1, GeometricShapeVariable gv2, boolean rcc8Relations) {
		ArrayList<Type> ret = new ArrayList<Type>();
		Geometry g1 = ((GeometricShapeDomain)gv1.getDomain()).getGeometry();
		Geometry g2 = ((GeometricShapeDomain)gv2.getDomain()).getGeometry();
		
		for (Type t : Type.values()) {
			try {
				if (rcc8Relations && !RCC8Types.contains(t.name())) continue;
				String methodName = t.name().substring(0, 1).toLowerCase() + t.name().substring(1);
				Method m = Geometry.class.getMethod(methodName, Geometry.class);
				if ((Boolean)m.invoke(g1, g2)) {
					boolean skip = false;
					if (t.equals(Type.Covers) || t.equals(Type.CoveredBy)) {
						IntersectionMatrix mat = g1.relate(g2);
						if (mat.get(1,1) == -1) skip = true;
					}
					else if (t.equals(Type.Contains) || t.equals(Type.Within)) {
						IntersectionMatrix mat = g1.relate(g2);
						if (mat.get(1,1) == 1) skip = true;
					}
					if (!skip) ret.add(t);
				}
			}
			catch (NoSuchMethodException e) { e.printStackTrace(); }
			catch (SecurityException e) { e.printStackTrace(); }
			catch (IllegalAccessException e) { e.printStackTrace(); }
			catch (IllegalArgumentException e) { e.printStackTrace(); }
			catch (InvocationTargetException e) { e.printStackTrace(); }
		}
		return ret.toArray(new Type[ret.size()]);
	}
	
	//cannot make a DE9IM relation without at least one type
	protected DE9IMRelation() {}
	
	public DE9IMRelation(Type ... types) {
		this.types = types;
	}
	
	public Type[] getTypes() {
		return types;
	}
	
	@Override
	public String getEdgeLabel() {
		return Arrays.toString(types);
	}

	@Override
	public Object clone() {
		DE9IMRelation ret = new DE9IMRelation(types);
		ret.setFrom(this.getFrom());
		ret.setTo(this.getTo());
		return ret;
	}

	@Override
	public boolean isEquivalent(Constraint c) {
		if (!(c instanceof DE9IMRelation)) return false;
		DE9IMRelation r = (DE9IMRelation)c;
		HashSet<Type> thisTypes = new HashSet<DE9IMRelation.Type>();
		for (Type t : this.getTypes()) thisTypes.add(t);
		HashSet<Type> thatTypes = new HashSet<DE9IMRelation.Type>();
		for (Type t : r.getTypes()) thatTypes.add(t);
		return thisTypes.size() == thatTypes.size() && thisTypes.containsAll(thatTypes);
	}

}

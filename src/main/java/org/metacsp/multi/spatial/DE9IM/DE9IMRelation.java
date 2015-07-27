package org.metacsp.multi.spatial.DE9IM;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.metacsp.framework.BinaryConstraint;
import org.metacsp.framework.Constraint;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.IntersectionMatrix;

/**
 * This class represents spatial relations between geometric shapes (variables of type {@link GeometricShapeVariable}).
 * These shapes can be points, strings of segments, or polygons. The relations constitute a topological mode called
 * the Dimensionally Extended nine-Intersection Model (DE-9IM) (see (Clementini et al., 1993) and {@link https://en.wikipedia.org/wiki/DE-9IM}).
 * If the shapes are all polygons, then a Jointly Exclusive and Pairwise Disjoint (JEPD) subset of these relations is
 * equivalent to RCC8 (Cohn et al., 2007).
 * 
 * @author Federico Pecora
 *
 */
public class DE9IMRelation extends BinaryConstraint {
	
	private static final long serialVersionUID = 5511066094460985805L;

	/**
	 * The 10 meaningful relations in DE-9IM. The subset
	 * { {@value Type#Contains}, {@value Type#Within}, {@value Type#Covers}, {@value Type#CoveredBy}, {@value Type#Disjoint}, {@value Type#Overlaps}, {@value Type#Touches}, {@value Type#Equals} }
	 * is Jointly Exclusive and Pairwise Disjoint (JEPD) and equivalent to RCC8 (Cohn et al., 2007).
	 */
	public static enum Type {Contains, Within, Covers, CoveredBy, Intersects, Disjoint, Crosses, Overlaps, Touches, Equals};

	//Subset for polygons, equivalent to RCC8 (Cohn, 2007)
	private static HashSet<String> RCC8Types = new HashSet<String>(Arrays.asList("Contains", "Within", "Covers", "CoveredBy", "Disjoint", "Overlaps", "Touches", "Equals"));

	private Type[] types = null;

	/**
	 * Assess whether this relation is of a given {@link Type}.
	 * @param relationType The {@link Type} to compare against.
	 * @return <code>true</code> iff this relation is of the given {@link Type}.
	 */
	public boolean isRelation(DE9IMRelation.Type relationType) {
		for (Type t : types) if (t.equals(relationType)) return true;
		return false;
	}
	
	/**
	 * Get the DE-9IM relation(s) existing between two {@link GeometricShapeVariable}s.
	 * @param gv1 The first {@link GeometricShapeVariable} (the source of the directed edge).
	 * @param gv2 The second {@link GeometricShapeVariable} (the destination of the directed edge).
	 * @return The DE-9IM relation(s) existing between the two given {@link GeometricShapeVariable}s.
	 */
	public static Type[] getRelations(GeometricShapeVariable gv1, GeometricShapeVariable gv2) {
		return getRelations(gv1, gv2, false);		
	}
	
	/**
	 * Get the RCC8 relation(s) existing between two {@link GeometricShapeVariable}s.
	 * @param gv1 The first {@link GeometricShapeVariable} (the source of the directed edge).
	 * @param gv2 The second {@link GeometricShapeVariable} (the destination of the directed edge).
	 * @return The RCC8 relation(s) existing between the two given {@link GeometricShapeVariable}s.
	 */
	public static Type[] getRCC8Relations(GeometricShapeVariable gv1, GeometricShapeVariable gv2) {
		return getRelations(gv1, gv2, true);		
	}
	
	/**
	 * Find out whether a relation is a {@link DE9IMRelation} belongs to the RCC8 subset.
	 * @param t The relation to test
	 * @return <code>true</code> iff the given {@link DE9IMRelation} belongs to the RCC8 subset.
	 */
	public static boolean isRCC8Relation(DE9IMRelation.Type t) {
		return RCC8Types.contains(t.name());
	}

	private static Type[] getRelations(GeometricShapeVariable gv1, GeometricShapeVariable gv2, boolean rcc8Relations) {
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
		if (rcc8Relations && ret.contains(Type.Equals)) return new Type[] {Type.Equals};
		return ret.toArray(new Type[ret.size()]);
	}
	
	//cannot make a DE9IM relation without at least one type
	protected DE9IMRelation() {}
	
	/**
	 * Create a new DE-9IM relation with given types.
	 * @param types The type(s) of the new relation.
	 */
	public DE9IMRelation(Type ... types) {
		this.types = types;
	}
	
	/**
	 * Get the types of this DE-9IM relation.
	 * @return The types of this DE-9IM relation.
	 */
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

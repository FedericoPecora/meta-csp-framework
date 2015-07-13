package org.metacsp.multi.spatial.DE9IM;

import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Domain;
import org.metacsp.framework.Variable;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Represents a variable that can be reasoned upon by the {@link DE9IMRelationSolver}. A {@link GeometricShapeVariable} should be provided a domain
 * representing one of the following geometric entities: point (provided by class {@link PointDomain}), line string (provided by class {@link LineStringDomain}),
 * and polygon (provided by class {@link PolygonalDomain}).   
 * 
 * @author Federico Pecora
 *
 */
public class GeometricShapeVariable extends Variable {
	
	private static final long serialVersionUID = 8683978818915583902L;

	private GeometricShapeDomain dom = null;
	
	protected GeometricShapeVariable(ConstraintSolver cs, int id) {
		super(cs, id);
		setDomain(new PolygonalDomain(this));
	}
	
	/**
	 * Returns the class used to represent the domain of this variable.
	 * @return The class used to represent the domain of this variable.
	 */
	public Class<?> getShapeType() {
		return ((GeometricShapeDomain)this.getDomain()).getShapeType();
	}
	
	@Override
	public int compareTo(Variable o) {
		return o.getID() - this.getID();
	}

	@Override
	public Domain getDomain() {
		return this.dom;
	}

	@Override
	public void setDomain(Domain d) {
		this.dom = (GeometricShapeDomain)d;
	}

	@Override
	public String toString() {
		return ""+this.getID();
	}

}

package org.metacsp.multi.spatial.DE9IM;

import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Domain;
import org.metacsp.framework.Variable;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class GeometricShapeVariable extends Variable {
	
	private static final long serialVersionUID = 8683978818915583902L;

	private GeometricShapeDomain dom = null;
	
	protected GeometricShapeVariable(ConstraintSolver cs, int id) {
		super(cs, id);
		setDomain(new PolygonalDomain(this));
	}
	
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

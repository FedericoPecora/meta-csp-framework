package org.metacsp.multi.spatial.DE9IM;

import org.metacsp.framework.Domain;
import org.metacsp.framework.Variable;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

public abstract class GeometricShapeDomain extends Domain {

	protected Coordinate[] coordinates = null;
	protected Geometry geom = null;
	
	private static final long serialVersionUID = -6193460915334324151L;
	
	protected GeometricShapeDomain(Variable v) {
		super(v);
		this.coordinates = null;
		updateGeometry();
	}

	protected GeometricShapeDomain(Variable v, Coordinate ... coord) {
		super(v);
		this.coordinates = coord;
		updateGeometry();
	}
	
	public Geometry getGeometry() {
		return geom;
	}
	
	protected abstract void updateGeometry();
	
	public void setCoordinates(Coordinate ... coord) {
		this.coordinates = coord;
	}
	
	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

}

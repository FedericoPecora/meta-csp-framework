package org.metacsp.multi.spatial.DE9IM;

import org.metacsp.framework.Domain;
import org.metacsp.framework.Variable;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Represents the domain of a {@link GeometricShapeVariable}. This is an abstract class that is used to refer to the generic
 * type of three types of domains: point (provided by implementing class {@link PointDomain}), line string (provided by implementing class {@link LineStringDomain}),
 * and polygon (provided by implementing class {@link PolygonalDomain}).   
 * 
 * @author Federico Pecora
 *
 */
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
	
	public boolean isInstantiated() {
		return this.coordinates != null && this.coordinates.length > 0;
	}

	/**
	 * Returns the {@link Geometry} used to represent this domain internally. Methods provided by the JTS Topology Suite (see {@link http://tsusiatsoftware.net/}) can
	 * be used on these objects.
	 * @return The {@link Geometry} used to represent this domain internally.
	 */
	public Geometry getGeometry() {
		return geom;
	}
	
	/**
	 * Returns the class used to represent this domain. Methods provided by the JTS Topology Suite (see {@link http://tsusiatsoftware.net/}) can
	 * be used on these objects.
	 * @return The class used to represent this domain.
	 */
	public Class<?> getShapeType() {
		return this.getClass();
	}
	
	protected abstract void updateGeometry();
	
	/**
	 * Set the coordinates of this domain.
	 * @param coord The coordinates that should be used to create this domain.
	 */
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
		return this.geom.getGeometryType() + " (" + ((this.coordinates==null) ? ("no coordinates") : (this.coordinates.length + " coordinates")) + ")";
	}

}

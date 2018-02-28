package org.metacsp.multi.spatial.DE9IM;

import org.metacsp.framework.Variable;
import org.metacsp.multi.spatial.SpatialDomain;

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
public abstract class GeometricShapeDomain extends SpatialDomain {

	protected Geometry geom = null;
	
	private static final long serialVersionUID = -6193460915332322151L;
	
	protected GeometricShapeDomain(Variable v) {
		super(v);
	}

	protected GeometricShapeDomain(Variable v, Coordinate ... coord) {
		super(v,coord);
	}
	
	/**
	 * Returns the class used to represent this domain. Methods provided by the JTS Topology Suite (see <a href="http://tsusiatsoftware.net/">http://tsusiatsoftware.net</a>) can
	 * be used on these objects.
	 * @return The class used to represent this domain.
	 */
	public Class<?> getShapeType() {
		return this.getClass();
	}
	
	/**
	 * Returns the {@link Geometry} used to represent this domain internally. Methods provided by the JTS Topology Suite (see <a href="http://tsusiatsoftware.net/">http://tsusiatsoftware.net</a>) can
	 * be used on these objects.
	 * @return The {@link Geometry} used to represent this domain internally.
	 */
	public Geometry getGeometry() {
		return this.geom;
	}
	
	protected abstract void updateGeometry();
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.geom.getGeometryType() + " (" + ((this.coordinates==null) ? ("no coordinates") : (this.coordinates.length + " coordinates")) + ")";
	}

}

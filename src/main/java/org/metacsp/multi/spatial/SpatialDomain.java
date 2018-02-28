package org.metacsp.multi.spatial;

import org.metacsp.framework.Domain;
import org.metacsp.framework.Variable;

import com.vividsolutions.jts.geom.Coordinate;

public abstract class SpatialDomain extends Domain {
	protected Coordinate[] coordinates = null;
	
	private static final long serialVersionUID = -6193460915334324151L;
	
	protected SpatialDomain(Variable v) {
		super(v);
		this.coordinates = null;
	}

	protected SpatialDomain(Variable v, Coordinate ... coord) {
		super(v);
		this.coordinates = coord;
	}
	
	public boolean isInstantiated() {
		return this.coordinates != null && this.coordinates.length > 0;
	}

//	/**
//	 * Returns the {@link Geometry} used to represent this domain internally. Methods provided by the JTS Topology Suite (see <a href="http://tsusiatsoftware.net/">http://tsusiatsoftware.net</a>) can
//	 * be used on these objects.
//	 * @return The {@link Geometry} used to represent this domain internally.
//	 */
//	public Geometry getGeometry() {
//		return geom;
//	}
	
//	/**
//	 * Returns the class used to represent this domain. Methods provided by the JTS Topology Suite (see <a href="http://tsusiatsoftware.net/">http://tsusiatsoftware.net</a>) can
//	 * be used on these objects.
//	 * @return The class used to represent this domain.
//	 */
//	public Class<?> getShapeType() {
//		return this.getClass();
//	}
	
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

}

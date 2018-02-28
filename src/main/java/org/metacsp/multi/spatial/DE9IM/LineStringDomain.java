package org.metacsp.multi.spatial.DE9IM;

import org.metacsp.framework.Variable;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Represents line string domains for {@link GeometricShapeVariable}s. 
 * 
 * @author Federico Pecora
 *
 */
public class LineStringDomain extends GeometricShapeDomain {

	private static final long serialVersionUID = 1543675650668270396L;

	protected LineStringDomain(Variable v) {
		super(v);
		updateGeometry();
	}
	
	/**
	 * Create a {@link LineStringDomain} for a variable given a list of coordinates.
	 * @param v The variable of which this object represents the domain.
	 * @param coord The coordinates to be used for creating this {@link LineStringDomain}.
	 */
	public LineStringDomain(Variable v, Coordinate[] coord) {
		super(v, coord);
		updateGeometry();
	}

	public Coordinate[] getCoordiantes() {
		return this.getGeometry().getCoordinates();
	}
	
	@Override
	protected void updateGeometry() {
		if (this.coordinates == null) this.geom = new GeometryFactory().createLineString(new Coordinate[]{});			
		else this.geom = new GeometryFactory().createLineString(this.coordinates);
	}

}

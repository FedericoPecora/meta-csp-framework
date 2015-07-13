package org.metacsp.multi.spatial.DE9IM;

import org.metacsp.framework.Variable;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;

/**
 * Represents point domains for {@link GeometricShapeVariable}s. 
 * 
 * @author Federico Pecora
 *
 */
public class PointDomain extends GeometricShapeDomain {

	private static final long serialVersionUID = 1543675650668270396L;

	protected PointDomain(Variable v) {
		super(v);
	}
	
	/**
	 * Create a {@link PointDomain} for a variable given a coordinate.
	 * @param v The variable of which this object represents the domain.
	 * @param coord The coordinate to be used for creating this {@link PointDomain}.
	 */
	public PointDomain(Variable v, Coordinate coord) {
		super(v, coord);
	}

	@Override
	protected void updateGeometry() {
		if (this.coordinates == null) this.geom = new GeometryFactory().createPoint(new Coordinate(null));			
		else this.geom = new GeometryFactory().createPoint(this.coordinates[0]);						
	}

}

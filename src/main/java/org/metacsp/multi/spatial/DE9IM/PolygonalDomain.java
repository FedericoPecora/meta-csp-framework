package org.metacsp.multi.spatial.DE9IM;

import org.metacsp.framework.Variable;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;

/**
 * Represents polygonal domains for {@link GeometricShapeVariable}s.
 * 
 * @author Federico Pecora
 *
 */
public class PolygonalDomain extends GeometricShapeDomain {

	private static final long serialVersionUID = 1543675650668270396L;

	protected PolygonalDomain(Variable v) {
		super(v);
	}
	
	/**
	 * Create a {@link PolygonalDomain} for a variable given a coordinate.
	 * @param v The variable of which this object represents the domain.
	 * @param coord The coordinates to be used for creating this {@link PolygonalDomain}.
	 */
	public PolygonalDomain(Variable v, Coordinate[] coord) {
		super(v, coord);
	}

	@Override
	protected void updateGeometry() {
		if (this.coordinates == null) {
			LinearRing nullLR = new LinearRing(null, new GeometryFactory());
			this.geom = new GeometryFactory().createPolygon(nullLR);			
		}
		else {
			Coordinate[] newCoords = new Coordinate[coordinates.length+1];
			for (int i = 0; i < coordinates.length; i++) {
				newCoords[i] = coordinates[i];
			}
			newCoords[coordinates.length] = this.coordinates[0];
			this.geom = new GeometryFactory().createPolygon(newCoords);						
		}
	}

}

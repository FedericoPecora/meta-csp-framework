package org.metacsp.multi.spatial.DE9IM;

import org.metacsp.framework.Variable;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;

public class PolygonalDomain extends GeometricShapeDomain {

	private static final long serialVersionUID = 1543675650668270396L;

	protected PolygonalDomain(Variable v) {
		super(v);
	}
	
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

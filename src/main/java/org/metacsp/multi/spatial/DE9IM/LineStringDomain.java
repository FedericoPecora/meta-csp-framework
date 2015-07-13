package org.metacsp.multi.spatial.DE9IM;

import org.metacsp.framework.Variable;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;

public class LineStringDomain extends GeometricShapeDomain {

	private static final long serialVersionUID = 1543675650668270396L;

	protected LineStringDomain(Variable v) {
		super(v);
	}
	
	public LineStringDomain(Variable v, Coordinate[] coord) {
		super(v, coord);
	}

	@Override
	protected void updateGeometry() {
		if (this.coordinates == null) this.geom = new GeometryFactory().createLineString(new Coordinate[]{});			
		else this.geom = new GeometryFactory().createLineString(this.coordinates);
	}

}

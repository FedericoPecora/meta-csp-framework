package org.metacsp.multi.spatial.DE9IM;

import java.util.logging.Logger;

import org.metacsp.framework.Variable;
import org.metacsp.utility.logging.MetaCSPLogging;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.TopologyException;

/**
 * Represents polygonal domains for {@link GeometricShapeVariable}s.
 * 
 * @author Federico Pecora
 *
 */
public class PolygonalDomain extends GeometricShapeDomain {

	private static final long serialVersionUID = 1543675650668270396L;
	private transient Logger metaCSPLogger = MetaCSPLogging.getLogger(this.getClass());

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
	
	/**
	 * Check whether this polygon contains a given point.
	 * @param point The coordinates specifying the point to check.
	 * @return <code>true</code> this {@link PolygonalDomain} contains this point.
	 */
	public boolean containsPoint(Coordinate point) {
		Point p = new GeometryFactory().createPoint(point);	
		return this.getGeometry().contains(p);
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
			if (!this.geom.isValid()) {
				try { 
					this.geom = this.geom.symDifference(this.geom.getBoundary());
				}
				catch(TopologyException e) {
					metaCSPLogger.info("Trying to fix GeometricShapeVariable " + this.getVariable().getID());
					this.geom = this.geom.buffer(0.1);
					if (!this.geom.isValid()) {
						metaCSPLogger.severe("... giving up!");
						throw e;
					}
				}
			}
		}
	}

}

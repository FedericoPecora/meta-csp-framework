package org.metacsp.multi.spatioTemporal.paths;



import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.metacsp.utility.logging.MetaCSPLogging;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Represents the pose of a robot in 2D.
 * 
 * @author Federico Pecora
 *
 */
public class Pose implements Serializable {
	
	private static final long serialVersionUID = -6109720311463668670L;
	private double x, y, theta;
		
	/**
	 * Create a new {@link Pose} given the position and orientation.
	 * @param x X coordinate of the {@link Pose}.
	 * @param y Y coordinate of the {@link Pose}.
	 * @param theta Orientation of the {@link Pose}.
	 */
	public Pose(double x, double y, double theta) {
		this.x = x;
		this.y = y;
		this.theta = theta;
	}
	
	/**
	 * Get the X position of this {@link Pose}.
	 * @return The X position of this {@link Pose}.
	 */
	public double getX() {
		return x;
	}

	/**
	 * Get the Y position of this {@link Pose}.
	 * @return The Y position of this {@link Pose}.
	 */
	public double getY() {
		return y;
	}
	
	/**
	 * Get the orientation of this {@link Pose}.
	 * @return The orientation of this {@link Pose}.
	 */
	public double getTheta() {
		return theta;
	}
	
	public Coordinate getPosition() {
		return new Coordinate(this.getX(), this.getY());
	}
	
	public double distanceTo(Pose p) {
		return p.getPosition().distance(this.getPosition());
	}
	
	public static double lerp(double a, double b, double ratio) {
	    return (a * (1.0 - ratio)) + (b * ratio);
	}
	
	public static double lerpDegrees(double a, double b, double ratio) {
        double difference = Math.abs(b - a);
        if (difference > Math.PI) {
        	if (b > a) a += 2*Math.PI;
            else b += 2*Math.PI;
        }
        double value = (a * (1.0 - ratio)) + (b * ratio);
        double rangeZero = 2*Math.PI;
        if (value >= 0 && value <= 2*Math.PI) return value;
        return (value % rangeZero);
    }
	
	/**
	 * Computes the {@link Pose} between this {@link Pose} and a given {@link Pose} via bilinear interpolation.
	 * @param p2 The second {@link Pose} used for interpolation.
	 * @param ratio Parameter in [0,1] used for bilinear interpolation.
	 * @return The {@link Pose} between this {@link Pose} and a given {@link Pose} via bilinear interpolation.
	 */
	public Pose interpolate(Pose p2, double ratio) {
		double newX = lerp(getX(),p2.getX(),ratio);
		double newY = lerp(getY(),p2.getY(),ratio);
		double newTheta = lerpDegrees(getTheta(),p2.getTheta(),ratio);
		return new Pose(newX,newY,newTheta);
	}

	public String toString() {
		return "(" + MetaCSPLogging.printDouble(this.getX(),4) + ", " + MetaCSPLogging.printDouble(this.getY(),4) + ", " + MetaCSPLogging.printDouble(this.getTheta(),4) + ")";
	}

}

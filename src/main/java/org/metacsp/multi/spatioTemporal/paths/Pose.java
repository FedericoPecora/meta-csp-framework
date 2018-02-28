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
	//private double x, y, theta;
	private double x, y, z, roll, pitch, yaw = Double.NaN;
	
	
	/**
	 * Create a new {@link Pose} given the position and orientations.
	 * @param x X coordinate of the {@link Pose}.
	 * @param y Y coordinate of the {@link Pose}.
	 * @param z Z coordinate of the {@link Pose}.
	 * @param roll Orientation of the {@link Pose} around the x-axis.
	 * @param pitch Orientation of the {@link Pose} around the y-axis.
	 * @param yaw Orientation of the {@link Pose} around the z-axis.
	 */
	public Pose(double x, double y, double z, double roll, double pitch, double yaw) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.roll = roll;
		this.pitch = pitch;
		this.yaw = yaw;
	}
	
	/**
	 * Create a new {@link Pose} given the position and orientation.
	 * @param x X coordinate of the {@link Pose}.
	 * @param y Y coordinate of the {@link Pose}.
	 * @param theta Orientation of the {@link Pose}.
	 */
	public Pose(double x, double y, double theta) {
		this.x = x;
		this.y = y;
		this.yaw = theta;
		this.z = Double.NaN;
		this.roll = Double.NaN;
		this.pitch = Double.NaN;
		//this.theta = theta;
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
	 * Get the Z position of this {@link Pose}.
	 * @return The Z position of this {@link Pose}.
	 */
	public double getZ() {
		return z;
	}
	
	/**
	 * Get the orientation of this {@link Pose}. This is the same as {@link #getYaw()}.
	 * @return The orientation of this {@link Pose}.
	 */
	public double getTheta() {
		return this.yaw;
		//return theta;
	}
	
	/**
	 * Get the roll (x-axis rotation) position of this {@link Pose}.
	 * @return The roll (x-axis rotation) position of this {@link Pose}.
	 */
	public double getRoll() {
		return this.roll;
	}
	
	/**
	 * Get the pitch (y-axis rotation) position of this {@link Pose}.
	 * @return The pitch (y-axis rotation) position of this {@link Pose}.
	 */
	public double getPitch() {
		return this.pitch;
	}
	
	/**
	 * Get the yaw (z-axis rotation) position of this {@link Pose}. This is the same as {@link #getTheta()}.
	 * @return The yaw (z-axis rotation) position of this {@link Pose}.
	 */
	public double getYaw() {
		return this.yaw;
	}
	
	/**
	 * Get the position of this {@link Pose}.
	 * @return The position of this pose.
	 */
	public Coordinate getPosition() {
		if (Double.isNaN(this.z)) return new Coordinate(this.getX(), this.getY());
		return new Coordinate(this.getX(), this.getY(), this.getZ());
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
		if (Double.isNaN(this.z)) {
			double newTheta = lerpDegrees(getTheta(),p2.getTheta(),ratio);
			return new Pose(newX,newY,newTheta);
		}
		double newZ = lerp(getZ(),p2.getZ(),ratio);
		double newRoll = lerpDegrees(getRoll(),p2.getRoll(),ratio);
		double newPitch = lerpDegrees(getPitch(),p2.getPitch(),ratio);
		double newYaw = lerpDegrees(getYaw(),p2.getYaw(),ratio);
		return new Pose(newX,newY,newZ,newRoll,newPitch,newYaw);
	}

	public String toString() {
		if (Double.isNaN(this.z)) return "(" + MetaCSPLogging.printDouble(this.getX(),4) + ", " + MetaCSPLogging.printDouble(this.getY(),4) + ", " + MetaCSPLogging.printDouble(this.getTheta(),4) + ")";
		return "(" + MetaCSPLogging.printDouble(this.getX(),4) + ", " + MetaCSPLogging.printDouble(this.getY(),4) + ", " + MetaCSPLogging.printDouble(this.getZ(),4) + ", " + MetaCSPLogging.printDouble(this.getRoll(),4) + ", " + MetaCSPLogging.printDouble(this.getPitch(),4) + ", " + MetaCSPLogging.printDouble(this.getYaw(),4) + ")";
	}

}

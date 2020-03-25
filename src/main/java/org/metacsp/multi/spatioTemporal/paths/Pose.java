package org.metacsp.multi.spatioTemporal.paths;

import java.io.Serializable;

import org.metacsp.utility.logging.MetaCSPLogging;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Represents the pose of a robot in 2D.
 * 
 * @author Federico Pecora
 *
 */
public class Pose 
	implements Serializable	 {
	
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
	
	/*public boolean equals(Pose other) {
		if (other == null) return false;
		boolean ret = this.x == other.x && this.y == other.y && this.yaw == other.yaw;
		boolean check = this.roll != Double.NaN && this.pitch != Double.NaN && this.z != Double.NaN &&
				other.roll != Double.NaN && other.pitch != Double.NaN && other.z != Double.NaN;
		if (check) ret = ret && check;
		return ret;
	}*/

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.isNaN(this.pitch) ? 0 : Double.doubleToLongBits(pitch);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.isNaN(this.roll) ? 0 : Double.doubleToLongBits(roll);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(yaw);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.isNaN(this.z) ? 0 : Double.doubleToLongBits(z);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
	
	public boolean isPose2D() {
		return Double.isNaN(this.roll) || Double.isNaN(this.pitch) || Double.isNaN(this.z);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pose other = (Pose) obj;
		
		if (this.isPose2D() && !other.isPose2D() || !this.isPose2D() && other.isPose2D()) 
			throw new Error("Invalid comparison between a 2D pose and a 3D one.");
			//throw new ArithmeticException("Invalid comparison between a 2D pose and a 3D one."); //for debugging
		
		//Compare the two 2D poses
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		if (Double.doubleToLongBits(yaw) != Double.doubleToLongBits(other.yaw))
			return false;
		if (this.isPose2D() && other.isPose2D()) return true;
		
		//compare the two 3D poses.
		if (Double.isNaN(this.roll) || Double.isNaN(this.pitch) || Double.isNaN(this.z) ||
				Double.isNaN(other.roll) || Double.isNaN(other.pitch) || Double.isNaN(other.z)) 
			throw new Error("Invalid 3D poses.");
			//throw new ArithmeticException("Invalid 3D poses."); //for debugging
		
		if (Double.doubleToLongBits(z) != Double.doubleToLongBits(other.z))
			return false;
		if (Double.doubleToLongBits(pitch) != Double.doubleToLongBits(other.pitch))
			return false;
		if (Double.doubleToLongBits(roll) != Double.doubleToLongBits(other.roll))
			return false;
		return true;
	}
}

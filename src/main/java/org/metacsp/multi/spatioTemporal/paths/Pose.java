package org.metacsp.multi.spatioTemporal.paths;

/**
 * Represents the pose of a robot in 2D.
 * 
 * @author Federico Pecora
 *
 */
public class Pose {
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
	

}

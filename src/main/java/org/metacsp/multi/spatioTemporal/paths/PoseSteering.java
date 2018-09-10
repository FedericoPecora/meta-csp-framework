package org.metacsp.multi.spatioTemporal.paths;

import java.io.Serializable;

/**
 * Represents a pose with a steering angle.
 * 
 * @author Federico Pecora
 *
 */
public class PoseSteering implements Serializable {

	private static final long serialVersionUID = -8099332582394979566L;
	private Pose pose;
	private double steering;
	
	
	/**
	 * Create a {@link PoseSteering} given a position, orientation, and a steering angle.
	 * @param x X coordinate of the {@link Pose}.
	 * @param y Y coordinate of the {@link Pose}.
	 * @param z Z coordinate of the {@link Pose}.
	 * @param roll Orientation of the {@link Pose} around the x-axis.
	 * @param pitch Orientation of the {@link Pose} around the y-axis.
	 * @param yaw Orientation of the {@link Pose} around the z-axis.
	 * @param steering Steering angle of this {@link PoseSteering}.
	 */
	public PoseSteering(double x, double y, double z, double roll, double pitch, double yaw, double steering) {
		this.pose = new Pose(x,y,z,roll,pitch,yaw);
		this.steering = steering;
	}
	
	/**
	 * Create a {@link PoseSteering} given a position, orientation, and a steering angle.
	 * @param x X coordinate of the pose.
	 * @param y Y coordinate of the pose.
	 * @param theta Orientation of the pose.
	 * @param steering Steering angle of this {@link PoseSteering}.
	 */
	public PoseSteering(double x, double y, double theta, double steering) {
		this.pose = new Pose(x,y,theta);
		this.steering = steering;
	}
	

	/**
	 * Create a {@link PoseSteering} given a {@link Pose} and a steering angle.
	 * @param p {@link Pose} of this {@link PoseSteering}.
	 * @param steering Steering angle of this {@link PoseSteering}.
	 */
	public PoseSteering(Pose p, double steering) {
		this.pose = p;
		this.steering = steering;
	}
	
	/**
	 * Get the X position of this {@link PoseSteering}.
	 * @return The X position of this {@link PoseSteering}.
	 */
	public double getX() {
		return this.pose.getX();
	}
	
	/**
	 * Get the Y position of this {@link PoseSteering}.
	 * @return The Y position of this {@link PoseSteering}.
	 */
	public double getY() {
		return this.pose.getY();
	}
	
	/**
	 * Get the Z position of this {@link PoseSteering}.
	 * @return The Z position of this {@link PoseSteering}.
	 */
	public double getZ() {
		return this.pose.getZ();
	}
	
	/**
	 * Get the orientation of this {@link PoseSteering}.
	 * @return The orientation of this {@link PoseSteering}.
	 */
	public double getTheta() {
		return this.pose.getTheta();
	}
	
	/**
	 * Get the roll of this {@link PoseSteering}.
	 * @return The roll of this {@link PoseSteering}.
	 */
	public double getRoll() {
		return this.pose.getRoll();
	}
	
	/**
	 * Get the pitch of this {@link PoseSteering}.
	 * @return The pitch of this {@link PoseSteering}.
	 */
	public double getPitch() {
		return this.pose.getPitch();
	}
	
	/**
	 * Get the yaw of this {@link PoseSteering}.
	 * @return The yaw of this {@link PoseSteering}.
	 */
	public double getYaw() {
		return this.pose.getYaw();
	}
	
	
	/**
	 * Get the pose of this {@link PoseSteering}.
	 * @return The pose of this {@link PoseSteering}.
	 */
	public Pose getPose() {
		return this.pose;
	}
	
	/**
	 * Get the steering angle of this {@link PoseSteering}.
	 * @return The steering angle of this {@link PoseSteering}.
	 */
	public double getSteering() {
		return this.steering;
	}
	
	/**
	 * Computes the {@link PoseSteering} between this {@link PoseSteering} and a given {@link PoseSteering} via bilinear interpolation.
	 * @param p2 The second {@link PoseSteering} used for interpolation.
	 * @param ratio Parameter in [0,1] used for bilinear interpolation.
	 * @return The {@link PoseSteering} between this {@link PoseSteering} and a given {@link PoseSteering} via bilinear interpolation.
	 */
	public PoseSteering interpolate(PoseSteering p2, double ratio) {
		Pose interp = this.getPose().interpolate(p2.getPose(), ratio);		
		return new PoseSteering(interp, Pose.lerpDegrees(getSteering(),p2.getSteering(),ratio));
	}

}


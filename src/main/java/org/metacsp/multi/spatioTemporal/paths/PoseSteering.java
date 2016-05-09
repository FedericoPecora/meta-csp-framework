package org.metacsp.multi.spatioTemporal.paths;

public class PoseSteering {
	private Pose pose;
	private double steering;
	public PoseSteering(double x, double y, double theta, double steering) {
		this.pose = new Pose(x,y,theta);
		this.steering = steering;
	}
	public double getX() {
		return this.pose.getX();
	}
	public double getY() {
		return this.pose.getY();
	}
	public double getTheta() {
		return this.pose.getTheta();
	}
	public Pose getPose() {
		return this.pose;
	}
	public double getSteering() {
		return this.steering;
	}

}


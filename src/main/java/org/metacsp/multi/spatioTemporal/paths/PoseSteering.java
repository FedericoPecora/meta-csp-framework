package org.metacsp.multi.spatioTemporal.paths;

public class PoseSteering {
	double x, y, theta, steering;
	public PoseSteering(double x, double y, double theta, double steering) {
		this.x = x;
		this.y = y;
		this.theta = theta;
		this.steering = steering;
	}
	public double getX() {
		return x;
	}
	public double getY() {
		return y;
	}
	public double getTheta() {
		return theta;
	}
	public double getSteering() {
		return steering;
	}

}


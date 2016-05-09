package org.metacsp.multi.spatioTemporal.paths;

public class Quaternion {
	
	private double x, y, z, w;
	
	public Quaternion (double x, double y, double z, double w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}
	public Quaternion (double theta) {
		this.x = 0;
		this.y = 0;
		this.z = Math.sin(theta/2.0);			
		this.w = Math.cos(theta/2.0);	
	}
	public double getTheta() {
		return Math.atan2(2.0*(this.x*this.y+this.z*this.w),1.0-2*(this.y*this.y+this.z*this.z));
	}
	public double getX() {
		return x;
	}
	public double getY() {
		return y;
	}
	public double getZ() {
		return z;
	}
	public double getW() {
		return w;
	}

}

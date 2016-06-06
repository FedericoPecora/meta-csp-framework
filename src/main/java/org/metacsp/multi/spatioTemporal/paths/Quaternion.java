package org.metacsp.multi.spatioTemporal.paths;

import java.io.Serializable;

/**
 * Class for maintaining and converting from/to Quaternion representations.
 * @author Federico Pecora
 *
 */
public class Quaternion implements Serializable {
	
	private static final long serialVersionUID = -5541257184074383188L;
	private double x, y, z, w;
	
	/**
	 * Create a {@link Quaternion} given its parameters.
	 * @param x X component of this {@link Quaternion}.
	 * @param y Y component of this {@link Quaternion}.
	 * @param z Z component of this {@link Quaternion}.
	 * @param w W component of this {@link Quaternion}.
	 */
	public Quaternion (double x, double y, double z, double w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}
	
	/**
	 * Creates a {@link Quaternion} from a given angle.
	 * @param theta The angle to encode as a {@link Quaternion}.
	 */
	public Quaternion (double theta) {
		this.x = 0;
		this.y = 0;
		this.z = Math.sin(theta/2.0);			
		this.w = Math.cos(theta/2.0);	
	}
	
	/**
	 * Get the angle in radians represented by this {@link Quaternion}.
	 * @return The angle in radians represented by this {@link Quaternion}.
	 */
	public double getTheta() {
		return Math.atan2(2.0*(this.x*this.y+this.z*this.w),1.0-2*(this.y*this.y+this.z*this.z));
	}
	
	/**
	 * Get the X component of this {@link Quaternion}.
	 * @return The X component of this {@link Quaternion}.
	 */
	public double getX() {
		return x;
	}

	/**
	 * Get the Y component of this {@link Quaternion}.
	 * @return The Y component of this {@link Quaternion}.
	 */
	public double getY() {
		return y;
	}
	
	/**
	 * Get the Z component of this {@link Quaternion}.
	 * @return The Z component of this {@link Quaternion}.
	 */
	public double getZ() {
		return z;
	}
	
	/**
	 * Get the W component of this {@link Quaternion}.
	 * @return The W component of this {@link Quaternion}.
	 */
	public double getW() {
		return w;
	}

}

package org.metacsp.utility.UI;

public class TweenableEnvelope {
	
	private float minX, maxX, minY, maxY;
	
	public TweenableEnvelope(double minX, double minY, double maxX, double maxY) {
		this.minX = (float)minX;
		this.minY = (float)minY;
		this.maxX = (float)maxX;
		this.maxY = (float)maxY;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TweenableEnvelope) {
			TweenableEnvelope other = (TweenableEnvelope)obj;
			if (other.getMaxX() == this.getMaxX() && other.getMaxY() == this.getMaxY() && other.getMinX() == this.getMinX() && other.getMinY() == this.getMinY()) return true;
		}
		return super.equals(obj);
	}
	
	public float getWidth() {
		return this.maxX-this.minX;
	}
	
	public float getHeight() {
		return this.maxY-this.minY;
	}
	
	public float getMinY() {
		return minY;
	}

	public void setMinY(float minY) {
		this.minY = minY;
	}

	public float getMaxY() {
		return maxY;
	}

	public void setMaxY(float maxY) {
		this.maxY = maxY;
	}

	public float getMinX() {
		return minX;
	}

	public void setMinX(float minX) {
		this.minX = minX;
	}

	public float getMaxX() {
		return maxX;
	}

	public void setMaxX(float maxX) {
		this.maxX = maxX;
	}
	

}

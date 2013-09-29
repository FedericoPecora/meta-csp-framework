package org.metacsp.multi.spatial.blockAlgebra;

import org.metacsp.multi.spatial.rectangleAlgebra.Point;

public class RectangularCuboid {
	
	private int length, width, height;
	private Point point;
	public RectangularCuboid(Point point, int lenght, int width, int height) {
		
		this.point = point;
		this.length = lenght;
		this.width = width;
		this.height = height;
	}
	
	public int getHeight() {
		return height;
	}
	public int getLength() {
		return length;
	}
	public int getWidth() {
		return width;
	}
	public Point getPoint() {
		return point;
	}
	
	@Override
	public String toString() {
		return "[" + "x: " + point.getX() + ", " + "y: " + point.getY() + ", " + "z: " + point.getZ() + ", " +
	"lenght: " + length + ", " + ": " + width + ", " + "height: " + height + "]" ;
	}

}

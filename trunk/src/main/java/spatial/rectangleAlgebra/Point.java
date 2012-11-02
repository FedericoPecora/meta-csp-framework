package spatial.rectangleAlgebra;

public class Point {
	public double x, y;
	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}
	public int x() {
		if (Math.abs(new Double(x).intValue()-x) < 0.5)
			return new Double(x).intValue();
		return new Double(x).intValue()+1;
	}
	public int y() {
		if (Math.abs(new Double(y).intValue()-y) < 0.5)
			return new Double(y).intValue();
		return new Double(y).intValue()+1;
	}
	public String toString() {
		return "(" + x + "," + y + ")";
	}
	public double distance(Point p) {		
		return Math.sqrt(Math.pow(p.x-this.x,2) + Math.pow(p.y-this.y,2));
	}
	public double getX() {
		return x;
	}
	public double getY() {
		return y;
	}
	
}

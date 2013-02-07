/*******************************************************************************
 * Copyright (c) 2010-2013 Federico Pecora <federico.pecora@oru.se>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
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

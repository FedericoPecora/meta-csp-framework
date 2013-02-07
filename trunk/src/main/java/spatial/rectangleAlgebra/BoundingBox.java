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

import java.awt.Rectangle;

import time.Bounds;

public class BoundingBox {

	private Bounds xLB, xUB, yLB, yUB;
	private String name = "";
	
	/**
	 * 
	 * @param xLB
	 * @param xUB
	 * @param yLB
	 * @param yUB
	 */
	public BoundingBox(Bounds xLB, Bounds xUB, Bounds yLB, Bounds yUB){
		
		this.xLB = xLB;
		this.xUB = xUB;
		this.yLB = yLB;
		this.yUB = yUB;
		
	}
	
	public Bounds getxLB() {
		return xLB;
	}
	
	public Bounds getxUB() {
		return xUB;
	}
	
	public Bounds getyLB() {
		return yLB;
	}
	
	public Bounds getyUB() {
		return yUB;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	//give min rectangle
	public Rectangle getMinRectabgle(){
		return new Rectangle((int)xLB.min, (int)yLB.min, (int)(xUB.min - xLB.min), (int)(yUB.min - yLB.min));
	}
	
	
	//give max rectangle
	public Rectangle getMaxRectabgle(){
		return new Rectangle((int)xLB.max, (int)yLB.max, (int)(xUB.max - xLB.max), (int)(yUB.max - yLB.max));
	}
	
	//it is not theoretically proved yet!!!
//	public double[] getACentrePointSolution(){
//		double recminCX = getMinRectabgle().getX() + getMinRectabgle().getWidth() / 2;
//		double recminCY = getMinRectabgle().getY() + getMinRectabgle().getHeight() / 2;
//		double recmaxCX = getMaxRectabgle().getX() + getMaxRectabgle().getWidth() / 2;;
//		double recmaxCY = getMaxRectabgle().getY() + getMaxRectabgle().getHeight() / 2;;
//		
//		double[] ret = new double[2];
//		ret[0] = recminCX + (recmaxCX - recminCX) / 2;
//		ret[1] = recminCY + (recmaxCY - recminCY) / 2;
//		
//		return ret;
//	}
	
	public Point getACentrePointSolution(){
		return new Point(getAlmostCentreRectangle().getCenterX(), getAlmostCentreRectangle().getCenterY());
	}

	
	public Rectangle getAlmostCentreRectangle(){
		return new Rectangle(
				(int)(xLB.min + ((xLB.max - xLB.min)/2)), 
				(int)(yLB.min + ((yLB.max - yLB.min)/2)), 
				(int)((xUB.min + ((xUB.max - xUB.min)/2)) - (xLB.min + ((xLB.max - xLB.min)/2))),
				(int)((yUB.min + ((yUB.max - yUB.min)/2)) - (yLB.min + ((yLB.max - yLB.min)/2)))
				);
	}
	
	
		
}

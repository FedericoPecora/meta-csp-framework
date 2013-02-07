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

public class NominalSpatialRelation {
	
	private String from = "";
	private String to = "";
	private String nominalConstraint = "";
	private RectangleConstraint raCons = new RectangleConstraint();
	
	public NominalSpatialRelation(String from, String to, String nominalConstraint){
		this.from = from;
		this.to = to;
		this.nominalConstraint = nominalConstraint;
	}
	
	public String getTo() {
		return to;
	}
	
	public String getFrom() {
		return from;
	}
	
	public String getNominalConstraint() {
		return nominalConstraint;
	}

	public RectangleConstraint getRaCons() {
		return raCons;
	}
	
	public void setRaCons(RectangleConstraint raCons) {
		this.raCons = raCons;
	}
	
	//to implement a mapper
	
	
	

	
	
	

}

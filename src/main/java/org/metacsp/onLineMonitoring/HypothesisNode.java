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
package org.metacsp.onLineMonitoring;

import org.metacsp.multi.fuzzyActivity.FuzzyActivity;


/**
 * 
 */

public class HypothesisNode{

	private FuzzyActivity fa;
	private double sigmaTC = 0;
	private double sigmaVC = 0;
	private double sigmaOC = 0;
	private Hypothesis hyp;
	
	public HypothesisNode(FuzzyActivity fa, double sigmaTc, double sigmaVC, double sigmaOC, Hypothesis hyp) {
		
		this.hyp = hyp;
		this.fa = fa;
		this.sigmaOC = sigmaOC;
		this.sigmaTC = sigmaTc;
		this.sigmaVC = sigmaVC;
	}	

	public Hypothesis getHyp() {
		return hyp;
	}
	
	public FuzzyActivity getFuzzyActivity() {
		return fa;
	}
	
	public double getSigmaOC() {
		return sigmaOC;
	}
	
	public double getSigmaVC() {
		return sigmaVC;
	}
	
	public double getSigmaTC() {
		return sigmaTC;
	}
	
	public String toString(){
		String ret = "Id: " + this.fa + " OC: " + this.sigmaOC + " TC: " + this.sigmaTC + " VC: " + this.sigmaVC + " hyp: " +this.hyp;
		return ret;
	}

}

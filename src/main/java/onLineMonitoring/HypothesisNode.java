package onLineMonitoring;

import multi.fuzzyActivity.FuzzyActivity;


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

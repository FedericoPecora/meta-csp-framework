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
package onLineMonitoring;


public class Rule {
	
	private Requirement[] requirements;
	private MonitoredComponent component;
	private double[] possibilities;
	private double threshold = 0.2;
	private int dependencyRank = 0;
	
	public Rule(MonitoredComponent component, double[] possibilities, Requirement... requirements) {
		this.requirements = requirements;
		this.component = component;
		this.possibilities = possibilities;
		//FIXME: Self assignment of field Rule.threshold in new onLineMonitoring.Rule(MonitoredComponent, double[], Requirement[])	Rule.java	/MetaCSPFramework/src/onLineMonitoring	line 16	FindBugs Problem (Scariest)
//		this.threshold = threshold;
	}

	public void setDependencyRank(int dependencyRank) {
		this.dependencyRank = dependencyRank;
	}
	
	public int getDependencyRank() {
		return dependencyRank;
	}
	/**
	 * @return the requirements
	 */
	public Requirement[] getRequirements() {
		return requirements;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}
	
	public double getThreshold() {
		return threshold;
	}
	/**
	 * @param requirements the requirements to set
	 	
	public void setRequirements(Requirement[] requirements) {
		this.requirements = requirements;
	}
	 */
	
	/**
	 * @return The component that this {@link Rule} refers to.
	 */
	public MonitoredComponent getComponent() {
		return component;
	}
	
	public double[] getPossibilities() { return possibilities; }

	/*
	public String toString() {
		return this.getComponent() + " " + Arrays.toString(this.getPossibilities());
	}
	*/
	
	public String toString() {
		String state = "";
		for (int i = 0; i < this.getPossibilities().length; i++) {
			if (this.getPossibilities()[i] == 1.0) {
				state = this.getComponent().getStates()[i];
				break;
			}
		}
		String ret = "" + this.getComponent().getName() + " " + state;
		for (Requirement r : this.getRequirements()) {
			ret += "\n\t" + r;
		}
		return ret;
	}

	public String getHead() {
		String state = "";
		for (int i = 0; i < this.getPossibilities().length; i++) {
			if (this.getPossibilities()[i] == 1.0) {
				state = this.getComponent().getStates()[i];
				break;
			}
		}
		String ret = state;
		
		return ret;
	}
	
}

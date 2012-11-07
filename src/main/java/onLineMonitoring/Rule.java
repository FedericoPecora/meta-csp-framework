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

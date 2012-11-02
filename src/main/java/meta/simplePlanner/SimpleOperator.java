package meta.simplePlanner;

import multi.allenInterval.AllenIntervalConstraint;
import cern.colt.Arrays;

public class SimpleOperator {

	protected String head;
	protected AllenIntervalConstraint[] requirementConstraints;
	protected String[] requirementActivities;
	protected int[] usages;
	protected AllenIntervalConstraint[][] extraConstraints;

	public SimpleOperator(String head, AllenIntervalConstraint[] requirementConstraints, String[] requirementActivities, int[] usages) {
		this.head = head;
		if (requirementActivities != null) {
			for (String a : requirementActivities) {
				if (a.equals(head)) throw new InvalidActivityException(a);
			}
		}
		this.requirementConstraints = requirementConstraints;
		this.requirementActivities = requirementActivities;
		this.usages = usages;
		if (requirementConstraints != null) this.extraConstraints = new AllenIntervalConstraint[requirementActivities.length+1][requirementActivities.length+1];
	}
	
	public void addConstraint(AllenIntervalConstraint c, int from, int to) {
		extraConstraints[from][to] = c;
	}
	
	public AllenIntervalConstraint[][] getExtraConstraints() {
		return this.extraConstraints;
	}
	
	public String getHead() {
		return head;
	}

	public AllenIntervalConstraint[] getRequirementConstraints() {
		return requirementConstraints;
	}

	public String[] getRequirementActivities() {
		return requirementActivities;
	}

	public int[] getUsages() {
		return usages;
	}

	public String toString() {
		String ret = "";
		if (requirementActivities != null) {
			for (int i = 0; i < requirementActivities.length; i++) {
				ret += head + " " + requirementConstraints[i].getType() + " " + Arrays.toString(requirementConstraints[i].getBounds()) + " " + requirementActivities[i];
				if (i != requirementActivities.length-1) ret += "\n";
			}
		}
		if (usages != null) {
			if (requirementActivities != null) ret += "\n";
			ret += head + " usage: " + Arrays.toString(usages);
		}
		return ret;
	}
	
	
}
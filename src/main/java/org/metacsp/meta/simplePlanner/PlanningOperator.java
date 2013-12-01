package org.metacsp.meta.simplePlanner;

import org.metacsp.multi.allenInterval.AllenIntervalConstraint;

public class PlanningOperator extends SimpleOperator {

	private boolean[] effects;
	
	public PlanningOperator(String head, AllenIntervalConstraint[] requirementConstraints, String[] requirementActivities, boolean[] effects, int[] usages) {
		super(head, requirementConstraints, requirementActivities, usages);
		this.effects = effects;
	}
	
	public boolean isEffect(String requirement) {
		for (int i = 0; i < this.requirementActivities.length; i++) {
			if 	(this.requirementActivities[i].equals(requirement)) return effects[i];
		}
		return false;
	}
	
}

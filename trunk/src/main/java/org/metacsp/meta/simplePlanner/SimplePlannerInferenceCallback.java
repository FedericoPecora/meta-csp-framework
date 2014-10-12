package org.metacsp.meta.simplePlanner;

import java.io.Serializable;
import java.util.logging.Logger;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariablePrototype;
import org.metacsp.framework.meta.MetaConstraint;
import org.metacsp.multi.activity.Activity;
import org.metacsp.sensing.InferenceCallback;
import org.metacsp.utility.logging.MetaCSPLogging;

public class SimplePlannerInferenceCallback implements InferenceCallback, Serializable {

	private static final long serialVersionUID = -6730506457770817729L;
	private SimplePlanner planner = null;
	private transient Logger logger = MetaCSPLogging.getLogger(this.getClass());
	private ProactivePlanningDomain domain = null;
	
	public SimplePlannerInferenceCallback(SimplePlanner planner) {
		this.planner = planner;
		MetaConstraint[] metaConstraints = planner.getMetaConstraints();
		for (MetaConstraint mc : metaConstraints) {
			if (mc instanceof ProactivePlanningDomain) {
				domain = (ProactivePlanningDomain) mc;
				break;
			}
		}
	}
	
	@Override
	public void doInference(long timeNow) {
		if (planner != null) {
			domain.resetContextInference();
			domain.updateTimeNow(timeNow);
			planner.clearResolvers();
			planner.backtrack();
			for (ConstraintNetwork cn : planner.getAddedResolvers()) {
				VariablePrototype var = null;
				for (Variable v : cn.getVariables()) {
					if (v instanceof VariablePrototype) {
						if (((VariablePrototype)v).getParameters().length > 2) {
							if (((VariablePrototype)v).getParameters()[2].equals("Inference")) {
								var = (VariablePrototype)v;
							}
						}
					}
				}
				if (var != null) {
					Activity act = (Activity)cn.getSubstitution(var);
					domain.setOldInference(act.getComponent(), act);
				}
			}
		}
	}

}

package meta.TCSP;

import multi.TCSP.DistanceConstraint;
import framework.ConstraintNetwork;
import framework.VariableOrderingH;

public class MostConstrainedFirstVarOH extends VariableOrderingH {

	@Override
	public int compare(ConstraintNetwork arg0, ConstraintNetwork arg1) {
		DistanceConstraint dc0 = (DistanceConstraint)arg0.getConstraints()[0];
		DistanceConstraint dc1 = (DistanceConstraint)arg1.getConstraints()[0];
		return dc0.getInternalConstraints().length-dc1.getInternalConstraints().length;
	}

	@Override
	public void collectData(ConstraintNetwork[] allMetaVariables) {
		// TODO Auto-generated method stub
		
	}

}

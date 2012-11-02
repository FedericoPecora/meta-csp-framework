package meta.symbolsAndTime;

import multi.activity.Activity;
import framework.ConstraintNetwork;
import framework.VariableOrderingH;

public class LatestStartTimeVarOH extends VariableOrderingH {

	@Override
	public int compare(ConstraintNetwork arg0, ConstraintNetwork arg1) {
		// TODO Auto-generated method stub
		long time1 = ((Activity)arg0.getVariables()[0]).getTemporalVariable().getEST();
		long time2 = ((Activity)arg1.getVariables()[0]).getTemporalVariable().getEST();
		if (time1 > time2) return -1;
		else if (time1 < time2) return 1;
		return 0;
	}

	@Override
	public void collectData(ConstraintNetwork[] allMetaVariables) {
		// TODO Auto-generated method stub
		
	}

}

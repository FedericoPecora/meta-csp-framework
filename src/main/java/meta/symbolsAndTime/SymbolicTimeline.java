package meta.symbolsAndTime;

import multi.activity.Activity;
import multi.activity.ActivityNetworkSolver;
import multi.activity.Timeline;
import symbols.SymbolicDomain;
import framework.Variable;

public class SymbolicTimeline extends Timeline {

	private Object[] values = null;
	
	public SymbolicTimeline(ActivityNetworkSolver ans, String component) {
		super(ans, component);
		cacheValues();
		// TODO Auto-generated constructor stub
	}

	private void cacheValues() {
		SymbolicDomain[] ret = new SymbolicDomain[getPulses().length];
		for (int i = 0; i < getPulses().length-1; i++) {
			for (Variable var : getAn().getVariables(component)) {
				Activity act = (Activity)var;
				if (act.getTemporalVariable().getEST() <= getPulses()[i] && act.getTemporalVariable().getEET() >= getPulses()[i+1]) {
					SymbolicDomain dom = (SymbolicDomain)act.getSymbolicVariable().getDomain();
					if (ret[i] == null) ret[i] = dom;
					//else ret[i] = SymbolicDomain.intersection(ret[i], dom);
					else ret[i] = SymbolicDomain.union(ret[i], dom);
				}
			}
		}
		values = ret;		
	}
	
	@Override
	public Object[] getValues()  { return values; }

	@Override
	public boolean isUndetermined(Object o) {
		return (o == null);
	}

	@Override
	public boolean isCritical(Object o) {
		if (o instanceof SymbolicDomain) return (((SymbolicDomain)o).getSymbols().length == 1);
		return false;
	}

	@Override
	public boolean isInconsistent(Object o) {
		if (o instanceof SymbolicDomain) return (((SymbolicDomain)o).getSymbols().length == 0);
		return false;
	}

}

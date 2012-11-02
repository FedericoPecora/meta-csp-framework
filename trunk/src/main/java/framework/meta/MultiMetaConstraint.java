package framework.meta;

import java.util.Arrays;
import java.util.HashMap;

import framework.ConstraintNetwork;
import framework.ConstraintOrderingH;
import framework.ValueOrderingH;
import framework.VariableOrderingH;

public abstract class MultiMetaConstraint extends MetaConstraint {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7319855771259677256L;
	private MetaConstraint[] myMetaCons;
	private ConstraintOrderingH myConstraintOrderingH;
		
	public MultiMetaConstraint(VariableOrderingH varOH, ValueOrderingH valOH, ConstraintOrderingH consOH, MetaConstraint ... metacons) {
		super(varOH, valOH);
		myMetaCons = metacons;
	}

	public ConstraintOrderingH getConOrderingH() {
		return myConstraintOrderingH;
	}
	
	public ConstraintNetwork getMetaVariable() {
		HashMap<MetaConstraint,ConstraintNetwork> subMetaCons = new HashMap<MetaConstraint, ConstraintNetwork>();
		for (MetaConstraint mc : myMetaCons) {
			ConstraintNetwork newMv = null;
			if ((newMv = mc.getMetaVariable()) != null) {
				subMetaCons.put(mc, newMv);
			}
		}
		
		MetaConstraint[] mcsArray = subMetaCons.keySet().toArray(new MetaConstraint[subMetaCons.keySet().size()]);
		Arrays.sort(mcsArray,this.getConOrderingH());
		return subMetaCons.get(mcsArray[0]);				
	}


}

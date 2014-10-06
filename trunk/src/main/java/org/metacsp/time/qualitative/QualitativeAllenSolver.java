package org.metacsp.time.qualitative;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;
import org.metacsp.framework.ConstraintSolver.OPTIONS;
import org.metacsp.time.qualitative.QualitativeAllenIntervalConstraint.Type;



public class QualitativeAllenSolver extends ConstraintSolver {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9130340233823443991L;
	private int IDs = 0;
	private ConstraintNetwork completeNetwork = null;
	
	public QualitativeAllenSolver() {
		super(new Class[]{QualitativeAllenIntervalConstraint.class}, SimpleAllenInterval.class);
		this.setOptions(OPTIONS.AUTO_PROPAGATE);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean propagate() {		
		if(this.getConstraints().length == 0) return true;
		createCompleteNetwork();
		return pathConsistency(); 
	}

	private void createCompleteNetwork() {
		completeNetwork = new ConstraintNetwork(this);
		ConstraintNetwork originalNetwork = this.getConstraintNetwork();
		for (Variable var : originalNetwork.getVariables()) completeNetwork.addVariable(var);
		for (Constraint con : originalNetwork.getConstraints()) completeNetwork.addConstraint(con);
		Variable[] vars = completeNetwork.getVariables();
		for (int i = 0; i < vars.length; i++) {
			for (int j = 0; j < vars.length; j++) {
				if (i != j && originalNetwork.getConstraint(vars[i],vars[j]) == null) {
					if (originalNetwork.getConstraint(vars[j],vars[i]) != null) {
						//add inverse
						Type[] types = ((QualitativeAllenIntervalConstraint)originalNetwork.getConstraint(vars[j],vars[i])).getTypes();
						Type[] inverses = QualitativeAllenIntervalConstraint.getInverseRelation(types);
						QualitativeAllenIntervalConstraint inverse = new QualitativeAllenIntervalConstraint(inverses);
						inverse.setFrom(vars[i]);
						inverse.setTo(vars[j]);
						completeNetwork.addConstraint(inverse);
					}
					else {
						//create universal relation
						Type[] allTypes = new Type[QualitativeAllenIntervalConstraint.Type.values().length];
						for (int k = 0; k < QualitativeAllenIntervalConstraint.Type.values().length; k++) allTypes[k] = QualitativeAllenIntervalConstraint.Type.values()[k];
						QualitativeAllenIntervalConstraint universe = new QualitativeAllenIntervalConstraint(allTypes);
						universe.setFrom(vars[i]);
						universe.setTo(vars[j]);
						completeNetwork.addConstraint(universe);
					}
				}
			}	
		}
	}

	private boolean pathConsistency() {
		boolean fixedpoint = false;
		Variable[] vars = this.completeNetwork.getVariables();
		while (!fixedpoint) {
			fixedpoint = true;
			for (int k = 0; k < vars.length; k++) {
				for (int i = 0; i < vars.length; i++) {
					if (i != k) {
						for (int j = 0; j < vars.length; j++) {
							if (j != k && j != i) {
								QualitativeAllenIntervalConstraint r_ij = (QualitativeAllenIntervalConstraint)completeNetwork.getConstraint(vars[i], vars[j]);
								QualitativeAllenIntervalConstraint r_ik = (QualitativeAllenIntervalConstraint)completeNetwork.getConstraint(vars[i], vars[k]);
								QualitativeAllenIntervalConstraint r_kj = (QualitativeAllenIntervalConstraint)completeNetwork.getConstraint(vars[k], vars[j]);
								//comp = R_ik * R_kj
								QualitativeAllenIntervalConstraint comp = getComposition(r_ik, r_kj);
								//inters = R_ij ^ comp
								QualitativeAllenIntervalConstraint inters = getIntersection(r_ij, comp);
								//if inters = 0 return false
								if (inters.getTypes().length == 0) return false;
								//if inters != R_ij
								if (inters.getTypes().length < r_ij.getTypes().length) {
									completeNetwork.removeConstraint(r_ij);
									completeNetwork.addConstraint(inters);
									fixedpoint = false;
								}
							}
						}	
					}
				}				
			}
		}
		return true;
	}
	
	


	private QualitativeAllenIntervalConstraint getIntersection(QualitativeAllenIntervalConstraint o1, QualitativeAllenIntervalConstraint o2) {
		Vector<QualitativeAllenIntervalConstraint.Type> intersetction =  new Vector<QualitativeAllenIntervalConstraint.Type>();
		for (Type t : o1.getTypes()) {
			if (Arrays.asList(o2.getTypes()).contains(t)) intersetction.add(t);
		}
		QualitativeAllenIntervalConstraint ret = new QualitativeAllenIntervalConstraint(intersetction.toArray(new Type[intersetction.size()]));
		ret.setFrom(o1.getFrom());
		ret.setTo(o1.getTo());
		return ret;
	}
	
	private QualitativeAllenIntervalConstraint getComposition(QualitativeAllenIntervalConstraint o1, QualitativeAllenIntervalConstraint o2) {
		Vector<QualitativeAllenIntervalConstraint.Type> cmprelation =  new Vector<QualitativeAllenIntervalConstraint.Type>();
		for (int t = 0; t < o1.types.length; t++) {
			for (int t2 = 0; t2 < o2.types.length; t2++) {
				QualitativeAllenIntervalConstraint.Type[] tmpType = QualitativeAllenIntervalConstraint.transitionTable[o1.types[t].ordinal()][o2.types[t2].ordinal()];
				for(QualitativeAllenIntervalConstraint.Type t3: tmpType) {
					if(!cmprelation.contains(t3)) cmprelation.add(t3);
				}	
			}
		}
		QualitativeAllenIntervalConstraint ret = new QualitativeAllenIntervalConstraint(cmprelation.toArray(new Type[cmprelation.size()]));
		ret.setFrom(o1.getFrom());
		ret.setTo(o2.getTo());
		return ret;
	}

	@Override
	protected boolean addConstraintsSub(Constraint[] c) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected void removeConstraintsSub(Constraint[] c) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected Variable[] createVariablesSub(int num) {
		SimpleAllenInterval[] ret = new SimpleAllenInterval[num];
		for (int i = 0; i < num; i++) ret[i] = new SimpleAllenInterval(this, IDs++);
			return ret;
	}

	@Override
	protected void removeVariablesSub(Variable[] v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void registerValueChoiceFunctions() {
		// TODO Auto-generated method stub
		
	}


}

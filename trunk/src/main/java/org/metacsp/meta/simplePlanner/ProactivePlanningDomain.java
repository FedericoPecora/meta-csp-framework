package org.metacsp.meta.simplePlanner;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.VariableOrderingH;
import org.metacsp.framework.VariablePrototype;
import org.metacsp.framework.meta.MetaConstraint;
import org.metacsp.framework.meta.MetaVariable;
import org.metacsp.meta.symbolsAndTime.Schedulable;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.utility.PowerSet;

public class ProactivePlanningDomain extends SimpleDomain {

	private boolean triggered = false;
	
	public ProactivePlanningDomain(int[] capacities, String[] resourceNames, String domainName) {
		super(capacities, resourceNames, domainName);
	}

	private static final long serialVersionUID = 5232380823036756902L;

	private static VariableOrderingH getVariableOrderingH() {
		//No variable ordering
		VariableOrderingH varOH = new VariableOrderingH() {
			@Override
			public int compare(ConstraintNetwork o1, ConstraintNetwork o2) { return 0; }
			@Override
			public void collectData(ConstraintNetwork[] allMetaVariables) { }
		};
		return varOH;
	}

	private static ValueOrderingH getValueOrderingH() {
		//Try to support most inferred activities first
		ValueOrderingH valOH = new ValueOrderingH() {
			@Override
			public int compare(ConstraintNetwork arg0, ConstraintNetwork arg1) {
				return arg1.getVariables().length - arg0.getVariables().length;
			}
		};
		return valOH;
	}
	/**
	 * Parses a domain file (see domains/testContextInference.ddl for an example), instantiates
	 * the necessary {@link MetaConstraint}s and adds them to the provided {@link SimplePlanner}.
	 * @param sp The {@link SimplePlanner} that will use this domain.
	 * @param filename Text file containing the domain definition. 
	 */
	public static void parseDomain(SimplePlanner sp, String filename) {
		String everything = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			try {
				StringBuilder sb = new StringBuilder();
				String line = br.readLine();
				while (line != null) {
					if (!line.startsWith("#")) {
						sb.append(line);
						sb.append('\n');
					}
					line = br.readLine();
				}
				everything = sb.toString();
				String name = parseName(everything);
				HashMap<String,Integer> resources = parseResources(everything);
				String[] operators = parseOperators(everything);
				String[] sensors = parseSensors(everything);
				String[] contextVars = parseContextVars(everything);
				
				//SimpleDomain rd = new SimpleDomain(
				//  new int[] {6,6,6},
				//  new String[] {"power", "usbport", "serialport"},
				//  "TestDomain"
				//);
				int[] resourceCaps = new int[resources.keySet().size()];
				String[] resourceNames = new String[resources.keySet().size()];
				int resourceCounter = 0;
				for (String rname : resources.keySet()) {
					resourceNames[resourceCounter] = rname;
					resourceCaps[resourceCounter] = resources.get(rname);
					resourceCounter++;
				}
				ProactivePlanningDomain dom = new ProactivePlanningDomain(resourceCaps, resourceNames, name);
				dom.setValOH(getValueOrderingH());
				dom.setVarOH(getVariableOrderingH());
				for (String sensor : sensors) dom.addSensor(sensor);
				for (String cv : contextVars) dom.addContextVar(cv);
				for (String operator : operators) {
					dom.addOperator(SimpleOperator.parseSimpleOperator(operator));
				}
				//This adds the domain as a meta-constraint of the SimplePlanner
				sp.addMetaConstraint(dom);
				//... and we also add all its resources as separate meta-constraints
				for (Schedulable sch : dom.getSchedulingMetaConstraints()) sp.addMetaConstraint(sch);
			}
			finally { br.close(); }
		}
		catch (FileNotFoundException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
	}

	private Set<Set<VariablePrototype>> generateGoals() {
		ActivityNetworkSolver groundSolver = (ActivityNetworkSolver)this.metaCS.getConstraintSolvers()[0];
		SimpleOperator[] ops = this.getOperators();
		HashSet<VariablePrototype> vars = new HashSet<VariablePrototype>();
		for (SimpleOperator op : ops) {
			String head = op.getHead();
			String headComponent = head.substring(0,head.indexOf("::"));
			String headValue = head.substring(head.indexOf("::")+2);
			if (this.isContextVar(headComponent)) {
				VariablePrototype toInfer = new VariablePrototype(groundSolver, headComponent, headValue);
				toInfer.setMarking(markings.UNJUSTIFIED);
				vars.add(toInfer);
			}
		}
		Set<Set<VariablePrototype>> ret = PowerSet.powerSet(vars);
		return ret;
	}

	public ConstraintNetwork[] getMetaVariables() {
		//Add the normal metavariables for planning (UNJUSTIFIED activities)
		ConstraintNetwork[] ret = super.getMetaVariables();
		Vector<ConstraintNetwork> newRet = new Vector<ConstraintNetwork>();
		for (ConstraintNetwork cn : ret) newRet.add(cn);
		if (!triggered) {
			triggered = true;
			//Add a null constraint network to signal that context inference should be done
			ConstraintNetwork nullConstraintNetwork = new ConstraintNetwork(null);
			newRet.add(nullConstraintNetwork);
		}
		return newRet.toArray(new ConstraintNetwork[newRet.size()]);
	}
	
	public ConstraintNetwork[] getMetaValues(MetaVariable metaVariable) {
		ConstraintNetwork mv = metaVariable.getConstraintNetwork();
		//If this is not context inference, get metavalues as usual 
		if (mv.getConstraints().length != 0 || mv.getVariables().length != 0) {
			ConstraintNetwork[] ret = super.getMetaValues(metaVariable);
			return ret;
		}
		//We have a context inference metavariable - let's generate all possible worlds
		Set<Set<VariablePrototype>> possibleWorlds = generateGoals();
		Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
		for (Set<VariablePrototype> oneWorld : possibleWorlds) {
			if (!oneWorld.isEmpty()) {
				ConstraintNetwork cn = new ConstraintNetwork(null);
				for (VariablePrototype var : oneWorld) cn.addVariable(var);
				ret.add(cn);
			}
		}
		if (ret.isEmpty()) return null;
		return ret.toArray(new ConstraintNetwork[ret.size()]);
	}
}

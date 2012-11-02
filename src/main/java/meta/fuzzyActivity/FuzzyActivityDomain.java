package meta.fuzzyActivity;

import java.util.HashMap;
import java.util.Vector;

import multi.fuzzyActivity.FuzzyActivity;
import multi.fuzzyActivity.FuzzyActivityNetwork;
import multi.fuzzyActivity.FuzzyActivityNetworkSolver;
import multi.fuzzyActivity.SimpleTimeline;
import onLineMonitoring.FuzzySensorEvent;
import onLineMonitoring.PhysicalSensor;
import onLineMonitoring.Requirement;
import onLineMonitoring.Rule;
import onLineMonitoring.Sensor;
import orbital.algorithm.Combinatorical;
import symbols.SymbolicValueConstraint;
import framework.Constraint;
import framework.ConstraintNetwork;
import framework.Variable;
import framework.VariableOrderingH;
import framework.meta.MetaConstraint;
import framework.meta.MetaVariable;
import fuzzyAllenInterval.FuzzyAllenIntervalConstraint;


/**
 * Implements the {@link MetaConstraint} for fuzzy context recognition.  Variable of the
 * the meta-CSP are rules in a so-called "domain" (this class), and values are unifications of the
 * requirements prescribed by these rules with existing {@link FuzzyActivity} variables.
 * 
 * @author Federico Pecora
 *
 */
public class FuzzyActivityDomain extends MetaConstraint {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7673606470845404508L;
	private Vector<Rule> rules = new Vector<Rule>();
	private transient FuzzyActivityNetworkSolver solver;
	private Vector<FuzzyActivity> ongoignActs = new Vector<FuzzyActivity>();
	private Vector<FuzzyAllenIntervalConstraint> truthMaintenanceConstraints = new Vector<FuzzyAllenIntervalConstraint>();
	private HashMap<String, SimpleTimeline> timelines = new HashMap<String, SimpleTimeline>();
	private HashMap<FuzzyActivity, Rule> ruleHeads = new HashMap<FuzzyActivity, Rule>();
	private Vector<FuzzyActivity> fas = new Vector<FuzzyActivity>();
	private Vector<FuzzyActivity> groundActivity = new Vector<FuzzyActivity>();// includin rules head and sensors
	private Vector<FuzzyActivity> heads = new Vector<FuzzyActivity>();// includin rules head
	private Vector<Rule> toSkip = new Vector<Rule>();

	/**
	 * A {@link FuzzyActivity} representing a hypothesis can be justified or not.  Unjustified
	 * activities are the meta-variables returned by the getMetaVariables() method.
	 * 
	 * @author Federico Pecora
	 *
	 */
	public enum markings {
		UNJUSTIFIED, JUSTIFIED
	};
	
	private void ruleDependencyFinder(){
		boolean hasDep = false;
		for (Rule r : this.rules) {
			if (!toSkip.contains(r)) {
				for(Requirement req : r.getRequirements()){
					if(r.getComponent().getName() == req.getSensor().getName()){ //if all req are in toSkiplist, add it to toSkip list
						if(!isFiredBefore(req))
							hasDep = true;					
						System.out.println(r.getComponent().getName());
					}
				}
				if(!hasDep)
					toSkip.add(r);
			}	
			hasDep = false;
		}
		if(toSkip.size() != this.rules.size())
			ruleDependencyFinder();
	}

	/**
	 * IRAN: can you make this private or protected?
	 */
	public void setRuleDependency(){		
		ruleDependencyFinder();
		VariableOrderingH varOH = new VariableOrderingH() {
			
			@Override
			public int compare(ConstraintNetwork arg0, ConstraintNetwork arg1) {
				// TODO Auto-generated method stub
				//System.out.println(toSkip.indexOf(ruleHeads.get(((FuzzyActivity)arg0.getVariables()[0]))));
				//System.out.println(toSkip.indexOf(ruleHeads.get(((FuzzyActivity)arg1.getVariables()[0]))));
				return toSkip.indexOf(ruleHeads.get(((FuzzyActivity)arg0.getVariables()[0]))) - toSkip.indexOf(ruleHeads.get(((FuzzyActivity)arg1.getVariables()[0])));
			}
			
			@Override
			public void collectData(ConstraintNetwork[] allMetaVariables) {
				// TODO Auto-generated method stub
				
			}
		};//// variable ordering should be implemented
		
		this.varOH = varOH;
		

	}
	
	private boolean isFiredBefore(Requirement req) {
		for (Rule r : toSkip) {
			if(comparePossibilityDegree(r, req))
				return true;
		}
		return false;
		
	}

	private boolean comparePossibilityDegree(Rule r, Requirement req) {
		for (int i = 0; i < req.getPossibilities().length; i++) {
			if(req.getPossibilities()[i] != r.getPossibilities()[i])
				return false;
		}
		return true;
	}

	/**
	 * Creates a new {@link FuzzyActivityDomain}.  Rules should then be added to the instantiated domain.
	 */
	public FuzzyActivityDomain() {
		super(null, null);
		solver = new FuzzyActivityNetworkSolver();
		// TODO Auto-generated constructor stub
	}

	/**
	 * Sets all {@link Variable}s underlying a {@link MetaVariable} to the UNJUSTIFIED state.
	 * @param metaVariable
	 */
	public void setUnjustified(ConstraintNetwork metaVariable) {
		for (Variable v : metaVariable.getVariables()) v.setMarking(markings.UNJUSTIFIED);
	}

	@Override
	public ConstraintNetwork[] getMetaVariables() {
		Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
		for (FuzzyActivity f : fas) {
			if (f.getMarking().equals(markings.UNJUSTIFIED)) {
				FuzzyActivityNetwork fan = new FuzzyActivityNetwork(null);
				fan.addVariable(f);
				ret.add(fan);
			}

		}

		return ret.toArray(new ConstraintNetwork[ret.size()]);
	}

	@Override
	public ConstraintNetwork[] getMetaValues(MetaVariable metaVariable, int initialTime) {
		return getMetaValues(metaVariable);
	}
	
	@Override
	public ConstraintNetwork[] getMetaValues(MetaVariable metaVariable) {
		
		ConstraintNetwork conflict = metaVariable.getConstraintNetwork();
		Vector<Vector<ConstraintNetwork>> constraints = new Vector<Vector<ConstraintNetwork>>();
		FuzzyActivity head = (FuzzyActivity) conflict.getVariables()[0];
		HashMap<Sensor, Vector<Variable>> sensorVariables = new HashMap<Sensor, Vector<Variable>>();
		Vector<FuzzyActivity> cleanupActs = new Vector<FuzzyActivity>();
		// cleanupActs.add(head);

		for (Requirement req : ruleHeads.get(
				(FuzzyActivity) conflict.getVariables()[0]).getRequirements()) {
			Sensor sens = req.getSensor();
			if (sensorVariables.get(sens) == null) {
				Vector<Variable> vec = new Vector<Variable>();
				Variable[] vars = solver.getVariables(sens.getName());
				if (vars != null) {
					for (Variable var : vars){
						//if(groundActivity.contains(var))
							vec.add(var);
					}
					sensorVariables.put(sens, vec);
				}
			}
		}

		for (Requirement req : ruleHeads.get(
				(FuzzyActivity) conflict.getVariables()[0]).getRequirements()) {

			Sensor sens = req.getSensor();
			Vector<Variable> vec1 = sensorVariables.get(sens);
			Vector<Variable> vec = new Vector<Variable>();
			//vec = (Vector<Variable>) vec1.clone();
			
			for (int i = 0; i < vec1.size(); i++) {
				if(groundActivity.contains(vec1.get(i)))
					vec.add(vec1.get(i));
			}
			
			
			
			Variable[] sensVars = vec.toArray(new Variable[vec.size()]);
			Vector<ConstraintNetwork> unifications = new Vector<ConstraintNetwork>();
			for (Variable sensVar : sensVars) {
				FuzzyActivity sensAct = (FuzzyActivity) sensVar;
				FuzzyActivityNetwork oneUnification = new FuzzyActivityNetwork(
						null);

				// make temporal constraint (from: head, to: sensAct)
				// of type req.gettCons()
				FuzzyAllenIntervalConstraint tcon = new FuzzyAllenIntervalConstraint(req.gettCons());
				tcon.setFrom(head);
				tcon.setTo(sensAct);

				// make value requirement and corresponding valueConstraint
				// (from: head, to: sensAct)
				// of type req.getvCons()
				FuzzyActivity reqValue = (FuzzyActivity) solver
						.createVariable(sens.getName());
				reqValue.setDomain(sens.getStates(), req.getPossibilities());
				SymbolicValueConstraint reqValueCon = new SymbolicValueConstraint(req.getvCons());
				reqValueCon.setFrom(reqValue);
				reqValueCon.setTo(sensAct);

				// add them to oneUnification
				oneUnification.addVariable(head);
				oneUnification.addVariable(sensAct);
				oneUnification.addConstraint(tcon);
				oneUnification.addVariable(reqValue);
				oneUnification.addConstraint(reqValueCon);

				cleanupActs.add(reqValue);
				// add oneUnification to unifications
				unifications.add(oneUnification);
				/*System.out.println(",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");
				System.out.println("oneUnification" + oneUnification);
				System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
				System.out.println("solver.getVAriables");
				for (int i = 0; i < solver.getVariables().length; i++) {
					System.out.println(solver.getVariables()[i]);
				}*/
				
			}
			constraints.add(unifications);
		}

		Vector<ConstraintNetwork> toAttempt = new Vector<ConstraintNetwork>();
		int max = 0;
		for (Vector<ConstraintNetwork> vcn : constraints) {
			if (vcn.size() > max)
				max = vcn.size();
		}

		Combinatorical c = Combinatorical.getPermutations(constraints.size(),
				max, true);

		while (c.hasNext()) {
			int[] combination = c.next();

			boolean skip = false;
			for (int i = 0; i < combination.length; i++) {
				if (constraints.elementAt(i).size() <= combination[i]) {
					skip = true;
					break;
				}
			}
			if (!skip) {
				FuzzyActivityNetwork oneAttempt = new FuzzyActivityNetwork(null);
				for (int i = 0; i < combination.length; i++) {
					Vector<ConstraintNetwork> unifs = constraints.elementAt(i);
					oneAttempt.join(unifs.elementAt(combination[i]));
				}
				toAttempt.add(oneAttempt);
			}
		}
		// for (Variable v : cleanupActs) solver.removeVariable(v);
		return toAttempt.toArray(new ConstraintNetwork[toAttempt.size()]);
	}

	@Override
	public void markResolvedSub(MetaVariable con, ConstraintNetwork metaValue) {
		con.getConstraintNetwork().getVariables()[0].setMarking(markings.JUSTIFIED);

		//
		// for (int i = 0; i < metaValue.getConstraints().length; i++) {
		// if(metaValue.getConstraints()[i].getClass().getCanonicalName() ==
		// SymbolicValueConstraint.class.getCanonicalName())
		// solver.removeConstraint(metaValue.getConstraints()[i]);
		// }

	}

	@Override
	public void draw(ConstraintNetwork network) {
		// TODO Auto-generated method stub

	}

	/**
	 * Add a {@link Rule} to this {@link MetaConstraint}.
	 * @param r The rule to add.
	 */
	public void addRule(Rule r) {
		rules.add(r);
		FuzzyActivity head = (FuzzyActivity) solver.createVariable(r
				.getComponent().getName());
		head.setMarking(markings.UNJUSTIFIED);
		head.setDomain(r.getComponent().getStates(), r.getPossibilities());
		ruleHeads.put(head, r);
		// fan.addVariable(head);
		fas.add(head);
		groundActivity.add(head);
		heads.add(head);
	}

	/**
	 * Get all rules in this {@link MetaConstraint}.
	 * @return All rules in this {@link MetaConstraint}.
	 */
	public Rule[] getRules() {
		return rules.toArray(new Rule[rules.size()]);
	}

	/**
	 * Add one or more sensor events to this {@link MetaConstraint}.
	 * @param events The events to add.
	 */
	public void addFuzzySensorEvents(FuzzySensorEvent... events) {
		for (FuzzySensorEvent e : events)
			updateSensorData(e);
		for (int i = 0; i < solver.getVariables().length; i++) {
			groundActivity.add((FuzzyActivity) solver.getVariables()[i]);
		}
		setCrispCons();

	}

	private void updateSensorData(FuzzySensorEvent e) {
		// Sensor sensor, double[] possibilities
		PhysicalSensor sensor = e.getSensor();
		double[] possibilities = e.getPossibilities();
		FuzzyActivity oldAct = sensor.getCurrentAct();
		sensor.setSolver(solver);
		if (timelines.get(sensor.getName()) == null)
			timelines.put(sensor.getName(),
					new SimpleTimeline(sensor.getName()));
		boolean firstOnTimeline = false;
		if (oldAct == null)
			firstOnTimeline = true;

		// if changed then tcon will be != null
		FuzzyAllenIntervalConstraint tcon = sensor
				.setCurrentPossibilities(possibilities);

		// get new activity
		FuzzyActivity act = sensor.getCurrentAct();

		Vector<FuzzyAllenIntervalConstraint> toRetract = new Vector<FuzzyAllenIntervalConstraint>();
		Vector<FuzzyAllenIntervalConstraint> toAdd = new Vector<FuzzyAllenIntervalConstraint>();

		if (tcon != null) {
			toAdd.add(tcon);
			// Update the timeline with new info
			SimpleTimeline tl = timelines.get(sensor.getName());
			if (oldAct != null)
				tl.setEnd(oldAct, e.getTime());
			tl.addVariable(act);
			tl.setStart(act, e.getTime());
			// remove old one from ongoing
			if (oldAct != null)
				ongoignActs.remove(oldAct);
			ongoignActs.add(act);
		}

		if (firstOnTimeline) {
			// Update the timeline with new info
			SimpleTimeline tl = timelines.get(sensor.getName());
			tl.addVariable(act);
			tl.setStart(act, e.getTime());
			ongoignActs.add(act);
		}

		// add act --{FINISHES v DURING v OVERLAPPEDBY}--> [all ongoing]
		for (FuzzyActivity ongoing : ongoignActs) {
			if (!ongoing.equals(act)) {
				// FuzzyAllenIntervalConstraint fcNew = new
				// FuzzyAllenIntervalConstraint(solver, Type.Finishes);
				FuzzyAllenIntervalConstraint fcNew = new FuzzyAllenIntervalConstraint(FuzzyAllenIntervalConstraint.Type.Finishes,
						FuzzyAllenIntervalConstraint.Type.During, FuzzyAllenIntervalConstraint.Type.OverlappedBy);
				fcNew.setFrom(act);
				fcNew.setTo(ongoing);
				toAdd.add(fcNew);
				truthMaintenanceConstraints.add(fcNew);
			}
		}

		if (tcon != null) {
			// For all oldAct --{FINISHES v DURING v OVERLAPPEDBY}--> [all
			// ongoing]
			Vector<FuzzyAllenIntervalConstraint> noLongerToMaintain = new Vector<FuzzyAllenIntervalConstraint>();
			for (FuzzyAllenIntervalConstraint con : truthMaintenanceConstraints) {

				// if oldAct started later than x
				// (and since oldAct is now finished and x continues)
				// remove oldAct --{FINISHES v DURING v OVERLAPPEDBY}--> x
				// add oldAct --DURING--> x
				if (con.getFrom().equals(oldAct)) {
					toRetract.add(con);
					noLongerToMaintain.add(con);
					FuzzyAllenIntervalConstraint fcNew = new FuzzyAllenIntervalConstraint(FuzzyAllenIntervalConstraint.Type.During);
					fcNew.setFrom(oldAct);
					fcNew.setTo(con.getTo());
					toAdd.add(fcNew);
				}

				// if oldAct started earlier than x
				// (and since oldAct is now finished and x continues)
				// remove x --{FINISHES v DURING v OVERLAPPEDBY}--> oldAct
				// (i.e., oldAct --{FINISHEDBY v CONTAINS v OVERLAPS}--> x)
				// add oldAct --OVERLAPS--> x
				else if (con.getTo().equals(oldAct)) {
					toRetract.add(con);
					noLongerToMaintain.add(con);
					FuzzyAllenIntervalConstraint fcNew = new FuzzyAllenIntervalConstraint(FuzzyAllenIntervalConstraint.Type.Overlaps);
					fcNew.setFrom(oldAct);
					fcNew.setTo(con.getFrom());
					toAdd.add(fcNew);
				}
			}
			for (FuzzyAllenIntervalConstraint con : noLongerToMaintain)
				truthMaintenanceConstraints.remove(con);
		}

		if (!toRetract.isEmpty()) {
			solver.removeConstraints(toRetract
					.toArray(new FuzzyAllenIntervalConstraint[toRetract.size()]));
		}
		if (!toAdd.isEmpty()) {
			solver.addConstraints(toAdd
					.toArray(new FuzzyAllenIntervalConstraint[toAdd.size()]));
		}
	}

	/**
	 * Get the upper bound on the overall (temporal + value) consistency of
	 * the {@link FuzzyActivityNetwork}.
	 * @return Upper bound on the overall (temporal + value) consistency of
	 * the {@link FuzzyActivityNetwork}.
	 */
	public double getConsitency() {
		return Math.min(solver.getTemporalConsistency(),
				solver.getValueConsistency());
	}

	/**
	 * Get the {@link ConstraintNetwork} used by the meta-CSP's
	 * {@link FuzzyActivityNetworkSolver}.
	 * @return The {@link FuzzyActivityNetwork} used by the meta-CSP.
	 */
	public ConstraintNetwork getConstraintNetwork() {
		return solver.getConstraintNetwork();
	}

	
	
	
	/**
	 * IRAN: please comment this.
	 * @return IRAN: please comment this.
	 */
	public Vector<Constraint> getFalseClause(){
		return solver.getFalseClause();
	}

	/**
	 * IRAN: please comment this.
	 */
	public void resetFalseClause(){
		solver.resetFalseClauses();
	}
	
	private void setCrispCons(){
		solver.setCrispCons(solver.getConstraints());
	}

	public String getOptimalHypothesis(ConstraintNetwork optCn, double vc, double tc) {
		String str = "[";
		for (int i = 0; i < optCn.getVariables().length; i++) {
			for (int j = 0; j < heads.size(); j++) {
				if(optCn.getVariables()[i].getID() == heads.get(j).getID())
					 str += (ruleHeads.get(heads.get(j))).getHead() + " ";
			}
		}
		str += "] = " + " Value Consistency: " +  vc + " Temporal Consistency: " + tc;
		return str;
	}

	public double getValueConsistency() {
	
		return solver.getValueConsistency();
	}

	public double getTemporalConsistency() {
		// TODO Auto-generated method stub
		return solver.getTemporalConsistency();
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getEdgeLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEquivalent(Constraint c) {
		// TODO Auto-generated method stub
		return false;
	}

}

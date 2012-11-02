package onLineMonitoring;

import multi.fuzzyActivity.FuzzyActivity;
import multi.fuzzyActivity.FuzzyActivityNetworkSolver;
import fuzzyAllenInterval.FuzzyAllenIntervalConstraint;


public abstract class Sensor {
	
	private String name;
	private String[] states;
	private transient FuzzyActivityNetworkSolver solver;
	private double[] currentPossibilities;
	private FuzzyActivity currentAct = null;
	
	/**
	 * @return the currentAct
	 */
	public FuzzyActivity getCurrentAct() {
		return currentAct;
	}

	/**
	 * @param currentAct the currentAct to set
	 */
	public void setCurrentAct(FuzzyActivity currentAct) {
		this.currentAct = currentAct;
	}

	public Sensor(String name, String... states) {
		this.setName(name);
		this.setStates(states);
		currentPossibilities = new double[states.length];
		for (int i = 0; i < currentPossibilities.length; i++) currentPossibilities[i] = 0.0;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setStates(String[] states) {
		this.states = states;
	}

	public String[] getStates() {
		return states;
	}

	public void setSolver(FuzzyActivityNetworkSolver solver) {
		this.solver = solver;
	}

	public FuzzyActivityNetworkSolver getSolver() {
		return solver;
	}

	public FuzzyAllenIntervalConstraint setCurrentPossibilities(double[] possibilities) {
		
		FuzzyAllenIntervalConstraint tcon = null;
		
		boolean diff = false;
		for (int i = 0; i < currentPossibilities.length; i++) {
			if (currentPossibilities[i] != possibilities[i]) {
				diff = true;
				break;
			}
		}

		//Case: currentPossibilities have changed
		if (diff) {
			FuzzyActivity act = (FuzzyActivity)solver.createVariable(this.getName());
			act.setDomain(this.states, possibilities);
			this.currentPossibilities = possibilities;
			if (currentAct != null) {
				//tcon = new FuzzyAllenIntervalConstraint(solver, Type.Before, Type.Meets);
				tcon = new FuzzyAllenIntervalConstraint(FuzzyAllenIntervalConstraint.Type.Meets);
				tcon.setFrom(currentAct);
				tcon.setTo(act);
				
			}
			currentAct = act;
		 }
		
		return tcon;
	}

	public double[] getCurrentPossibilities() {
		return currentPossibilities;
	}
	

}

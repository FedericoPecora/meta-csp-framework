package onLineMonitoring;

import symbols.SymbolicValueConstraint;
import fuzzyAllenInterval.FuzzyAllenIntervalConstraint;

public class Requirement {
	
	private Sensor sensor;
	private SymbolicValueConstraint.Type vCons;
	private FuzzyAllenIntervalConstraint.Type tCons;
	private double[] possibilities;
	
	public Requirement(Sensor sensor, double[] possibilities, SymbolicValueConstraint.Type vCons, 
			FuzzyAllenIntervalConstraint.Type tCons) {
		this.setSensor(sensor);
		this.possibilities = possibilities;
		this.vCons = vCons;
		this.tCons = tCons;
	}
	
	public Requirement() {
		// TODO Auto-generated constructor stub
	}

	public double[] getPossibilities() { return possibilities; }
	/**
	 * @return the vCons
	 */
	public SymbolicValueConstraint.Type getvCons() {
		return vCons;
	}

	/**
	 * @param vCons the vCons to set
	 */
	public void setvCons(SymbolicValueConstraint.Type vCons) {
		this.vCons = vCons;
	}

	/**
	 * @return the tCons
	 */
	public FuzzyAllenIntervalConstraint.Type gettCons() {
		return tCons;
	}

	/**
	 * @param tCons the tCons to set
	 */
	public void settCons(FuzzyAllenIntervalConstraint.Type tCons) {
		this.tCons = tCons;
	}

	public void setSensor(Sensor sensor) {
		this.sensor = sensor;
	}

	public Sensor getSensor() {
		return sensor;
	}
	
	public String toString() {
		String state = "";
		for (int i = 0; i < this.getPossibilities().length; i++) {
			if (this.getPossibilities()[i] == 1.0) {
				state = this.getSensor().getStates()[i];
				break;
			}
		}
		return "" + this.getSensor().getName() + " {" + this.getvCons() + "," + this.gettCons() + "} " + state;
	}
	
}

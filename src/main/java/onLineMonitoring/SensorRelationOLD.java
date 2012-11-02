package onLineMonitoring;

import fuzzyAllenInterval.FuzzyAllenIntervalConstraint;

public class SensorRelationOLD {
	
	private SensorDataOLD[] sd = null;
	private FuzzyAllenIntervalConstraint.Type type;
	
	public SensorRelationOLD() {
		// TODO Auto-generated constructor stub
	}
	
	public SensorRelationOLD(SensorDataOLD[] sd, FuzzyAllenIntervalConstraint.Type t) {
		this.sd = sd;
		this.type = t;
		
	}
	
	public void setSd(SensorDataOLD[] sd) {
		this.sd = sd;
	}
	
	public void setType(FuzzyAllenIntervalConstraint.Type type) {
		this.type = type;
	}
	
	public SensorDataOLD[] getSd() {
		return sd;
	}
	
	public FuzzyAllenIntervalConstraint.Type getType() {
		return type;
	}
}

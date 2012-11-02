package onLineMonitoring;

import java.util.Collections;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Vector;

import multi.fuzzyActivity.FuzzyActivity;
import multi.fuzzyActivity.SimpleTimeline;
import time.APSPSolver;
import time.Bounds;
import framework.Constraint;
import framework.ConstraintNetwork;
import framework.Variable;
import fuzzyAllenInterval.FuzzyAllenIntervalConstraint;

public class Hypothesis implements Comparable<Hypothesis> {
	
	private double temporalConsistency;
	private double valueConsistency;
	private ConstraintNetwork constraintNetwork;
	private Rule rule;
	private Variable head;
	private int pass;
	private int id; 
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	
	public Hypothesis (double tc, double vc, ConstraintNetwork cn, Rule r, Variable head, int pass) {
		this.setTemporalConsistency(tc);
		this.setValueConsistency(vc);
		this.setConstraintNetwork(cn);
		this.setRule(r);
		this.head = head;
		this.pass = pass;
		this.id = 0;
	}

	public Variable getHead() {
		return head;
	}
	
	public int getPass() {
		return pass;
	}
	
	protected void setTemporalConsistency(double temporalConsistency) {
		this.temporalConsistency = temporalConsistency;
	}

	public double getTemporalConsistency() {
		return temporalConsistency;
	}

	protected void setValueConsistency(double valueConsistency) {
		this.valueConsistency = valueConsistency;
	}

	public double getValueConsistency() {
		return valueConsistency;
	}

	protected void setConstraintNetwork(ConstraintNetwork constraintNetwork) {
		this.constraintNetwork = constraintNetwork;
	}

	public ConstraintNetwork getConstraintNetwork() {
		return constraintNetwork;
	}

	protected void setRule(Rule rule) {
		this.rule = rule;
	}

	public Rule getRule() {
		return rule;
	}
		
	public String toCompactString() {
		int head = -1;
		double maxPoss = -1.0;
		for (int i = 0; i < rule.getPossibilities().length; i++) {
			if (rule.getPossibilities()[i] > maxPoss) {
				maxPoss = rule.getPossibilities()[i];
				head = i;
			}
		}
		String ret = rule.getComponent().getName() + "=" + rule.getComponent().getStates()[head]  + " (T,V)=(" + this.temporalConsistency + ", " + this.valueConsistency + ")";
		return ret;
	}
	
	public String toString() {
		int head = -1;
		double maxPoss = -1.0;
		for (int i = 0; i < rule.getPossibilities().length; i++) {
			if (rule.getPossibilities()[i] > maxPoss) {
				maxPoss = rule.getPossibilities()[i];
				head = i;
			}
		}
		String ret = rule.getComponent().getName() + " " + rule.getComponent().getStates()[head]  + "\n\t(Temporal, Value) consistency: (" + this.temporalConsistency + ", " + this.valueConsistency + ") TOT: " + getOverallConsistency();
		
		/****/
		//String acts = Arrays.toString(this.getConstraintNetwork().getVariables());
		//ret = ret + "\n" + acts;
		/****/
		
		return ret; 
	}
	
	public double getOverallConsistency() {
		return Math.min(this.getTemporalConsistency(), this.getValueConsistency());
	}
	
	public Bounds getInterval(HashMap<String,SimpleTimeline> tls) {
		//Vector<Long> startTimes = new Vector<Long>();
		//Vector<Long> endTimes = new Vector<Long>();
		Vector<Long> minStart = new Vector<Long>();
		Vector<Long> minEnd = new Vector<Long>();
		Vector<Long> maxStart = new Vector<Long>();
		Vector<Long> maxEnd = new Vector<Long>();
		
		for (Constraint con : this.constraintNetwork.getConstraints()) {
			if (con instanceof FuzzyAllenIntervalConstraint) {
				FuzzyAllenIntervalConstraint fc = (FuzzyAllenIntervalConstraint)con;
				FuzzyActivity act = (FuzzyActivity)fc.getTo();
				SimpleTimeline tl = tls.get(act.getComponent());
				long start = tl.getStart(act);
				long end = tl.getEnd(act);
				
				if (fc.containsType(FuzzyAllenIntervalConstraint.Type.After)) {
					minStart.add(end+1);
					//minEnd.add(end+1);//-
				}
				if (fc.containsType(FuzzyAllenIntervalConstraint.Type.Before)) {
					maxEnd.add(start-1);
					//maxStart.add(start-1);//-
				}
				if (fc.containsType(FuzzyAllenIntervalConstraint.Type.Contains)) {
					maxStart.add(start-1);
					minEnd.add(end+1);
				}
				if (fc.containsType(FuzzyAllenIntervalConstraint.Type.During)) {
					minStart.add(start+1);
					maxEnd.add(end-1);
				}
				if (fc.containsType(FuzzyAllenIntervalConstraint.Type.Equals)) {
					minStart.add(start);
					maxStart.add(start);
					minEnd.add(end);
					maxEnd.add(end);
				}
				// |----------| A FinishedBy
				//     |------| B
				if (fc.containsType(FuzzyAllenIntervalConstraint.Type.FinishedBy)) {
					maxStart.add(start-1);
					minEnd.add(end);
					maxEnd.add(end);
				}
				//     |------| A Finishes
				// |----------| B
				if (fc.containsType(FuzzyAllenIntervalConstraint.Type.Finishes)) {
					minStart.add(start+1);
					minEnd.add(end);
					maxEnd.add(end);					
				}
				if (fc.containsType(FuzzyAllenIntervalConstraint.Type.Meets)) {
					maxEnd.add(start);
					minEnd.add(start);
				}
				if (fc.containsType(FuzzyAllenIntervalConstraint.Type.MetBy)) {
					minStart.add(end);
					maxStart.add(end);
				}
				//     |------| A OverlappedBy
				// |------|     B				
				if (fc.containsType(FuzzyAllenIntervalConstraint.Type.OverlappedBy)) {
					maxStart.add(end-1);
					minStart.add(start+1);
					minEnd.add(end + 1);//Iran
				}
				// |------|     A Overlaps
				//     |------| B 				
				if (fc.containsType(FuzzyAllenIntervalConstraint.Type.Overlaps)) {
					minEnd.add(start+1);
					maxEnd.add(end-1);
					maxStart.add(start-1);//Iran
				}
				// |-----------| A StartedBy
				// |------|      B 								
				if (fc.containsType(FuzzyAllenIntervalConstraint.Type.StartedBy)) {
					minStart.add(start);
					maxStart.add(start);
					minEnd.add(end+1);
				}
				// |------|      A Starts
				// |-----------| B
				if (fc.containsType(FuzzyAllenIntervalConstraint.Type.Starts)) {
					minStart.add(start);
					maxStart.add(start);
					maxEnd.add(end-1);
				}				
			}
		}
		long latestStart, latestEnd, earliestStart, earliestEnd;
		
		try { earliestStart = Collections.min(minStart); }
		catch (NoSuchElementException e) { earliestStart = 0; }
		
		try { latestEnd = Collections.max(maxEnd); }
		catch (NoSuchElementException e) { latestEnd = APSPSolver.INF; }

		try { latestStart = Collections.max(maxStart); }
		catch (NoSuchElementException e) { latestStart = earliestStart; }

		try { earliestEnd = Collections.min(minEnd); }
		catch (NoSuchElementException e) { earliestEnd = latestEnd; }
		
		Bounds ret;
		
		
		//Bounds(latestStart, latestEnd); //is missing!
		try { ret = new Bounds(latestStart, earliestEnd); }
		catch(IllegalArgumentException e) {
			try { ret = new Bounds(earliestStart, earliestEnd); }
			catch(IllegalArgumentException e1) {
				try { ret = new Bounds(earliestStart, latestEnd); }
				//This should occur only if TC < 1.0
				catch(IllegalArgumentException e2) { ret = new Bounds(Math.min(earliestStart, latestEnd), Math.max(earliestStart, latestEnd)); }
			}
		}
		return ret;
	}

	/*
	public Interval getMaxInterval(HashMap<String,SimpleTimeline> tls) {
		Vector<Long> startTimes = new Vector<Long>();
		Vector<Long> endTimes = new Vector<Long>();
		for (Constraint con : this.constraintNetwork.getConstraints()) {
			if (con instanceof FuzzyAllenIntervalConstraint) {
				FuzzyAllenIntervalConstraint fc = (FuzzyAllenIntervalConstraint)con;
				FuzzyActivity act = (FuzzyActivity)fc.getTo();
				SimpleTimeline tl = tls.get(act.getComponent());
				long start = tl.getStart(act);
				long end = tl.getEnd(act);
				if (fc.containsType(FuzzyAllenIntervalConstraint.Type.After) ||
						fc.containsType(FuzzyAllenIntervalConstraint.Type.MetBy)) {
					startTimes.add(end);
				}
				else if (fc.containsType(FuzzyAllenIntervalConstraint.Type.OverlappedBy) ||
						fc.containsType(FuzzyAllenIntervalConstraint.Type.During) ||
						fc.containsType(FuzzyAllenIntervalConstraint.Type.Equals) ||
						fc.containsType(FuzzyAllenIntervalConstraint.Type.FinishedBy) ||
						fc.containsType(FuzzyAllenIntervalConstraint.Type.Starts) ||
						fc.containsType(FuzzyAllenIntervalConstraint.Type.StartedBy) ) {
					startTimes.add(start);
				}
				if (fc.containsType(FuzzyAllenIntervalConstraint.Type.Meets) ||
						fc.containsType(FuzzyAllenIntervalConstraint.Type.Before)) {
					endTimes.add(start);
				}
				else if (fc.containsType(FuzzyAllenIntervalConstraint.Type.Overlaps) ||
						fc.containsType(FuzzyAllenIntervalConstraint.Type.During) ||
						fc.containsType(FuzzyAllenIntervalConstraint.Type.Equals) ||
						fc.containsType(FuzzyAllenIntervalConstraint.Type.FinishedBy) ||
						fc.containsType(FuzzyAllenIntervalConstraint.Type.Finishes) ||
						fc.containsType(FuzzyAllenIntervalConstraint.Type.StartedBy)) {
					endTimes.add(end);
				}
			}
		}

		long minStart, maxEnd; 
		
		try { minStart = Collections.max(startTimes); }
		catch (NoSuchElementException e) { minStart = 0; }
		
		try { maxEnd = Collections.min(endTimes); }
		catch (NoSuchElementException e) { maxEnd = APSPSolver.INF; }

		return new Interval(null, minStart, maxEnd);

	}
	*/

	@Override
	public int compareTo(Hypothesis arg0) {
		if (this.getOverallConsistency() > arg0.getOverallConsistency()) return -1;
		if (this.getOverallConsistency() < arg0.getOverallConsistency()) return 1;
		return 0;
	}
	

}
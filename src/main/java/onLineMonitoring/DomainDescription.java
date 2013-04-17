/*******************************************************************************
 * Copyright (c) 2010-2013 Federico Pecora <federico.pecora@oru.se>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package onLineMonitoring;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Vector;

import multi.fuzzyActivity.FuzzyActivity;
import multi.fuzzyActivity.FuzzyActivityNetwork;
import multi.fuzzyActivity.FuzzyActivityNetworkSolver;
import multi.fuzzyActivity.SimpleTimeline;
import orbital.algorithm.Combinatorical;
import symbols.SymbolicValueConstraint;
import time.Bounds;
import framework.Constraint;
import framework.ConstraintNetwork;
import framework.Variable;
import fuzzyAllenInterval.FuzzyAllenIntervalConstraint;


public class DomainDescription {

	private Rule[] rules;
	private FuzzyActivityNetworkSolver solver;
	private Vector<FuzzyActivity> ongoignActs = new Vector<FuzzyActivity>();
	private Vector<FuzzyAllenIntervalConstraint> truthMaintenanceConstraints = new Vector<FuzzyAllenIntervalConstraint>();
	private long clockStart = -1;
	private boolean fastForward = true;
	private HashMap<String,SimpleTimeline> timelines = new HashMap<String,SimpleTimeline>();
	private Vector<Hypothesis> fixedHypotheses = new Vector<Hypothesis>();
	private Vector<FuzzyActivityNetwork> fixedNetworks = new Vector<FuzzyActivityNetwork>();
//	private Vector<String> sensorsNames = new Vector<String>();
//	private Vector<Sensor> sensors = new Vector<Sensor>();
	private HashMap<String,Sensor> sensors = new HashMap<String,Sensor>();
	private boolean optimize = false;
	private Vector<Rule> toSkip = new Vector<Rule>();
	
	//FIXME: Uninitialized read of solver in new onLineMonitoring.DomainDescription(Rule[])	DomainDescription.java	/MetaCSPFramework/src/onLineMonitoring	line 47	FindBugs Problem (Scariest)
	private FuzzyActivityNetwork inferredHypotheses = new FuzzyActivityNetwork(this.solver);
	
	private HypothesisListener hl = null;
	private double threshold = -1.0;
	private int maxHypotheses = -1;
	private boolean stopped = true;
	private boolean paused = false;
	private long pauseStart = 0;
	private long pauseDelta = 0;
	private int currentPass = 1;
	
	/*Iran*/
//	private Vector<Vector<Integer>> glbHypothesisDepList = new Vector<Vector<Integer>>();
//	private Vector<Vector<Boolean>> markDepList = new Vector<Vector<Boolean>>();   
	private boolean timelinestate = true;
	private Vector<Integer> firstLayer = new Vector<Integer>();
	private Vector<HypothesisNode> hypNodes = new Vector<HypothesisNode>();
//	private int hypCounter = 0;
	private Vector<FuzzyActivity> groundSensors = new Vector<FuzzyActivity>();
	private boolean firstCall = false;
	private HashMap<FuzzyActivity, HypothesisNode> hnodeshmap = new HashMap<FuzzyActivity, HypothesisNode>();
	private Vector<FuzzyAllenIntervalConstraint> crispCons = new Vector<FuzzyAllenIntervalConstraint>();
	/*Iran*/
		
	private Vector<Thread> threads = null;
	
	private Object criticalSection = new Object();

	public static enum OPTIONS {SIMULATE_SENSOR_DISPATCH, NO_SENSOR_DISPATCH};
	public static enum TIMELINEOPION{MAX_OVERALL_CONSISTENCY, MAX_TEMPORAL_CONSISTENCY, MAX_VALUE_CONSISTENCY}
//	private TIMELINEOPION TLO = TIMELINEOPION.MAX_OVERALL_CONSISTENCY;

	private class SensorWaitingThread extends Thread {
		private FuzzySensorEvent event;
		public SensorWaitingThread(FuzzySensorEvent e) {
			this.event = e;
			threads.add(this);
		}
		public void run() {
			while ((Calendar.getInstance().getTimeInMillis()-(pauseDelta))-clockStart < event.getTime()*1000 || paused) {
				if (stopped) break;
				try { Thread.sleep(100); } 
				catch (InterruptedException e) { e.printStackTrace(); }
			}
			if (!stopped) {
				System.out.println("Fired event: " + event);
				updateSensorData(event);
				triggerHypothesisListener();
			}
		}
	}

	public DomainDescription(Rule... rules) {
		solver = new FuzzyActivityNetworkSolver();
		this.setRules(rules);
		clockStart = Calendar.getInstance().getTimeInMillis();
		for (Rule r : rules) {
			//MonitoredComponent head = r.getComponent();
			//if (timelines.get(head.getName()) == null) timelines.put(head.getName(), new SimpleTimeline(head.getName()));
			for (Requirement req : r.getRequirements()) {
				req.getSensor().setSolver(solver);
				if (!sensors.keySet().contains(req.getSensor().getName())) sensors.put(req.getSensor().getName(), req.getSensor());
				if (timelines.get(req.getSensor().getName()) == null) timelines.put(req.getSensor().getName(), new SimpleTimeline(req.getSensor().getName()));
			}
		}
		firstLayer.add(-1);
	}
	
	public DomainDescription() {
		this(new Rule[0]);
	}
	
	public void addRule(Rule r) {
		Vector<Rule> rulesVec = new Vector<Rule>();
		rulesVec.addAll(Arrays.asList(this.rules));
		rulesVec.add(r);
		this.rules = rulesVec.toArray(new Rule[rulesVec.size()]);
		//MonitoredComponent head = r.getComponent();
		//if (timelines.get(head.getName()) == null) timelines.put(head.getName(), new SimpleTimeline(head.getName()));
		for (Requirement req : r.getRequirements()) {
			req.getSensor().setSolver(solver);
			if (!sensors.keySet().contains(req.getSensor().getName())) {
				sensors.put(req.getSensor().getName(), req.getSensor());
			}
			if (timelines.get(req.getSensor().getName()) == null) timelines.put(req.getSensor().getName(), new SimpleTimeline(req.getSensor().getName()));
		}
	}
		
	public void addRules(Rule[] rules) {
		this.setRules(rules);
		for (Rule r : rules) {
			//MonitoredComponent head = r.getComponent();
			//if (timelines.get(head.getName()) == null) timelines.put(head.getName(), new SimpleTimeline(head.getName()));
			for (Requirement req : r.getRequirements()) {
				req.getSensor().setSolver(solver);
				if (!sensors.keySet().contains(req.getSensor().getName())) {
					sensors.put(req.getSensor().getName(), req.getSensor());
				}
				if (timelines.get(req.getSensor().getName()) == null) timelines.put(req.getSensor().getName(), new SimpleTimeline(req.getSensor().getName()));
			}
		}
	}
	
	public void startMonitoring() {
		stopped = false;
		if (!fastForward) threads = new Vector<Thread>();
		clockStart = Calendar.getInstance().getTimeInMillis();
	}
	
	public void stopMonitoring() {
		stopped = true;
		boolean checkStopped = true;
		if (!fastForward) {
			do {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				for (Thread t : threads) {
					if (t.isAlive()) {
						checkStopped = false;
						//System.out.println(t.getName() + "is Alive!");
					}
				}
			} while (!checkStopped);
		}
		clockStart = -1;
	}
	
	public void pauseMonitoring() {
		if (!paused) {
			pauseStart = Calendar.getInstance().getTimeInMillis()-pauseDelta;
			paused = true;
		}
	}
	
	public void resumeMonitoring() {
		if (paused) {
			synchronized(criticalSection) {
				pauseDelta = (Calendar.getInstance().getTimeInMillis()-pauseStart);
				paused = false;
			}
		}
	}
	
	public long getTime() {
		if (paused) return (pauseStart-clockStart)/1000;
		return ((Calendar.getInstance().getTimeInMillis()-pauseDelta)-clockStart)/1000;
	}

	public void setOptions(OPTIONS opt) {
		if (opt.equals(OPTIONS.SIMULATE_SENSOR_DISPATCH)) fastForward = false;
		else if (opt.equals(OPTIONS.NO_SENSOR_DISPATCH)) fastForward = true;
	}

	public ConstraintNetwork getConstraintNetwork() {
		return solver.getConstraintNetwork();
	}

	public Hypothesis[] getBestHypotheses(int max) {
		Vector<Hypothesis> ret = new Vector<Hypothesis>();
		Vector<Hypothesis> bestSet = new Vector<Hypothesis>();
		for (Rule r : this.rules) {
			if (!toSkip.contains(r)) {
				System.out.println("rule: " + r);
				Hypothesis[] oneRule = getConsistency(r);
				if (oneRule != null) {
					Arrays.sort(oneRule);
					for (int i = 0; i < oneRule.length && i < max; i++) ret.add(oneRule[i]); 
					/**/
					//If there are ghost sensors, update them...
					Hypothesis best = ret.elementAt(0);
					if (best != null) bestSet.add(best);
					/**/	
				}
			}
		}
		if (!bestSet.isEmpty()) {
			for (Hypothesis best : bestSet) {
				this.addFuzzyInferredEvents(best);
				this.toSkip.add(best.getRule());
			}
			currentPass++;
			this.triggerHypothesisListener();
		}
		
		return ret.toArray(new Hypothesis[ret.size()]);
	}

	public Hypothesis[] getBestHypotheses(double threshold) {
		Vector<Hypothesis> ret = new Vector<Hypothesis>();
		Vector<Hypothesis> bestSet = new Vector<Hypothesis>();
		for (Rule r : this.rules) {
			if (!toSkip.contains(r)) {
				Hypothesis[] oneRule = getConsistency(r);
				if (oneRule != null) {
					Arrays.sort(oneRule);
					for (Hypothesis h : oneRule) {
						if (h.getOverallConsistency() >= threshold) ret.add(h);
						else break;
					}
					/**/
					//If there are ghost sensors, update them...
					Hypothesis best = ret.elementAt(0);
					if (best != null) bestSet.add(best);
					/**/	
				}
			}
		}
		if (!bestSet.isEmpty()) {
			for (Hypothesis best : bestSet) this.addFuzzyInferredEvents(best);
			this.triggerHypothesisListener();
		}
		return ret.toArray(new Hypothesis[ret.size()]);
	}

	//Make pruned graph from all possible hypotheses and find the maximum path which includes .. 
	//..the instance of each hypothesis....(it is not necessary the maximum one) 
	public Hypothesis[] getMaxTimeline() {
		
		Vector<Hypothesis> ret = new Vector<Hypothesis>();
		Vector<Hypothesis> bestSet = new Vector<Hypothesis>();
		if(!firstCall){	
			for (int i = 0; i < this.getVariables().length; i++) {
				groundSensors.add((FuzzyActivity)this.getVariables()[i]);
			}
			firstCall = true;
		}
		
		for (Rule r : this.rules) {
			if (!toSkip.contains(r)) {
				Hypothesis[] oneRule = getConsistency(r);
				if (oneRule != null) {
					//Arrays.sort(oneRule);
					for (int i = 0; i < oneRule.length; i++){
						if(oneRule[i].getOverallConsistency() > r.getThreshold()){
							ret.add(oneRule[i]);
							bestSet.add(oneRule[i]);
						}
					}	
				}
			}
		}
		
		if (!bestSet.isEmpty()) {
			for (Hypothesis best : bestSet) {
				this.addFuzzyInferredEvents(best);
				this.toSkip.add(best.getRule());
			}
			currentPass++;
			this.triggerHypothesisListener();
		}
		
		
		
		//get best timeline based on the best overall consistency of last layer!
		HypothesisNode bestNode = hypNodes.get(hypNodes.size() - 1); 
		for (int i = hypNodes.size() - 2; i < 0; i--) {
			if(hypNodes.get(i).getHyp().getPass() == currentPass){
				if(hypNodes.get(i).getSigmaOC() > bestNode.getSigmaOC()){
					bestNode = hypNodes.get(i);
				}
			}
		}
		
		
		/*System.out.println("best node: " + bestNode.toString());
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		Vector<HypothesisNode> paths = new  Vector<HypothesisNode>();
		paths = getPathByStartNode(bestNode);
		for (int i = 0; i < paths.size(); i++) {
			System.out.println(paths.get(i).getHyp());
			System.out.println(getMinInterval(paths.get(i).getHyp()));
		}
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
*/		return ret.toArray(new Hypothesis[ret.size()]);
	}//iran

	
	//find a path form the last layer of hypothesis(no hypothesis is depend on that) to the first layer(it is just based on sensory event)
//	private Vector<HypothesisNode> getPathByStartNode(HypothesisNode hn) {
//		Vector<HypothesisNode> tlp = new Vector<HypothesisNode>();
//		HashMap<HypothesisNode, Boolean> marks = new HashMap<HypothesisNode, Boolean>();
//		for (HypothesisNode h: hypNodes) {
//			marks.put(h, false);
//		}
//		Queue q = new LinkedList();
//		q.add(hn);
//		marks.put(hn, true);
//		while(!q.isEmpty()){
//			HypothesisNode temp = (HypothesisNode)q.remove();
//			for (int i = 0; i < temp.getFuzzyActivity().getDependencies().size(); i++) {
//				if(temp.getFuzzyActivity().getDependencies().get(i).IsHypothesis()){
//					if(!marks.get(hnodeshmap.get(temp.getFuzzyActivity().getDependencies().get(i)))){
//						//mark hypothesis node
//						//add to the queue	
//						marks.put(hnodeshmap.get(temp.getFuzzyActivity().getDependencies().get(i)),true);
//						q.add(hnodeshmap.get(temp.getFuzzyActivity().getDependencies().get(i)));
//					}
//				}			}
//			marks.put(temp, true);
//			tlp.add(temp);
//		}
//		return tlp;
//	}//iran

	public void addFuzzyInferredEvents(Hypothesis best) {
		Sensor ghostSensor = this.sensors.get("_" + best.getRule().getComponent().getName());
		if (ghostSensor != null) {
			double[] poss = best.getRule().getPossibilities();
			for (int i = 0; i < poss.length; i++) {
				if (poss[i] != 0.0) {
					poss[i] = best.getOverallConsistency();
				}
			}
			Vector<FuzzyActivity> dependencies = new Vector<FuzzyActivity>();
			//Add "ghost" sensor event with all its dependencies
			//this.addFuzzySensorEvents(new FuzzySensorEvent((PhysicalSensor)ghostSensor, poss, this.getMinInterval(best).start));
			synchronized(criticalSection) {
				FuzzyActivity act = (FuzzyActivity)solver.createVariable(ghostSensor.getName());
				act.setDomain(ghostSensor.getStates(), poss);
				inferredHypotheses.addVariable(act);
				Constraint[] cons = best.getConstraintNetwork().getConstraints();
								
				int hasHead = 0;
				for(Constraint con : cons) {
					//Variable[] newScope = new Variable[con.getScope().length];
					Variable[] oldScope = con.getScope();
					if (oldScope[0].equals(best.getHead())){
						hasHead++;
					}
					else{
						dependencies.add((FuzzyActivity)oldScope[0]);
					}
					if(oldScope[1].equals(best.getHead())){
						hasHead--;
					}
					else{
						dependencies.add((FuzzyActivity)oldScope[1]);
					}
					
					//con.setScope(newScope);
					//inferredHypotheses.addConstraint(con);
					
					//SymbolicValueConstraint
					if(hasHead == 1){
						FuzzyAllenIntervalConstraint contmp = new FuzzyAllenIntervalConstraint(((FuzzyAllenIntervalConstraint)con).getTypes());
						contmp.setFrom(act);
						contmp.setTo(oldScope[1]);
						this.solver.addConstraints(contmp);
						inferredHypotheses.addConstraint(contmp);
					}
					else if(hasHead == -1){
						FuzzyAllenIntervalConstraint contmp = new FuzzyAllenIntervalConstraint(((FuzzyAllenIntervalConstraint)con).getTypes());
						contmp.setFrom(oldScope[1]);
						contmp.setTo(act);
						this.solver.addConstraints(contmp);
						inferredHypotheses.addConstraint(contmp);
					}
					hasHead = 0;
				}
				//this.solver.addConstraints(cons);
				
				//Update the timeline with new info
				SimpleTimeline tl = timelines.get(ghostSensor.getName());
				tl.addVariable(act);
				long start = best.getInterval(timelines).min;
				long stop = best.getInterval(timelines).max;
				tl.setStart(act, start);
				tl.setEnd(act, stop);
				
				
				act.setIsHypothesis(true);//iran
				act.setDependencies(dependencies);
								
				double sigmaTC = best.getTemporalConsistency();
				double sigmaVC = best.getValueConsistency();
				double sigmaOC = best.getOverallConsistency();
				for (int i = 0; i < dependencies.size(); i++) {
					if(dependencies.get(i).IsHypothesis()){
						sigmaTC += hnodeshmap.get(dependencies.get(i)).getSigmaTC();
						sigmaVC += hnodeshmap.get(dependencies.get(i)).getSigmaVC();
						sigmaOC += hnodeshmap.get(dependencies.get(i)).getSigmaOC();
					}
				}
				//To store the path
				HypothesisNode hn = new HypothesisNode(act, sigmaTC, sigmaVC, sigmaOC, best);
				//System.out.println("hn" + hn.toString());
				hypNodes.add(hn);
				hnodeshmap.put(act, hn);

			}
		}	
	
	}
	
	public void addFuzzySensorEvents(FuzzySensorEvent... events) {
		if (!toSkip.isEmpty()) {
			toSkip = new Vector<Rule>();
			Constraint[] cons = inferredHypotheses.getConstraints();
			Variable[] vars = inferredHypotheses.getVariables();
			this.solver.removeConstraints(cons);
			this.solver.removeVariables(vars);
			inferredHypotheses = new FuzzyActivityNetwork(this.solver);
		}
		
		if (!fastForward) for (FuzzySensorEvent e : events) new SensorWaitingThread(e).start(); 
		else { 
			for (FuzzySensorEvent e : events)
				updateSensorData(e);
			this.triggerHypothesisListener();
		}
	}
	
	private void triggerHypothesisListener() {
		if (this.hl != null) {
			Hypothesis[] hypotheses = null;
			if(timelinestate)//iran
			{
				hypotheses = this.getMaxTimeline();
			}
			else if (this.threshold != -1.0) {
				hypotheses = this.getBestHypotheses(threshold);
			}
			else if (this.maxHypotheses != -1) {
				hypotheses = this.getBestHypotheses(maxHypotheses);
			}
			if (hypotheses != null) this.hl.processHypotheses(hypotheses);
		}
	}

	private void updateSensorData(FuzzySensorEvent e) {
		synchronized(criticalSection) {
			//Sensor sensor, double[] possibilities
			PhysicalSensor sensor = e.getSensor();
			double[] possibilities = e.getPossibilities();
			FuzzyActivity oldAct = sensor.getCurrentAct();

			boolean firstOnTimeline = false;
			if (oldAct == null) firstOnTimeline = true;
			
			//if changed then tcon will be != null
			FuzzyAllenIntervalConstraint tcon = sensor.setCurrentPossibilities(possibilities);
			
			//get new activity
			FuzzyActivity act = sensor.getCurrentAct();
			
			Vector<FuzzyAllenIntervalConstraint> toRetract = new Vector<FuzzyAllenIntervalConstraint>();
			Vector<FuzzyAllenIntervalConstraint> toAdd = new Vector<FuzzyAllenIntervalConstraint>();

			if (tcon != null) {
				toAdd.add(tcon);
				//Update the timeline with new info
				SimpleTimeline tl = timelines.get(sensor.getName());
				if (oldAct != null) tl.setEnd(oldAct, e.getTime());
				tl.addVariable(act);
				tl.setStart(act, e.getTime());
				//remove old one from ongoing
				if (oldAct != null) ongoignActs.remove(oldAct);
				ongoignActs.add(act);
			}
			
			if (firstOnTimeline) {
				//Update the timeline with new info
				SimpleTimeline tl = timelines.get(sensor.getName());
				tl.addVariable(act);
				tl.setStart(act, e.getTime());
				ongoignActs.add(act);
			}
			
			//add act --{FINISHES v DURING v OVERLAPPEDBY}--> [all ongoing]
			for (FuzzyActivity ongoing : ongoignActs) {
				if (!ongoing.equals(act)) {
					//FuzzyAllenIntervalConstraint fcNew = new FuzzyAllenIntervalConstraint(solver, Type.Finishes);
					FuzzyAllenIntervalConstraint fcNew = new FuzzyAllenIntervalConstraint(FuzzyAllenIntervalConstraint.Type.Finishes, 
							FuzzyAllenIntervalConstraint.Type.During, FuzzyAllenIntervalConstraint.Type.OverlappedBy);
					fcNew.setFrom(act);
					fcNew.setTo(ongoing);
					toAdd.add(fcNew);
					truthMaintenanceConstraints.add(fcNew);
				}
			}

			if (tcon != null) {
				//For all oldAct --{FINISHES v DURING v OVERLAPPEDBY}--> [all ongoing]
				Vector<FuzzyAllenIntervalConstraint> noLongerToMaintain = new Vector<FuzzyAllenIntervalConstraint>();
				for (FuzzyAllenIntervalConstraint con : truthMaintenanceConstraints) {
					
					//if oldAct started later than x
					//(and since oldAct is now finished and x continues)
					//remove oldAct --{FINISHES v DURING v OVERLAPPEDBY}--> x
					//add oldAct --DURING--> x
					if (con.getFrom().equals(oldAct)) {
						toRetract.add(con);
						noLongerToMaintain.add(con);
						FuzzyAllenIntervalConstraint fcNew = new FuzzyAllenIntervalConstraint(FuzzyAllenIntervalConstraint.Type.During);
						fcNew.setFrom(oldAct);
						fcNew.setTo(con.getTo());
						toAdd.add(fcNew);
					}
					
					//if oldAct started earlier than x
					//(and since oldAct is now finished and x continues)
					//remove x --{FINISHES v DURING v OVERLAPPEDBY}--> oldAct
					//(i.e., oldAct --{FINISHEDBY v CONTAINS v OVERLAPS}--> x)
					//add oldAct --OVERLAPS--> x
					else if (con.getTo().equals(oldAct)) {
						toRetract.add(con);
						noLongerToMaintain.add(con);
						FuzzyAllenIntervalConstraint fcNew = new FuzzyAllenIntervalConstraint(FuzzyAllenIntervalConstraint.Type.Overlaps);
						fcNew.setFrom(oldAct);
						fcNew.setTo(con.getFrom());
						toAdd.add(fcNew);
					}
				}
				for (FuzzyAllenIntervalConstraint con : noLongerToMaintain) truthMaintenanceConstraints.remove(con);
			}
								


			if (!toRetract.isEmpty()) {	solver.removeConstraints(toRetract.toArray(new FuzzyAllenIntervalConstraint[toRetract.size()])); }
			if (!toAdd.isEmpty()) {
				crispCons.addAll(toAdd);	
				solver.addConstraints(toAdd.toArray(new FuzzyAllenIntervalConstraint[toAdd.size()]));
			}			
		}
	}


	private void setRules(Rule[] rules) {
		this.rules = rules;
	}


	public Rule[] getRules() {
		return rules;
	}
	
	
	/**
	 * Returns temporal and symbolic degrees of satisfaction
	 * as two elements of a double array.
	 * @param r The rule to evaluate.
	 * @return Double array containing temporal and symbolic degrees of satisfaction
	 * of the given rule.
	 */
	private Hypothesis[] getConsistency(Rule r) {
		synchronized(criticalSection) {
			boolean impossibleReq = false;
			Vector<Hypothesis> ret = new Vector<Hypothesis>();
			
			//OPT
			if (optimize) ret.addAll(fixedHypotheses);
			
			MonitoredComponent component = r.getComponent();
			//create prototype activity
			FuzzyActivity head = (FuzzyActivity)solver.createVariable(component.getName());
			head.setDomain(component.getStates(), r.getPossibilities());
			//System.out.println("Created HEAD: " + head);
			Vector<Vector<ConstraintNetwork>> constraints = new Vector<Vector<ConstraintNetwork>>();
			Vector<FuzzyActivity> cleanupActs = new Vector<FuzzyActivity>();
			cleanupActs.add(head);
			HashMap<Sensor, Vector<Variable>> sensorVariables = new HashMap<Sensor, Vector<Variable>>();
			
			for (Requirement req : r.getRequirements()) {
				Sensor sens = req.getSensor();
				if (sensorVariables.get(sens) == null) {
					Vector<Variable> vec = new Vector<Variable>();
					Variable[] vars = solver.getVariables(sens.getName());
					if (vars != null) {
						for (Variable var : vars) vec.add(var);
						sensorVariables.put(sens, vec);
					}
				}
			}
			
			Vector<FuzzyActivity> dependenciesFzAct = new Vector<FuzzyActivity>();//iran and it is required
//			Vector<Integer> dependenciesHyp = new Vector<Integer>();//iran
			Vector<FuzzyActivity> marksAsgeneralReq = new Vector<FuzzyActivity>();
			
			for (Requirement req : r.getRequirements()) {
				Sensor sens = req.getSensor();
				//Variable[] sensVars = solver.getVariables(sens.getName());
				Vector<Variable> vec = sensorVariables.get(sens);
				if (vec == null) {
					impossibleReq = true;
					break;
				}
				Variable[] sensVars = vec.toArray(new Variable[vec.size()]);
				//System.out.println("GOT VARS for SENSOR " + sens.getName() + ": " + Arrays.toString(sensVars));				
				Vector<ConstraintNetwork> unifications = new Vector<ConstraintNetwork>();
				for (Variable sensVar : sensVars) {
					FuzzyActivity sensAct = (FuzzyActivity)sensVar;
					FuzzyActivityNetwork oneUnification = new FuzzyActivityNetwork(null);

					//make temporal constraint (from: head, to: sensAct)
					//of type req.gettCons()
					FuzzyAllenIntervalConstraint tcon = new FuzzyAllenIntervalConstraint(req.gettCons());
					tcon.setFrom(head);
					tcon.setTo(sensAct);
	
					//make value requirement and corresponding valueConstraint (from: head, to: sensAct)
					//of type req.getvCons()
					FuzzyActivity reqValue = (FuzzyActivity)solver.createVariable(sens.getName());
					reqValue.setDomain(sens.getStates(), req.getPossibilities());
					//System.out.println("Created REQVALUE: " + reqValue.toString());
					SymbolicValueConstraint reqValueCon = new SymbolicValueConstraint(req.getvCons());
					reqValueCon.setFrom(reqValue);
					reqValueCon.setTo(sensAct);
					
					//iran
					//Store the head and req for the further extraction of subgraph from the whole network
					if(sensAct.IsHypothesis()) 
						dependenciesFzAct.add(sensAct);
					marksAsgeneralReq.add(reqValue);
					marksAsgeneralReq.add(head);
					
					//add them to oneUnification
					oneUnification.addVariable(head);
					oneUnification.addVariable(sensAct);
					oneUnification.addConstraint(tcon);
					oneUnification.addVariable(reqValue);
					oneUnification.addConstraint(reqValueCon);
	
					//make sure we clean up properly afterwards!
					cleanupActs.add(reqValue);
										
					//add oneUnification to unifications
					unifications.add(oneUnification);
				}				
				constraints.add(unifications);
			}
			
			//OPT
			Vector<FuzzyActivityNetwork> newFixedNetworks = null;
			if (optimize) newFixedNetworks = new Vector<FuzzyActivityNetwork>();
			
			if (!impossibleReq) {
				Vector<ConstraintNetwork> toAttempt = new Vector<ConstraintNetwork>();
				int max = 0;
				for (Vector<ConstraintNetwork> vcn : constraints) {
					if (vcn.size() > max) max = vcn.size();
				}
				
				Combinatorical c = Combinatorical.getPermutations(constraints.size(), max, true);
		
				while (c.hasNext()) {
					int[] combination = c.next();
					//System.out.println("Doing " + Arrays.toString(combination));
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
						
						//OPT
						if (optimize) {
							FuzzyActivityNetwork filtered = new FuzzyActivityNetwork(null);
							for (Variable v : oneAttempt.getVariables()) {
								if (sensors.keySet().contains(solver.getComponent(v)) && !cleanupActs.contains(v)) filtered.addVariable(v);
							}
							if (!fixedNetworks.contains(filtered)) {
								toAttempt.add(oneAttempt);
								boolean allFixed = true;
								for (Variable var : filtered.getVariables()) if (ongoignActs.contains(var)) allFixed = false;
								if (allFixed) {
									fixedNetworks.add(filtered);
									newFixedNetworks.add(oneAttempt);
								}
							}
							//else System.out.println("SKIPPED");
						}
						else {
							toAttempt.add(oneAttempt);
						}
					}
				}
		
				/*
				System.out.println("==============");
				for (ConstraintNetwork cn : toAttempt) {
					System.out.println("MUST ATTEMPT:\n" + cn);
				}
				System.out.println("==============");
				 */
		
				if (toAttempt == null || toAttempt.isEmpty()) {
					//solver.removeVariable(cleanupActs.toArray(new FuzzyActivity[cleanupActs.size()]));
					for (Variable v : cleanupActs) solver.removeVariable(v);
					return null;
				}
				//System.out.println("----------> ATTEMPTING " + toAttempt.size() + " UNIFICATIONS");
				for (ConstraintNetwork cn : toAttempt) {
					//iran
					//to retrieve ghostActivity from network in order to build dependency vector!
					
					Constraint[] toPropagate = cn.getConstraints();
					
					//iran
					Vector<FuzzyActivity> marksAsSensReq = new Vector<FuzzyActivity>();
					if(currentPass > 1){
						for (Constraint con : toPropagate) {
							extractDependencies(marksAsSensReq, (FuzzyActivity)con.getScope()[0]);
							extractDependencies(marksAsSensReq, (FuzzyActivity)con.getScope()[1]);
						}
						marksAsSensReq.addAll(marksAsgeneralReq);
						marksAsSensReq.addAll(groundSensors);
						solver.setVarOfSubGraph(marksAsSensReq);
						solver.setCrispCons(crispCons.toArray(new Constraint[crispCons.size()]));
					}
					
					//iran						
					/*for (Constraint con : toPropagate) {
						System.out.println("TRYING: " + con);
					}*/
					
					solver.addConstraints(toPropagate);
					marksAsSensReq.clear();//iran
					//System.out.println("THE CONSTRIANTS: " + toPropagate.length);
					
//					hypCounter++;
					double tc = solver.getTemporalConsistency();
					double vc = solver.getValueConsistency();
					Hypothesis h = new Hypothesis(tc, vc, cn, r, head, currentPass);
					
					System.out.println("hypothesis: " + h);
					String outS = " (pass " + h.getPass() + "): " +
							"\n\tminInterval = " + this.getMinInterval(h); 
					System.out.println(outS);
										
					
					System.out.println("..........................................................");
					ret.add(h);
					
					//OPT
					if (optimize && newFixedNetworks.contains(cn)) {
						fixedHypotheses.add(h);
						//System.out.println("Added fixed " + h);
					}
					
					//System.out.println("THE CONSTRIANTS (1): " + toPropagate.length);
					solver.removeConstraints(toPropagate);
					//iran
				}
			}
			
			for (Variable v : cleanupActs) solver.removeVariable(v);
					
			if (impossibleReq) return null;
			
			System.out.println("=====================================================");//iran
			return ret.toArray(new Hypothesis[ret.size()]);
			
		}
	}

	private void extractDependencies(Vector<FuzzyActivity> marksAsSensReq,
			FuzzyActivity fa) {
		if(fa.getDependencies().size() == 0)
			return;
		if(!marksAsSensReq.contains(fa))
			marksAsSensReq.add(fa);
		for (int i = 0; i < fa.getDependencies().size(); i++) {
			if(!marksAsSensReq.contains(fa.getDependencies().get(i))){
				marksAsSensReq.add(fa.getDependencies().get(i));
				extractDependencies(marksAsSensReq, fa.getDependencies().get(i));
			}
		}
		
	}

	
	public void drawNetwork() {
		ConstraintNetwork.draw(this.solver.getConstraintNetwork());
	}

	public SimpleTimeline getTimeline(Sensor s) { return timelines.get(s.getName()); }

	public SimpleTimeline[] getTimelines() { return timelines.values().toArray(new SimpleTimeline[timelines.values().size()]); }

	public Bounds getMinInterval(Hypothesis h) {
		return h.getInterval(timelines);
	}
	
	/*
	public Interval getMaxInterval(Hypothesis h) {
		return h.getMaxInterval(timelines);
	}
	*/

	public void registerHypothesisListener(HypothesisListener hl, double threshold) {
		this.hl = hl;
		this.threshold = threshold;
		this.maxHypotheses = -1;
	}

	public void registerHypothesisListener(HypothesisListener hl, int maxHypotheses) {
		this.hl = hl;
		this.threshold = -1.0;
		this.maxHypotheses = maxHypotheses;
	}
	
	public Variable[] getVariables() {
		return this.solver.getVariables();
	}
	
	public Constraint[] getConstraints() {
		return this.solver.getConstraints();
	}
	

}

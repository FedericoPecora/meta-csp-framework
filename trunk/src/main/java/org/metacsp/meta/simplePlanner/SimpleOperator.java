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
package org.metacsp.meta.simplePlanner;

import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.time.Bounds;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

public class SimpleOperator {

	protected String head;
	protected AllenIntervalConstraint[] requirementConstraints;
	protected String[] requirementActivities;
	protected int[] usages;
	protected AllenIntervalConstraint[][] extraConstraints;
	
	private static enum ReservedWord {Head, SimpleOperator, Resource, Constraint, SimpleDomain, RequiredResource, RequiredState};
	
	/**
	 * Creates a {@link SimpleOperator} from a textual specification (used by the
	 * domain parser {@link SimpleDomain#parseDomain(SimplePlanner, String)}.
	 * @param textualSpecification A textual specification of an operator
	 * @return a {@link SimpleOperator} build according to the textual specification.
	 */
	public static SimpleOperator parseSimpleOperator(String textualSpecification, String[] resources) {
		HashMap<String,String> requiredStates = new HashMap<String, String>();
		String head = null;
		Vector<AllenIntervalConstraint> constraints = new Vector<AllenIntervalConstraint>();
		Vector<String> froms = new Vector<String>();
		Vector<String> tos = new Vector<String>();
		String[] args = null;
		int[] resourceRequirements = new int[resources.length];
		
		String[] split = textualSpecification.split("\\(");
		boolean inConstraint = false;
		boolean headInConstraint = false;
		for (int i = 0; i < split.length; i++) {
			String oneElement = split[i];
			if (oneElement != null) {
				if (oneElement.trim().equals("")) split[i] = null;
				else {
					split[i] = "(" + split[i];
					boolean found = false;
					for (ReservedWord reservedWord : ReservedWord.values()) {
						if (oneElement.contains(reservedWord.toString())) {
							if (reservedWord.equals(ReservedWord.Constraint)) {
								inConstraint = true;
							}
							found = true;
							if (reservedWord.equals(ReservedWord.Head) && inConstraint) {
								headInConstraint = true;
								inConstraint = false;
							}
							break;
						}				
					}
					if (!found || headInConstraint) {
						//join two split elements
						split[i-1] = split[i-1] + split[i];
						split[i] = null;
						if (headInConstraint) headInConstraint = false;
					}	
				}
			}
		}
		
		for (int i = 0; i < split.length; i++) {
			String oneElement = split[i];
			if (oneElement != null) {
				ReservedWord rv = null;
				for (ReservedWord reservedWord : ReservedWord.values()) {
					if (oneElement.substring(oneElement.indexOf("(")+1).trim().startsWith(reservedWord.toString())) {
						rv = reservedWord;
						break;
					}				
				}
				if (rv.equals(ReservedWord.SimpleOperator)) {
					//do nothing
				}
				else if (rv.equals(ReservedWord.Head)) {
					head = oneElement.substring(oneElement.indexOf("Head")+4,oneElement.lastIndexOf(")")).trim();
					String argString = head.substring(head.indexOf("(")+1,head.indexOf(")"));
					if (!argString.trim().equals("")) {
						args = argString.split(",");
						//resources = argString.split(",");
						//resourceRequirements = new int[resources.length];
					}
				}
				else if (rv.equals(ReservedWord.RequiredState)) {
					String reqKey = oneElement.substring(oneElement.indexOf("RequiredState")+13).trim();
					//reqKey = "req1 LaserScanner1::On())"
					String req = null;
					req = reqKey.substring(reqKey.indexOf(" "),reqKey.lastIndexOf(")")).trim();
					reqKey = reqKey.substring(0,reqKey.indexOf(" ")).trim();
					//reqKey = "req1"
					requiredStates.put(reqKey,req);
				}
				else if (rv.equals(ReservedWord.RequiredResource)) {
					String reqKey = oneElement.substring(oneElement.indexOf("RequiredResource")+16).trim();
					//reqKey = "power(5))"
					String requiredResource = reqKey.substring(0,reqKey.indexOf("(")).trim();
					int requiredAmount = Integer.parseInt(reqKey.substring(reqKey.indexOf("(")+1,reqKey.indexOf(")")).trim());
					for (int k = 0; k < resources.length; k++) {
						if (resources[k].equals(requiredResource)) {
							resourceRequirements[k] = requiredAmount;
						}
					}
				}
				else if (rv.equals(ReservedWord.Constraint)) {
					String aux = oneElement.substring(oneElement.indexOf("Constraint")+10);
					String constraintName = null;
					Vector<Bounds> bounds = null;
					if (aux.contains("[")) {
						constraintName = aux.substring(0,aux.indexOf("[")).trim();
						String boundsString = aux.substring(aux.indexOf("["),aux.indexOf("]")+1);
						String[] splitBounds = boundsString.split("\\[");
						bounds = new Vector<Bounds>();
						for (String oneBound : splitBounds) {
							if (!oneBound.trim().equals("")) {
								String lbString = oneBound.substring(oneBound.indexOf("[")+1,oneBound.indexOf(",")).trim();
								String ubString = oneBound.substring(oneBound.indexOf(",")+1,oneBound.indexOf("]")).trim();
								long lb, ub;
								if (lbString.equals("INF")) lb = org.metacsp.time.APSPSolver.INF;
								else lb = Long.parseLong(lbString);
								if (ubString.equals("INF")) ub = org.metacsp.time.APSPSolver.INF;
								else ub = Long.parseLong(ubString);
								bounds.add(new Bounds(lb,ub));
							}
						}
					}
					else {
						constraintName = aux.substring(0,aux.indexOf("(")).trim();
					}
					String from = null;
					String to = null;
					if (constraintName.equals("Duration")) {
						from = aux.substring(aux.indexOf("(")+1, aux.indexOf(")")).trim();
						to = from;
					}
					else {
						from = aux.substring(aux.indexOf("(")+1, aux.indexOf(",")).trim();
						to = aux.substring(aux.indexOf(",")+1, aux.indexOf(")")).trim();
					}
					AllenIntervalConstraint con = null;
					if (bounds != null) con = new AllenIntervalConstraint(AllenIntervalConstraint.Type.valueOf(constraintName),bounds.toArray(new Bounds[bounds.size()]));
					else con = new AllenIntervalConstraint(AllenIntervalConstraint.Type.valueOf(constraintName));
					constraints.add(con);
					froms.add(from);
					tos.add(to);
				}
			}
		}
		
		class AdditionalConstraint {
			AllenIntervalConstraint con;
			int from, to;
			public AdditionalConstraint(AllenIntervalConstraint con, int from, int to) {
				this.con = con;
				this.from = from;
				this.to = to;
			}
			public void addAdditionalConstraint(SimpleOperator op) {
				op.addConstraint(con, from, to);
			}
		}
		
		//What I have:
		//constraints = {During, Duration, Before}
		//froms = {Head, Head, req1}
		//tos = {req1, Head, req2}
		//requirements = {req2 = Robot1::At(room), req1 = Robot1::MoveTo()}

		int reqCounter = 0;
		
		//pass this to constructor
		String[] requirementStrings = new String[requiredStates.keySet().size()];
		//pass this to constructor
		AllenIntervalConstraint[] consFromHeadtoReq = new AllenIntervalConstraint[requiredStates.keySet().size()];
		Vector<AdditionalConstraint> acs = new Vector<AdditionalConstraint>();
		
		for (String reqKey : requiredStates.keySet()) {
			String requirement = requiredStates.get(reqKey);
			requirementStrings[reqCounter] = requirement;
			for (int i = 0; i < froms.size(); i++) {
				if (froms.elementAt(i).equals("Head") && tos.elementAt(i).equals(reqKey)) {
					consFromHeadtoReq[reqCounter] = constraints.elementAt(i);
				}
			}
			reqCounter++;
		}

		//What I have:
		//constraints = {During, Duration, Before}
		//froms = {Head, Head, req1}
		//tos = {req1, Head, req2}
		//requirements = {req2 = Robot1::At(room), req1 = Robot1::MoveTo()}
		//requirementStrings = [Robot1::At(room), Robot1::MoveTo()]
		//consFromHeadtoReq = [During,null]
		
		//addConstraint(durationMoveTo, 0, 0);
		//addConstraint(beforeReq1Req2, 1, 2);
		
		for (int i = 0; i < froms.size(); i++) {
			if (froms.elementAt(i).equals("Head") && tos.elementAt(i).equals("Head")) {
				AdditionalConstraint ac = new AdditionalConstraint(constraints.elementAt(i), 0, 0);
				acs.add(ac);
			}
			else if (!froms.elementAt(i).equals("Head") && !tos.elementAt(i).equals("Head")) {
				String reqFromKey = froms.elementAt(i);
				String reqToKey = tos.elementAt(i);
				int reqFromIndex = -1;
				int reqToIndex = -1;
				AllenIntervalConstraint con = constraints.elementAt(i);
				String reqFrom = requiredStates.get(reqFromKey);
				String reqTo = requiredStates.get(reqToKey);
				for (int j = 0; j < requirementStrings.length; j++) {
					if (requirementStrings[i].equals(reqFrom)) reqFromIndex = j;
					if (requirementStrings[i].equals(reqTo)) reqToIndex = j;
				}
				AdditionalConstraint ac = new AdditionalConstraint(con, reqFromIndex, reqToIndex);
				acs.add(ac);
			}
		}
		
		//Call constructor
		SimpleOperator ret = new SimpleOperator(head,consFromHeadtoReq,requirementStrings,resourceRequirements);
		for (AdditionalConstraint ac : acs) ac.addAdditionalConstraint(ret);
		return ret;
	}

	public SimpleOperator(String head, AllenIntervalConstraint[] requirementConstraints, String[] requirementActivities, int[] usages) {
		this.head = head;
		if (requirementActivities != null) {
			for (String a : requirementActivities) {
				if (a.equals(head)) throw new InvalidActivityException(a);
			}
		}
		this.requirementConstraints = requirementConstraints;
		this.requirementActivities = requirementActivities;
		this.usages = usages;
		if (requirementConstraints != null) this.extraConstraints = new AllenIntervalConstraint[requirementActivities.length+1][requirementActivities.length+1];
		else this.extraConstraints = new AllenIntervalConstraint[1][1];
	}
	
	public void addConstraint(AllenIntervalConstraint c, int from, int to) {
		extraConstraints[from][to] = c;
	}
	
	public AllenIntervalConstraint[][] getExtraConstraints() {
		return this.extraConstraints;
	}
	
	public String getHead() {
		return head;
	}

	public AllenIntervalConstraint[] getRequirementConstraints() {
		return requirementConstraints;
	}

	public String[] getRequirementActivities() {
		return requirementActivities;
	}

	public int[] getUsages() {
		return usages;
	}

	public String toString() {
		String ret = "";
		String acts = "";
		if (requirementActivities != null) {
			for (int i = 0; i < requirementActivities.length; i++) {
				acts += head + " --" + Arrays.toString(requirementConstraints[i].getTypes()) + " " + Arrays.toString(requirementConstraints[i].getBounds()) + "--> " + requirementActivities[i];
				if (i != requirementActivities.length-1) acts += "\n";
			}
		}
		if (!acts.trim().equals("")) ret += acts;
		if (usages != null) {
			if (!acts.trim().equals("")) ret += "\n";
			ret += head + " usage: " + Arrays.toString(usages);
		}
		String extraCons = "";
		if (extraConstraints != null) {
			extraCons += "\n";
			for (int i = 0; i < extraConstraints.length; i++) {
				for (int j = 0; j < extraConstraints[i].length; j++) {
					if (extraConstraints[i][j] != null) {
						if (i == 0) extraCons += head;
						else extraCons += requirementActivities[i-1];
						extraCons += " --" + Arrays.toString(extraConstraints[i][j].getTypes()) + " " + Arrays.toString(extraConstraints[i][j].getBounds()) + "--> ";
						if (j == 0) extraCons += head;
						else extraCons += requirementActivities[j-1];
					}
				}				
			}
		}
		if (!extraCons.trim().equals("")) ret += extraCons;
		return ret;
	}
	
	
}

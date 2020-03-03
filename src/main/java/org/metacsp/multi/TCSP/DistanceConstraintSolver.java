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
package org.metacsp.multi.TCSP;

import java.util.ArrayList;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiConstraintSolver;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;
import org.metacsp.time.qualitative.QualitativeAllenIntervalConstraint;
import org.metacsp.time.qualitative.QualitativeAllenIntervalConstraint.Type;

public class DistanceConstraintSolver extends MultiConstraintSolver {

	private static final long serialVersionUID = -8474292073131422005L;
	private MultiTimePoint source = null;
	private MultiTimePoint sink = null;
	
	private ConstraintNetwork completeNetwork = null;
	
	public DistanceConstraintSolver(long origin, long horizon) {
		super(new Class[]{DistanceConstraint.class}, MultiTimePoint.class, createConstraintSolvers(origin, horizon), new int[] {1});	
		//Create source and sink as wrappers of APSPSolver's source and sink
		APSPSolver internalSolver = (APSPSolver)this.constraintSolvers[0];
		source = new MultiTimePoint(this, IDs++, constraintSolvers, new Variable[] {internalSolver.getSource()});
		sink = new MultiTimePoint(this, IDs++, constraintSolvers, new Variable[] {internalSolver.getSink()});
		this.theNetwork.addVariable(source);
		this.theNetwork.addVariable(sink);
		this.setOptions(OPTIONS.ALLOW_INCONSISTENCIES);		
	}

	private static ConstraintSolver[] createConstraintSolvers(long origin, long horizon) {
		APSPSolver stpSolver = new APSPSolver(origin, horizon);
		return new ConstraintSolver[] {stpSolver};
	}
	
//	@Override
//	protected Variable[] createVariablesSub(int num) {
//		MultiTimePoint[] ret = new MultiTimePoint[num];
//		for (int i = 0; i < num; i++) {
//			ret[i] = new MultiTimePoint(this, IDs++, this.constraintSolvers);
//		}
//		return ret;
//	}
	
	private void createCompleteNetwork() {
		this.completeNetwork = new ConstraintNetwork(this);
		ConstraintNetwork originalNetwork = this.getConstraintNetwork();
		for (Variable var : originalNetwork.getVariables()) this.completeNetwork.addVariable(var);
		//for (Constraint con : originalNetwork.getConstraints()) this.completeNetwork.addConstraint(con);
		Variable[] vars = this.completeNetwork.getVariables();
		for (int i = 0; i < vars.length; i++) {
			for (int j = 0; j < vars.length; j++) {
				if (i != j) {
					if (originalNetwork.getConstraint(vars[i],vars[j]) == null) {
						if (originalNetwork.getConstraint(vars[j],vars[i]) != null) {
							//add inverse
							DistanceConstraint orig = (DistanceConstraint)originalNetwork.getConstraint(vars[j],vars[i]);
							Bounds[] origBounds = orig.getBounds();
							Bounds[] inverseBounds = new Bounds[origBounds.length];
							for (int k = 0; k < origBounds.length; k++) {
								// i --[10,20]--> j ==> i --(20)--> j + j --(-10)--> i
								// j --[-20,-10]--> i ==> j --(-10)--> i + i --(20)--> j
								inverseBounds[k] = new Bounds(-origBounds[k].max, -origBounds[k].min);
							}
							DistanceConstraint inverse = new DistanceConstraint(inverseBounds);
							inverse.setFrom(vars[i]);
							inverse.setTo(vars[j]);
							this.completeNetwork.addConstraint(inverse);
						}
						else {
							//create universal relation
							Bounds universalBounds = new Bounds(-APSPSolver.INF, APSPSolver.INF);
							DistanceConstraint universal = new DistanceConstraint(universalBounds);
							universal.setFrom(vars[i]);
							universal.setTo(vars[j]);
							this.completeNetwork.addConstraint(universal);
						}
					}
					else {
						//add copy of constraint to complete network
						DistanceConstraint orig = (DistanceConstraint)originalNetwork.getConstraint(vars[i],vars[j]);
						Bounds[] origBounds = orig.getBounds();
						Bounds[] newBounds = new Bounds[origBounds.length];
						for (int k = 0; k < origBounds.length; k++) {
							newBounds[k] = new Bounds(origBounds[k].min, origBounds[k].max);
						}
						DistanceConstraint copy = new DistanceConstraint(newBounds);
						copy.setFrom(vars[i]);
						copy.setTo(vars[j]);						
						this.completeNetwork.addConstraint(copy);
					}
				}
			}	
		}
	}
	
	public DistanceConstraint getComposition(DistanceConstraint c1, DistanceConstraint c2) {
		Bounds[] b1 = c1.getBounds();
		Bounds[] b2 = c2.getBounds();
		ArrayList<Bounds> compBounds = new ArrayList<Bounds>();
		for (int i = 0; i < b1.length; i++) {
			for (int j = 0; j < b2.length; j++) {
				Bounds oneSum = new Bounds(b1[i].min+b2[j].min, b1[i].max+b2[j].max);
				compBounds.add(oneSum);
			}
		}
		
		//Filter out bounds that are contained in other bounds
		ArrayList<Bounds> toRemove = new ArrayList<Bounds>();
		for (int i = 0; i < compBounds.size(); i++) {
			for (int j = 0; j < compBounds.size(); j++) {
				if (i != j) {
					if (compBounds.get(i).min >= compBounds.get(j).min && compBounds.get(i).max <= compBounds.get(j).max) {
						toRemove.add(compBounds.get(i));
					}
				}
			}	
		}
		for (Bounds b : toRemove) compBounds.remove(b);
		
		DistanceConstraint ret = new DistanceConstraint(compBounds.toArray(new Bounds[compBounds.size()]));
		ret.setFrom(c1.getFrom());
		ret.setTo(c2.getTo());
		
		return ret;
	}
	
	public DistanceConstraint getIntersection(DistanceConstraint c1, DistanceConstraint c2) {
		
		if (!c1.getFrom().equals(c2.getFrom()) || !c1.getTo().equals(c2.getTo())) return null;
		
		Bounds[] b1 = c1.getBounds();
		Bounds[] b2 = c2.getBounds();
		ArrayList<Bounds> intBounds = new ArrayList<Bounds>();
		for (int i = 0; i < b1.length; i++) {
			for (int j = 0; j < b2.length; j++) {
				if (b1[i].isIntersecting(b2[j])) {
					Bounds oneInt = b1[i].intersect(b2[j]);
					intBounds.add(oneInt);					
				}
			}
		}
		
		if (intBounds.isEmpty()) return null;
		
		DistanceConstraint ret = new DistanceConstraint(intBounds.toArray(new Bounds[intBounds.size()]));
		ret.setFrom(c1.getFrom());
		ret.setTo(c1.getTo());
		
		return ret;
	}
	
	@Override
	public boolean propagate() {
		// APSPSolver will also propagate what it can...
		// but first, let's reduce these intervals!
		this.createCompleteNetwork();
		boolean fixedpoint = false;
		Variable[] vars = this.completeNetwork.getVariables();
		while (!fixedpoint) {
			fixedpoint = true;
			for (int k = 0; k < vars.length; k++) {
				for (int i = 0; i < vars.length; i++) {
					if (i != k) {
						for (int j = 0; j < vars.length; j++) {
							if (j != k && j != i) {
								DistanceConstraint r_ij = (DistanceConstraint)completeNetwork.getConstraint(vars[i], vars[j]);
								DistanceConstraint r_ik = (DistanceConstraint)completeNetwork.getConstraint(vars[i], vars[k]);
								DistanceConstraint r_kj = (DistanceConstraint)completeNetwork.getConstraint(vars[k], vars[j]);
								//comp = R_ik * R_kj
								DistanceConstraint comp = getComposition(r_ik, r_kj);
								//inters = R_ij ^ comp
								DistanceConstraint inters = getIntersection(r_ij, comp);
								//if inters = 0 return false
								if (inters == null) return false;
								//if inters != R_ij
								Bounds[] bOrig = r_ij.getBounds();
								Bounds[] bNew = inters.getBounds();
								for (int i1 = 0; i1 < bOrig.length; i1++) {
									boolean found_i1 = false;
									for (int i2 = 0; i2 < bNew.length; i2++) {
										if (bOrig[i1].equals(bNew[i2])) {
											found_i1 = true;
											break;
										}
									}
									if (!found_i1) {
										System.out.println("Replaced " + r_ij + " with " + inters);
										this.completeNetwork.removeConstraint(r_ij);
										this.completeNetwork.addConstraint(inters);
										fixedpoint = false;
										break;
									}
								}
							}
						}	
					}
				}				
			}
		}
		
		return true;
	}

	public MultiTimePoint getSource() {
		return source;
	}

	public MultiTimePoint getSink() {
		return sink;
	}

	
}

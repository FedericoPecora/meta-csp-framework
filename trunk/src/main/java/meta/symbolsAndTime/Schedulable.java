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
package meta.symbolsAndTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import multi.activity.Activity;
import multi.activity.ActivityComparator;
import multi.activity.ActivityNetwork;
import multi.allenInterval.AllenIntervalConstraint;
import time.APSPSolver;
import time.Bounds;
import utility.logging.MetaCSPLogging;
import framework.ConstraintNetwork;
import framework.ValueOrderingH;
import framework.Variable;
import framework.VariableOrderingH;
import framework.meta.MetaConstraint;
import framework.meta.MetaVariable;

public abstract class Schedulable extends MetaConstraint {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5719994497319584156L;
	private transient Logger logger = MetaCSPLogging.getLogger(this.getClass());
	
	public int getBeforeParameter() {
		return beforeParameter;
	}

	public void setBeforeParameter(int beforeParameter) {
		this.beforeParameter = beforeParameter;
	}

	int beforeParameter;

	public PEAKCOLLECTION getPeakCollectionStrategy() {
		return peakCollectionStrategy;
	}

	public void setPeakCollectionStrategy(PEAKCOLLECTION peakCollectionStrategy) {
		this.peakCollectionStrategy = peakCollectionStrategy;
	}

	protected Vector<Activity> activities;
	
	public static enum PEAKCOLLECTION {SAMPLING, COMPLETE, BINARY};
	
	protected PEAKCOLLECTION peakCollectionStrategy = PEAKCOLLECTION.SAMPLING;

	public static <T> Set<Set<T>> powerSet(Set<T> originalSet) {
	    Set<Set<T>> sets = new HashSet<Set<T>>();
	    if (originalSet.isEmpty()) {
	        sets.add(new HashSet<T>());
	        return sets;
	    }
	    List<T> list = new ArrayList<T>(originalSet);
	    T head = list.get(0);
	    Set<T> rest = new HashSet<T>(list.subList(1, list.size())); 
	    for (Set<T> set : powerSet(rest)) {
	    	Set<T> newSet = new HashSet<T>();
	        newSet.add(head);
	        newSet.addAll(set);
	        sets.add(newSet);
	        sets.add(set);
	    }           
	    return sets;
	}
	
	public Schedulable(VariableOrderingH varOH, ValueOrderingH valOH) {
		super(varOH, valOH);
		this.beforeParameter=1;
	}
	
	
	// Finds sets of overlapping activities and assesses whether they are conflicting (e.g., over-consuming a resource)
	private ConstraintNetwork[] samplingPeakCollection() {

		if (activities != null && !activities.isEmpty()) {
			
			Activity[] groundVars = activities.toArray(new Activity[activities.size()]);
			
			Arrays.sort(groundVars,new ActivityComparator(true));
			
			Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
			
			HashMap<Activity,ActivityNetwork> usages = new HashMap<Activity,ActivityNetwork>();
			
			Vector<Vector<Activity>> overlappingAll = new Vector<Vector<Activity>>();
			
			
			for (Activity act : activities) {
				if (isConflicting(new Activity[] {act})) {
					ActivityNetwork temp = new ActivityNetwork(null);
					temp.addVariable(act);
					ret.add(temp);
				}
			}
	
			//	groundVars are ordered activities
			for (int i = 0; i < groundVars.length; i++) {
				Vector<Activity> overlapping = new Vector<Activity>();
				overlapping.add(groundVars[i]);
				long start = (groundVars[i]).getTemporalVariable().getEST();
				long end = (groundVars[i]).getTemporalVariable().getEET();
				Bounds intersection = new Bounds(start, end);
				// starting from act[i] all the forthcoming activities are evaluated to see if they temporally
				// overlaps with act[i]
				for (int j = 0; j < groundVars.length; j++) {
					if (i != j) {
						start = (groundVars[j]).getTemporalVariable().getEST();
						end = (groundVars[j]).getTemporalVariable().getEET();
						Bounds nextInterval = new Bounds(start, end);
						Bounds intersectionNew = intersection.intersectStrict(nextInterval);
						// if act[j] overlaps it is added to the temporary (wrt i) set of activities
						if (intersectionNew != null) {
							overlapping.add(groundVars[j]);
							// the current set of overlapping activities is evaluated to see if
							// the resource capacity is exceeded
							if (isConflicting(overlapping.toArray(new Activity[overlapping.size()]))) {
								// if it is exceeded the Vector of activities gathered in this iteration is put
								// in a Vector<Vector<Activity>>
								overlappingAll.add(overlapping);
								break;						
							}
							// if they don't exceed the capacity, just the newIntersection is taken into account...
							else intersection = intersectionNew;
						}
					}
				}
			}
	
			for (Vector<Activity> overlapping : overlappingAll) {
				if (overlapping.size() > 1) {
					Activity first = overlapping.get(0);
					ActivityNetwork temp = new ActivityNetwork(null);
					for (Activity act : overlapping) temp.addVariable(act);
					usages.put(first, temp);
				}
			}
			
			for (Activity key : usages.keySet()) {
				if (usages.get(key).getVariables().length > 1) ret.add(usages.get(key));
			}
			
			return ret.toArray(new ConstraintNetwork[ret.size()]);
		}
		return (new ConstraintNetwork[0]);		
	}

	
	private ConstraintNetwork[] completePeakCollection() {
		if (activities != null && !activities.isEmpty()) {
			logger.finest("Doing complete peak collection with " + activities.size() + " activities...");
			Activity[] groundVars = activities.toArray(new Activity[activities.size()]);
			Vector<Long> discontinuities = new Vector<Long>();
			for (Activity a : groundVars) {
				long start = a.getTemporalVariable().getEST();
				long end = a.getTemporalVariable().getEST();
				if (!discontinuities.contains(start)) discontinuities.add(start);
				if (!discontinuities.contains(end)) discontinuities.add(end);
			}
			
			Long[] discontinuitiesArray = discontinuities.toArray(new Long[discontinuities.size()]);
			Arrays.sort(discontinuitiesArray);
			
			HashSet<HashSet<Activity>> superPeaks = new HashSet<HashSet<Activity>>();

			for (int i = 0; i < discontinuitiesArray.length-1; i++) {
				HashSet<Activity> onePeak = new HashSet<Activity>();
				superPeaks.add(onePeak);
				Bounds interval = new Bounds(discontinuitiesArray[i], discontinuitiesArray[i+1]);
				for (Activity a : groundVars) {
					Bounds interval1 = new Bounds(a.getTemporalVariable().getEST(), a.getTemporalVariable().getEET());
					Bounds intersection = interval.intersectStrict(interval1);
					if (intersection != null && !intersection.isSingleton()) {
						onePeak.add(a);
					}
				}
			}

			Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
			for (HashSet<Activity> superSet : superPeaks) {
				for (Set<Activity> s : powerSet(superSet)) {
					if (!s.isEmpty()) {
						ActivityNetwork cn = new ActivityNetwork(null);
						for (Activity a : s) cn.addVariable(a); 
						if (!ret.contains(cn) && isConflicting(s.toArray(new Activity[s.size()]))) ret.add(cn);
					}
				}
			}
			logger.finest("Done peak sampling");
			return ret.toArray(new ConstraintNetwork[ret.size()]);			
		} 
		
		return (new ConstraintNetwork[0]);
	}

	
	private ConstraintNetwork[] binaryPeakCollection() {
		if (activities != null && !activities.isEmpty()) {
			Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
			logger.finest("Doing binary peak collection with " + activities.size() + " activities...");
			Activity[] groundVars = activities.toArray(new Activity[activities.size()]);
			for (Activity a : groundVars) {
				if (isConflicting(new Activity[] {a})) {
					ActivityNetwork cn = new ActivityNetwork(null);
					cn.addVariable(a);
					ret.add(cn);
				}
			}
			if (!ret.isEmpty()) {
				return ret.toArray(new ConstraintNetwork[ret.size()]);
			}
			for (int i = 0; i < groundVars.length-1; i++) {
				for (int j = i+1; j < groundVars.length; j++) {
					Bounds bi = new Bounds(groundVars[i].getTemporalVariable().getEST(), groundVars[i].getTemporalVariable().getEET());
					Bounds bj = new Bounds(groundVars[j].getTemporalVariable().getEST(), groundVars[j].getTemporalVariable().getEET());
					if (bi.intersectStrict(bj) != null && isConflicting(new Activity[] {groundVars[i], groundVars[j]})) {
						ActivityNetwork cn = new ActivityNetwork(null);
						cn.addVariable(groundVars[i]);
						cn.addVariable(groundVars[j]);
						ret.add(cn);
					}
				}
			}
			if (!ret.isEmpty()) {
				return ret.toArray(new ConstraintNetwork[ret.size()]);			
			}
		}
		return (new ConstraintNetwork[0]);
	}
	
//	private ConstraintNetwork[] binaryPeakCollection() {
//		ConstraintNetwork[] nonMinimalPeaks = this.completePeakCollection();
//		Vector<ConstraintNetwork> ret = null;
//		for (ConstraintNetwork cn : nonMinimalPeaks) {
//			if (cn.getVariables().length == 2) {
//				if (ret == null) ret = new Vector<ConstraintNetwork>();
////				Variable[] vaux= cn.getVariables();
////				if(!vaux[0].equals(vaux[1]))
//					ret.add(cn);
//			}
//		}
//		if (ret != null) return ret.toArray(new ConstraintNetwork[ret.size()]);
//		return (new ConstraintNetwork[0]);
//	}
	
	
	@Override
	public ConstraintNetwork[] getMetaVariables() {
		if (peakCollectionStrategy.equals(PEAKCOLLECTION.SAMPLING))
			return samplingPeakCollection();
		else if (peakCollectionStrategy.equals(PEAKCOLLECTION.BINARY))
			return binaryPeakCollection();
		return completePeakCollection();
	}

	@Override
	public ConstraintNetwork[] getMetaValues(MetaVariable metaVariable, int initialTime) {
		return getMetaValues(metaVariable);
	}
	
	
	@Override
	public ConstraintNetwork[] getMetaValues(MetaVariable metaVariable) {	
		ConstraintNetwork conflict = metaVariable.getConstraintNetwork();
		MCSData[] mcsinfo = getOrderedMCSs(conflict);
		
		Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
		if(mcsinfo == null) //unresolvabe MCS: no solution can be found
		{				
			//System.out.println("ESTA Fails: unresolvable MCS.");
			return null;
		}
		
		for (MCSData mcs : mcsinfo) {
			AllenIntervalConstraint before = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, new Bounds(this.beforeParameter, APSPSolver.INF));
			before.setFrom(mcs.mcsActFrom);			
			before.setTo(mcs.mcsActTo);
			ActivityNetwork resolver = new ActivityNetwork(mcs.mcsActFrom.getConstraintSolver());
			resolver.addVariable(mcs.mcsActFrom);
			resolver.addVariable(mcs.mcsActTo);
			resolver.addConstraint(before);
			ret.add(resolver);
		}

		return ret.toArray(new ConstraintNetwork[ret.size()]);

	}
	
	
	/**
	 * Get a list of {@link MCSData} objects, ordered according to decreasing k, where k is a heuristic estimator
	 * of the amount of flexibility which is maintained when imposing a temporal constraint that resolves an MCS -
	 * see [P. Laborie, M. Ghallab, "Planning with Sharable Resource Constraints", IJCAI 1995].
	 * @param peak The peaks from which to sample MCSs and compute the k-based ordering.
	 * @return An ordered array of {@link MCSData} objects.
	 */
	public MCSData[] getOrderedMCSs(ConstraintNetwork peak)
	{
		
		//System.out.println("PEAK SIZE: " + peak.getVariables().length);
		
		Vector<Variable[]> mcslist = new Vector<Variable[]>();
		
		Variable[] vars = peak.getVariables();
		for (int i = 0; i < vars.length; i++) {
			for (int j = i+1; j < vars.length; j++) {
				Variable[] oneMcs = new Variable[2];
				oneMcs[0] = vars[i];
				oneMcs[1] = vars[j];
				mcslist.add(oneMcs);
			}
		}

		//MCSData[] mcsinfo = new MCSData[mcslist.size()];
		Vector<MCSData> mcsinfo = new Vector<MCSData>();
		
		int index = 0;
		boolean unresMCSFound = false;
		//System.out.println("MCSINFO size: " + mcsinfo.length);
		
		while((!unresMCSFound) && (index < mcslist.size())) //Per ogni MCS
		{
			float pcmin = 1.0f; //Valore del pcmin
			float pcminBad = 1.0f; //Valore del pcmin
			float kReciprocal = 0.0f; //Reciproco di K (Ribaltare prima di uscire)
			Variable actFrom = null;
			Variable actTo = null;
			int unresMCS = 0;
			
			Variable[] currentMcs = mcslist.elementAt(index);
			//Vector che conterrà i valori dei commit
			Vector<Float> pcVector = new Vector<Float>();
			
			int mcsSize = currentMcs.length;
						
			//Per ogni coppia di attività {Ag, Ah} dell'MCS
			for (int g = 0; g < mcsSize; g++)
			{
				long est1 = ((Activity)currentMcs[g]).getTemporalVariable().getEST();
				long eet1 = ((Activity)currentMcs[g]).getTemporalVariable().getEST();
				long lst1 = ((Activity)currentMcs[g]).getTemporalVariable().getLST();
				long let1 = ((Activity)currentMcs[g]).getTemporalVariable().getLET();
			
				for (int h = g+1; h < mcsSize; h++)
				{
					long est2 = ((Activity)currentMcs[h]).getTemporalVariable().getEST();
					long eet2 = ((Activity)currentMcs[h]).getTemporalVariable().getEET();
					long lst2 = ((Activity)currentMcs[h]).getTemporalVariable().getLST();
					long let2 = ((Activity)currentMcs[h]).getTemporalVariable().getLET();

					//Analisi coppia diretta
					long dmin = est2 - let1;
					long dmax = lst2 - eet1;

					if(dmin > dmax)
					{
						logger.severe("Direct pair and dmin > dmax: IMPOSSIBLE");
						System.exit(0);
					}
					
					float pc = 0.0f; //pc corrente
					if(dmin != dmax) 
					{
						pc = ((float)(Math.min(dmax, 0) - Math.min(dmin, 0)))/((float)(dmax - dmin));
						pcVector.add(pc);
												
						if(pc < pcmin)
						{
							//System.out.println("ADDED DIRECT: " + pc);
							pcmin = pc;
							pcminBad = pcmin;
							actFrom = currentMcs[g];
							actTo = currentMcs[h];
						}
						else
						{
							unresMCS++;
						}
					}
					else //If dmin == dmax, we can skip the analysis of the pair {Ag, Ah}
					{
						unresMCS++;
					}
										
					//Analisi coppia inversa
					dmin = est1 - let2;
					dmax = lst1 - eet2;

					if(dmin > dmax)
					{
						logger.severe("Inverse pair and dmin > dmax: IMPOSSIBLE");
						System.exit(0);
					}
					
					if(dmin != dmax) //Se dmin == dmax, possiamo skippare l'analisi della coppia {Ag, Ah}
					{
						pc = ((float)(Math.min(dmax, 0) - Math.min(dmin, 0)))/((float)(dmax - dmin));
						pcVector.add(pc);
						
						if(pc < pcmin)
						{
							//System.out.println("ADDED INVERSE: " + pc);
							pcmin = pc;
							actFrom = currentMcs[h];
							actTo = currentMcs[g];
						}
						else
						{
							unresMCS++;
						}
					}
					else
					{
						unresMCS++;
					}
				}					
			}
			
			//SE INCONTRIAMO UN MCS IRRISOLVIBILE POSSIAMO INTERROMPERE IL CICLO: LA SOLUZIONE E' IMPOSSIBILE
			if(unresMCS < (mcsSize*(mcsSize-1)))
			{
				//Calcolo del K(MCS)
				for(int g=0; g<pcVector.size(); g++)
				{
					//Dbg.printMsg("" + pcVector.elementAt(g), LogLvl.Normal);
					kReciprocal += 1.0f/(1.0f + pcVector.elementAt(g) - pcmin);
				}
						
				//mcsinfo[index] = new MCSData(pcmin, (Activity)actFrom, (Activity)actTo, (kReciprocal == 0.0f) ? 1 : 1.0f/kReciprocal);			
				mcsinfo.add(new MCSData(pcmin, (Activity)actFrom, (Activity)actTo, (kReciprocal == 0.0f) ? 1 : 1.0f/kReciprocal));
				mcsinfo.add(new MCSData(pcminBad, (Activity)actTo, (Activity)actFrom, (kReciprocal == 0.0f) ? 1 : 1.0f/kReciprocal));
				index++;
			}
			else
			{
				unresMCSFound = true;
			}
		}

		MCSData[] mcsinfoArray = mcsinfo.toArray(new MCSData[mcsinfo.size()]);
		//Se abbiamo trovato una soluzione
		if(!unresMCSFound)
		{
			//Arrays.sort(mcsinfo);		
			Arrays.sort(mcsinfoArray);
		}
		else
		{
			//mcsinfo = null;
			mcsinfoArray = null;
		}
		
		//return mcsinfo;
		return mcsinfoArray;
	}	

	protected boolean temporalOverlap(Activity a1, Activity a2) {
		return !(
				a1.getTemporalVariable().getEET() <= a2.getTemporalVariable().getEST() ||
				a2.getTemporalVariable().getEET() <= a1.getTemporalVariable().getEST()
				);
	}

	@Override
	public void markResolvedSub(MetaVariable con, ConstraintNetwork metaValue) {
		// TODO Auto-generated method stub
	}
	
	public abstract boolean isConflicting(Activity[] peak);
	
	public void setUsage(Activity... acts) {
		if (activities == null) activities = new Vector<Activity>();
		for (Activity act : acts) 
			if (!activities.contains(act)) 
				activities.add(act);
		//System.out.println("-->" + activities.size());
	}

	public void removeUsage(Activity... acts) {
		for (Activity act : acts) activities.removeElement(act);
		//System.out.println("-->" + activities.size());
	}

}

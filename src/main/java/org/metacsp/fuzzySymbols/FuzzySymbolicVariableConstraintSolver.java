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
package org.metacsp.fuzzySymbols;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Vector;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;
import org.metacsp.multi.fuzzyActivity.FuzzyActivity;
import org.metacsp.multi.symbols.SymbolicValueConstraint;

/**
 * This {@link ConstraintSolver} solves CSPs where variables are fuzzy sets (see
 * {@link FuzzySymbolicVariable}) and constraints are equality or inequalities
 * among fuzzy sets.  Constraints are crisp, and represented by {@link SymbolicValueConstraint}s.   
 * 
 * @author Masoumeh Mansouri
 *
 */
public class FuzzySymbolicVariableConstraintSolver extends ConstraintSolver {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1380477808694562200L;
	private Vector<FuzzyActivity> subs = new Vector<FuzzyActivity>();
	//Progressively increasing IDs for state variables
	protected int SVIDs = 0;

	private Vector<Double> allConPosib = new Vector<Double>(); 
	private HashMap<Variable, Integer[]> orderHash = new HashMap<Variable, Integer[]>();
	private double possibilityDegree = 0.0;
	private Vector<Constraint> falseConstraint = new Vector<Constraint>(); 
	//private LinkedHashMap<SymbolicDomain[], Double> sdTuples = new LinkedHashMap<SymbolicDomain[], Double>();
	//private LinkedHashMap<FuzzySymbolicDomain[], Double> sdTuples = new LinkedHashMap<FuzzySymbolicDomain[], Double>();
	public FuzzySymbolicVariableConstraintSolver() {
		super(new Class[]{SymbolicValueConstraint.class}, FuzzySymbolicVariable.class);
		//this.setOptions(OPTIONS.MANUAL_PROPAGATE);
		this.setOptions(OPTIONS.AUTO_PROPAGATE);
		this.setOptions(OPTIONS.DOMAINS_MANUALLY_INSTANTIATED);
	}
	
	@Override
	protected boolean addConstraintsSub(Constraint[] c) {
		//this.propagateFuzzyValues(c);
		return true;
	}

	@Override
	protected Variable[] createVariablesSub(int num) {
		FuzzySymbolicVariable [] ret = new FuzzySymbolicVariable [num];
		for (int i = 0; i < num; i++) ret[i] = new FuzzySymbolicVariable (this, SVIDs++);
		return ret;
	}
	
	public double getUpperBound() 
	{
		if (allConPosib.size() == 0) {
			return 1.0;
		}
			
		else{	
			double tmp = Collections.min(allConPosib);
			return  tmp;
		}
			 
	}
	
	public double getPosibilityDegree()
	{
		return  possibilityDegree; 
	}
	
	/*
	public LinkedHashMap<FuzzySymbolicDomain[], Double> getLabelings() {
		return this.sdTuples;
	}
	*/
	
	
	private boolean checkEquality(HashMap<String, Double> a,
			HashMap<String, Double> b) {
		for(String s: a.keySet()){
			if(Double.compare(a.get(s), b.get(s)) != 0)
				return false;
		}
		return true;
	}
	
	
	
	
	private boolean CheckTermination(HashMap<Integer, HashMap<String, Double>> var){
		boolean isEqual = true;		
		for(int i =  0; i < this.getVariables().length; i++)
		{
			if(!checkEquality(((FuzzySymbolicVariable)this.getVariables()[i]).getSymbolsAndPossibilities(), var.get(i))){
				HashMap<String, Double> m = new HashMap<String, Double>();
				m.putAll(((FuzzySymbolicVariable)this.getVariables()[i]).getSymbolsAndPossibilities());
				var.put(i, m);
				isEqual = false;
			}			
		}		
		return isEqual;
	}
	
	/*
	private boolean CheckTermination(HashMap<Integer, FuzzySymbolicDomain> var){
		boolean isEqual = true;		
		for(int i =  0; i < this.getVariables().length; i++)
		{
			if(! ((FuzzySymbolicVariable)this.getVariables()[i]).getDomain().equals(var.get(i)) ){
				FuzzySymbolicDomain m = new FuzzySymbolicDomain((FuzzySymbolicVariable)this.getVariables()[i], ((FuzzySymbolicDomain)((FuzzySymbolicVariable)this.getVariables()[i]).getDomain()).getSymbols(), ((FuzzySymbolicDomain)((FuzzySymbolicVariable)this.getVariables()[i]).getDomain()).getPossibilityDegrees());
				var.put(i, m);
				isEqual = false;
			}			
		}		
		return isEqual;
	}
	*/
	
	//fuzzy arc consistency
	/*
	private void acProppagation(Constraint[] svcArray) {
		HashMap<Integer, FuzzySymbolicDomain> var = new HashMap<Integer, FuzzySymbolicDomain>();
		for(int i = 0; i < this.getVariables().length; i++){
			FuzzySymbolicDomain m = new FuzzySymbolicDomain((FuzzySymbolicVariable)this.getVariables()[i], ((FuzzySymbolicDomain)((FuzzySymbolicVariable)this.getVariables()[i]).getDomain()).getSymbols(), ((FuzzySymbolicDomain)((FuzzySymbolicVariable)this.getVariables()[i]).getDomain()).getPossibilityDegrees());
			var.put(i, m);
		}
		while(true)
		{
			for(int i = 0; i < svcArray.length; i++)
			{
				if (!(svcArray[i] instanceof SymbolicValueConstraint));
				else {
					SymbolicValueConstraint svc = (SymbolicValueConstraint)svcArray[i];
					if(svc.getType().equals(SymbolicValueConstraint.Type.DIFFERENT))
					{
						allConPosib.add(getSup(svcArray[i], 0, 1));
						allConPosib.add(getSup(svcArray[i], 1, 0));
					}
					if(svc.getType().equals(SymbolicValueConstraint.Type.EQUALS))
						allConPosib.add(getInf(svcArray[i]));
				}	
			}
			if(CheckTermination(var))
				break;
			allConPosib.clear();
		}
	}
	*/
	
	private void resetDomains() {
		for (Variable var : this.getVariables()) {
			FuzzySymbolicVariable fvar = (FuzzySymbolicVariable)var;
			fvar.resetDomain();
		}
	}
	
	private void acPropagation(Constraint[] svcArray) {
		HashMap<Integer, HashMap<String, Double>> var = new HashMap<Integer, HashMap<String, Double>>();
//		System.out.println(this.getVariables().length);
		for(int i = 0; i < this.getVariables().length; i++){
			HashMap<String, Double> m = new HashMap<String, Double>();
//			System.out.println(((FuzzySymbolicVariable)this.getVariables()[i]));
			m.putAll(((FuzzySymbolicVariable)this.getVariables()[i]).getSymbolsAndPossibilities());
			var.put(i, m);
		}
		
	
		double infTmp = 0;
		boolean notAdded = false;
//		Constraint ctemp = null;
		allConPosib.clear();
		while(true)
		{
			for(int i = 0; i < svcArray.length; i++)
			{				
				if (!(svcArray[i] instanceof SymbolicValueConstraint));
				else {
					SymbolicValueConstraint svc = (SymbolicValueConstraint)svcArray[i];
					//if(subs != null){
						//if we we are interested in propagating a subgraph of this graph
						//if(subs.size() > 0){
							//if(!isInTheSubGraph(svc.getScope()[0])) continue;
							//if(!isInTheSubGraph(svc.getScope()[1])) continue;
						//}
					
						if(svc.getType().equals(SymbolicValueConstraint.Type.DIFFERENT)){
							if (svcArray.length == 1) {
								allConPosib.add(0.0);
							}
							else {
								allConPosib.add(getSup(svcArray[i], 0, 1));
								allConPosib.add(getSup(svcArray[i], 1, 0));
							}
						}
						if(svc.getType().equals(SymbolicValueConstraint.Type.EQUALS)){
//							allConPosib.add(getInf(svcArray[i]));
//							if(hasZeroPossibilities(((FuzzySymbolicDomain)((FuzzySymbolicVariable)svcArray[i].getScope()[0]).getDomain()).getPossibilityDegrees()) && 
//							hasZeroPossibilities(((FuzzySymbolicDomain)((FuzzySymbolicVariable)svcArray[i].getScope()[1]).getDomain()).getPossibilityDegrees()))
//								notAdded = true;
							infTmp = getInf(svcArray[i]);
							allConPosib.add(infTmp);
							if(Double.compare(infTmp, 0.0) == 0){
								if(!isAlreadyMarkedAsFalse(svcArray[i]) && !notAdded){
									falseConstraint.add(svcArray[i]);
									//System.out.println("falseConstraint: " + falseConstraint);
	
								}	
							}
							notAdded = false;
						}
					}
					//}	
			}
			if(CheckTermination(var))
				break;
		}
		subs.clear();		
	}
	
	private boolean isAlreadyMarkedAsFalse(Constraint c) {
		for (int i = 0; i < falseConstraint.size(); i++) {
			if(isAFalseClause(falseConstraint.get(i), c))
				return true;
		}
		return false;
	}

	private boolean isAFalseClause(Constraint c1, Constraint c2) {
		if((c1.getScope()[0].getID() == c2.getScope()[0].getID()) && (c1.getScope()[1].getID() == c2.getScope()[1].getID()))
			return true;
		if((c1.getScope()[0].getID() == c2.getScope()[1].getID()) && (c1.getScope()[0].getID() == c2.getScope()[1].getID()))
			return true;
		return false;
	}
	
	/*private boolean isInTheSubGraph(Variable varTmp) {	
		for (int i = 0; i < subs.size(); i++) {
			if(subs.get(i).getInternalVariables()[1].getDomain() == varTmp.getDomain() &&		
					isEqual(((FuzzySymbolicDomain)subs.get(i).getInternalVariables()[1].getDomain()).getPossibilityDegrees(),
							((FuzzySymbolicDomain)varTmp.getDomain()).getPossibilityDegrees()))
			return true;		
		}
		return false;
	}*/

	/*private boolean isEqual(double[] t1, double[] t2){
		
		for (int i = 0; i < t2.length; i++) {
			if(t1[i] != t2[i]) return false;
		}
		return true;
	}*/	
	
//	private boolean hasZeroPossibilities(double[] t1){
//		for (int i = 0; i < t1.length; i++) {
//			if(Double.compare(t1[i], 0.0) != 0) return false;
//		}
//		return true;
//	}
	
	
	private boolean isFound(String[] s, String st)
	{
		String[] t = s;
		java.util.Arrays.sort(t);
		if (Arrays.binarySearch(t, st) >= 0) return true;
		return false;
	}
	
	
	private double getInf(Constraint c){
//		SymbolicValueConstraint svc = (SymbolicValueConstraint)c;
		Vector<Double> psd = new Vector<Double>();
		Vector<String> intersection = new Vector<String>();
		
		
		for( String st: ((FuzzySymbolicDomain)((FuzzySymbolicVariable)c.getScope()[0]).getDomain()).getSymbols() ){
			if (isFound(((FuzzySymbolicDomain)((FuzzySymbolicVariable)c.getScope()[1]).getDomain()).getSymbols(), st))
			{
				double minTmp = Math.min(((FuzzySymbolicVariable)c.getScope()[0]).getSymbolsAndPossibilities().get(st), ((FuzzySymbolicVariable)c.getScope()[1]).getSymbolsAndPossibilities().get(st));
				((FuzzySymbolicVariable)c.getScope()[0]).getSymbolsAndPossibilities().put(st, minTmp);//To update the value
				((FuzzySymbolicVariable)c.getScope()[1]).getSymbolsAndPossibilities().put(st, minTmp);//To update the value
				psd.add(minTmp);
				intersection.add(new String(st));	
			}
		}
		//to update value preferences which are not consistent!
		for( String st: ((FuzzySymbolicDomain)((FuzzySymbolicVariable)c.getScope()[0]).getDomain()).getSymbols() )
			if (!isFound(intersection.toArray(new String[intersection.size()]), st))
				((FuzzySymbolicVariable)c.getScope()[0]).getSymbolsAndPossibilities().put(st, 0.0);//To update the value
		for( String st: ((FuzzySymbolicDomain)((FuzzySymbolicVariable)c.getScope()[1]).getDomain()).getSymbols() )
			if (!isFound(intersection.toArray(new String[intersection.size()]), st))
				((FuzzySymbolicVariable)c.getScope()[1]).getSymbolsAndPossibilities().put(st, 0.0);//To update the value
		
		return Collections.max(psd);
	}
	

	
	private double getSup(Constraint c , int from, int to){
		Vector<Double> values = new Vector<Double>(); 
		Vector<Double> tmp = new Vector<Double>();
		double[] max = new double[((FuzzySymbolicDomain)((FuzzySymbolicVariable)c.getScope()[from]).getDomain()).getSymbols().length];
		for(int i = 0; i < ((FuzzySymbolicDomain)((FuzzySymbolicVariable)c.getScope()[from]).getDomain()).getSymbols().length; i++ ){
			for(int j = 0; j < ((FuzzySymbolicDomain)((FuzzySymbolicVariable)c.getScope()[to]).getDomain()).getSymbols().length; j++ ){
				if( ((FuzzySymbolicDomain)((FuzzySymbolicVariable)c.getScope()[from]).getDomain()).getSymbols()[i] !=  ((FuzzySymbolicDomain)((FuzzySymbolicVariable)c.getScope()[to]).getDomain()).getSymbols()[j]){
					double t = ((FuzzySymbolicVariable)c.getScope()[to]).getSymbolsAndPossibilities().get( ((FuzzySymbolicDomain)((FuzzySymbolicVariable)c.getScope()[to]).getDomain()).getSymbols()[j] );
					tmp.add(t);
				}
			}
			max[i] = Collections.max(tmp);
			tmp.clear();
		}
		int i = 0;
		for( String st: ((FuzzySymbolicDomain)((FuzzySymbolicVariable)c.getScope()[from]).getDomain()).getSymbols() ){
			double t = Math.min(((FuzzySymbolicVariable)c.getScope()[from]).getSymbolsAndPossibilities().get(st), max[i]);
			values.add(t);
			((FuzzySymbolicVariable)c.getScope()[from]).getSymbolsAndPossibilities().put(st, t);//update the value
			i++;
		} 
		return Collections.max(values);
	}
	
		
	private void propagateFuzzyValues(Constraint[] svcArray) {
//		int[] orderVar = new int[this.getVariables().length];//counter for generating different state of value for different  variable
		acPropagation(svcArray);
		valueOrdering();//greedy algorithm
		

		
			

			/*
			for (int i = 0; i < svcArray.length; i++) {
			 if(svcArray[i].getScope()[1].getID() == 6){
				for (int j = 0; j < ((FuzzySymbolicDomain)(svcArray[i].getScope()[0].getDomain())).getPossibilityDegrees().length; j++) {
					System.out.println(" " + ((FuzzySymbolicDomain)(svcArray[i].getScope()[0].getDomain())).getPossibilityDegrees()[j] + " ");
				}
			}
			if(svcArray[i].getScope()[0].getID() == 6){
				for (int j = 0; j < ((FuzzySymbolicDomain)(svcArray[i].getScope()[1].getDomain())).getPossibilityDegrees().length; j++) {
					System.out.println(" " + ((FuzzySymbolicDomain)(svcArray[i].getScope()[1].getDomain())).getPossibilityDegrees()[j] + " ");
				}
			}
			}
*/								
				
		
		
		/*
		while(true)//backtracking
		{
			Vector<Double> valuedegrees = new Vector<Double>(); 			
			for(int i = 0; i < svcArray.length; i++)
			{
				SymbolicValueConstraint svc = (SymbolicValueConstraint)svcArray[i];
				String fs = ((FuzzySymbolicDomain)(svcArray[i].getScope()[0].getDomain())).getSymbols()[orderHash.get(svcArray[i].getScope()[0])[orderVar[getOrderInVar(svcArray[i].getScope()[0])]]];				
				String ts = ((FuzzySymbolicDomain)(svcArray[i].getScope()[1].getDomain())).getSymbols()[orderHash.get(svcArray[i].getScope()[1])[orderVar[getOrderInVar(svcArray[i].getScope()[1])]]];				
				double from = ((FuzzySymbolicVariable)svcArray[i].getScope()[0]).getSymbolsAndPossibilities().get(fs);
				double to =  ((FuzzySymbolicVariable)svcArray[i].getScope()[1]).getSymbolsAndPossibilities().get(ts);
				if(svc.getType().equals(SymbolicValueConstraint.Type.DIFFERENT))
				{
					if(fs != ts)
						valuedegrees.add(Math.min(from, to));
					else valuedegrees.add(0.0);
				}
				if(svc.getType().equals(SymbolicValueConstraint.Type.EQUALS))
				{
					if(fs == ts)
						valuedegrees.add(Math.min(from, to));
					else valuedegrees.add(0.0);
				}
			
			}
			//make feasible tuple
			//SymbolicDomain[] sd = new SymbolicDomain[this.getVariables().length];
			====
			FuzzySymbolicDomain[] sd = new FuzzySymbolicDomain[this.getVariables().length];
			if(Double.compare(Collections.min(valuedegrees), 0.0) != 0){
				for(int i = 0; i < orderVar.length; i++)
					//sd[i] = new SymbolicDomain((FuzzySymbolicVariable)this.getVariables()[i], new String[]{((SymbolicDomain)this.getVariables()[i].getDomain()).getSymbols()[orderHash.get(this.getVariables()[i])[orderVar[i]]]});
					sd[i] = new FuzzySymbolicDomain((FuzzySymbolicVariable)this.getVariables()[i], new String[]{((FuzzySymbolicDomain)this.getVariables()[i].getDomain()).getSymbols()[orderHash.get(this.getVariables()[i])[orderVar[i]]]});
				sdTuples.put(sd, Collections.min(valuedegrees));
			}
			====	
			if(Double.compare(Collections.min(valuedegrees), this.getUpperBound()) == 0){//check if is equal to max number which we get from arc consistency
				possibilityDegree = this.getUpperBound();
				//break;
			}
			valuedegrees.clear();
			if(IsTerminated(this.getVariables(), orderVar)) //check for reaching all the possible permutation
				break;
			this.GetNextTuple(orderVar);
			
		}	*/
	}

	/*
	private void valueOrdering() {
		int j = 0;
		for(int i = 0; i <  this.getVariables().length; i++){			
			HashMap<String, Double> tempMapUnsorted = new HashMap<String, Double>();
			String[] symbols = ((FuzzySymbolicDomain)((FuzzySymbolicVariable)this.getVariables()[i]).getDomain()).getSymbols();
			double[] possibilities = ((FuzzySymbolicDomain)((FuzzySymbolicVariable)this.getVariables()[i]).getDomain()).getPossibilityDegrees();
			for (int k = 0; k < symbols.length; k++) tempMapUnsorted.put(symbols[k], possibilities[k]);
			//tempMap = SortHashmap(((FuzzySymbolicVariable)this.getVariables()[i]).getSymbolsAndPossibilities());
			HashMap<String, Double> tempMap = SortHashmap(tempMapUnsorted);
			j = 0;
			Integer[] dtod = new Integer[((FuzzySymbolicDomain)((FuzzySymbolicVariable)this.getVariables()[i]).getDomain()).getSymbols().length];
			for(String s: tempMap.keySet()){
				if(Double.compare(tempMap.get(s), 0.0) == 0)
					continue;
				for(int k = 0; k < ((FuzzySymbolicDomain)((FuzzySymbolicVariable)this.getVariables()[i]).getDomain()).getSymbols().length; k++){
					if(((FuzzySymbolicDomain)((FuzzySymbolicVariable)this.getVariables()[i]).getDomain()).getSymbols()[k] == s){
						dtod[j] = k;
						break;
					}
				}
				if(j < ((FuzzySymbolicDomain)((FuzzySymbolicVariable)this.getVariables()[i]).getDomain()).getSymbols().length - 1)
					j++;
			}
			
			orderHash.put(this.getVariables()[i], dtod);			
		}
	}
	*/

	private void valueOrdering() {
		int j = 0;
		for(int i = 0; i <  this.getVariables().length; i++){			
			HashMap<String, Double> tempMap = new HashMap<String, Double>();
			tempMap = sortHashmap(((FuzzySymbolicVariable)this.getVariables()[i]).getSymbolsAndPossibilities());
			j = 0;
			Integer[] dtod = new Integer[((FuzzySymbolicDomain)((FuzzySymbolicVariable)this.getVariables()[i]).getDomain()).getSymbols().length];
			for(String s: tempMap.keySet()){
				if(Double.compare(tempMap.get(s), 0.0) == 0)
					continue;
				for(int k = 0; k < ((FuzzySymbolicDomain)((FuzzySymbolicVariable)this.getVariables()[i]).getDomain()).getSymbols().length; k++){
					if(((FuzzySymbolicDomain)((FuzzySymbolicVariable)this.getVariables()[i]).getDomain()).getSymbols()[k] == s){
						dtod[j] = k;
						break;
					}
				}
				if(j < ((FuzzySymbolicDomain)((FuzzySymbolicVariable)this.getVariables()[i]).getDomain()).getSymbols().length - 1)
					j++;
			}
			
			orderHash.put(this.getVariables()[i], dtod);			
		}
			
	}
	
	
	private LinkedHashMap<String, Double> sortHashmap(HashMap<String, Double> psHashMap) {
		HashMap<String, Double> passedMap = new HashMap<String, Double>();
//		passedMap = (HashMap<String, Double>) psHashMap.clone();
		Comparator<String> comparator = Collections.reverseOrder();
		Comparator<Double> comparator1 = Collections.reverseOrder();
		ArrayList<String> mapKeys = new ArrayList<String>(passedMap.keySet());
		ArrayList<Double> mapValues =  new ArrayList<Double>(passedMap.values());
	    Collections.sort(mapValues, comparator1);
	    Collections.sort(mapKeys, comparator);
	        
	    LinkedHashMap<String, Double>  sortedMap = new LinkedHashMap<String, Double>();
	    
	    Iterator<Double> valueIt = mapValues.iterator();
	    while (valueIt.hasNext()) {
	        Object val = valueIt.next();
	        Iterator<String> keyIt = mapKeys.iterator();
	        
	        while (keyIt.hasNext()) {
	            Object key = keyIt.next();
	            String comp1 = passedMap.get(key).toString();
	            String comp2 = val.toString();
	            
	            if (comp1.equals(comp2)){
	                passedMap.remove(key);
	                (mapKeys).remove(key);
	                sortedMap.put((String)key, (Double)val);
	                break;
	            }

	        }

	    }
	    return sortedMap;
	}
		
//	private void GetNextTuple(int[] orderVar)
//	{
//		int index = orderVar.length - 1;
//		if(orderVar[index] == (((FuzzySymbolicDomain)this.getVariables()[index].getDomain()).getSymbols().length - 1))
//			while(true)
//			{
//				if(orderVar[index] == (((FuzzySymbolicDomain)getVariables()[index].getDomain()).getSymbols().length - 1))
//				{
//					orderVar[index] = 0;
//					if(index != 0)
//						index--;
//					else 
//						break;
//				}
//				else
//				{
//					orderVar[index]++;
//					break;
//				}
//			}
//		else
//			orderVar[index]++;
//	}

//	private boolean IsTerminated(Variable[] vars, int[] orderVar)
//	{
//		int sum1 = 0, sum2 = 0;
//		for(int i = 0; i < orderVar.length; i++)
//		{
//			sum1 += ((FuzzySymbolicDomain)vars[i].getDomain()).getSymbols().length - 1;
//			sum2 += orderVar[i]; 
//		}
//
//		if(sum1 == sum2)
//			return true;
//		return false;
//	}

//	private int getOrderInVar(Variable v)
//	{
//		for(int i = 0; i < this.getVariables().length; i++)
//			if(v.equals(this.getVariables()[i]))
//				return i;
//		return -1;
//	}

	@Override
	public boolean propagate() {
		resetDomains();
		Constraint[] cons = this.getConstraints();
		this.propagateFuzzyValues(cons);
		return true;
	}

	@Override
	protected void removeConstraintsSub(Constraint[] c) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void removeVariablesSub(Variable[] v) {
		// TODO Auto-generated method stub
		
	}
	
	public void setVarOfSubGraph(Vector<FuzzyActivity> subs)
	{
		this.subs = subs;
	}
	
	public Vector<Constraint> getFalseConstraint(){
		return falseConstraint;
	}

	public void resetFalseClauses() {
		falseConstraint.clear();
	}

	@Override
	public void registerValueChoiceFunctions() {
		// TODO Auto-generated method stub
	}
	

}

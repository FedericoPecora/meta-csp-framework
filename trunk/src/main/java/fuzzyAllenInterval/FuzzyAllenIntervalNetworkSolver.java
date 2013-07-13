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
package fuzzyAllenInterval;

import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import time.qualitative.SimpleAllenInterval;
import time.qualitative.SimpleInterval;

import multi.fuzzyActivity.FuzzyActivity;
import framework.Constraint;
import framework.ConstraintNetwork;
import framework.ConstraintSolver;
import framework.Domain;
import framework.ValueChoiceFunction;
import framework.Variable;


/**
 * This {@link ConstraintSolver} implements a fuzzified version of Allen's path consistency 
 * algorithm, as well as search procedure to extract the optimal solution of a fuzzy temporal
 * CSP.
 * 
 * @author Federico Pecora
 *
 */
public class FuzzyAllenIntervalNetworkSolver extends ConstraintSolver {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1499386815980553133L;
	private int IDs = 0;
	private double globalPossibilityDegree = 0.0;
	private Vector<FuzzyActivity> subs = new Vector<FuzzyActivity>();
	private int numVars = 0;
	private Vector<Variable> subVariables = new Vector<Variable>();
	private boolean isSubGraph = false;
	private HashMap<Integer, Integer> varIndex = new HashMap<Integer, Integer>();
	private Constraint[] crispCons = null;
	
	/**
	 * Creates a new {@link FuzzyAllenIntervalNetworkSolver}.
	 */
	public FuzzyAllenIntervalNetworkSolver() {
		super(new Class[]{FuzzyAllenIntervalConstraint.class}, new Class[]{SimpleAllenInterval.class});
		this.setOptions(OPTIONS.AUTO_PROPAGATE);
	}

	@Override
	protected boolean addConstraintsSub(Constraint[] c) {
		return true;
	}

	@Override
	protected ConstraintNetwork createConstraintNetwork() {
		return new FuzzyAllenIntervalNetwork(this);
	}

	@Override
	protected Variable[] createVariablesSub(int num) {
		SimpleAllenInterval[] ret = new SimpleAllenInterval[num];
		for (int i = 0; i < num; i++) {
			ret[i] = new SimpleAllenInterval(this, IDs++);
		}
		return ret;
	}
	
	/**
	 * Get the upper bound of the possibility degree of this solver's {@link FuzzyAllenIntervalNetwork}. 
	 * @return The upper bound of the possibility degree of this solver's {@link FuzzyAllenIntervalNetwork}.
	 */
	public double getPosibilityDegree() {
		return globalPossibilityDegree;
	}
	
	
	@Override
	public boolean propagate() {
		isSubGraph = false;
		Vector<Vector<HashMap<FuzzyAllenIntervalConstraint.Type, Double>>> frelations = createFuzzyCompleteNetwork(this.getConstraints());
		frelations = simplify(frelations);
		
		//revise frelations w/ PC
		frelations = fuzzyPathConsistency(frelations);
		//update global possibility degree
		updateGlobalPossibilityDegree(frelations);
		return true;
	}
	
	

//	private HashMap<FuzzyAllenIntervalConstraint.Type, Double> movaghati(FuzzyAllenIntervalConstraint.Type type) { 
//		HashMap<FuzzyAllenIntervalConstraint.Type, Double> fr = new HashMap<FuzzyAllenIntervalConstraint.Type, Double>();
//		for (FuzzyAllenIntervalConstraint.Type t : FuzzyAllenIntervalConstraint.Type.values()) fr.put(t, 0.0);
//			for(int t = 0; t <  FuzzyAllenIntervalConstraint.freksa_neighbor[type.ordinal()].length; t++){
//				if (fr.get(FuzzyAllenIntervalConstraint.lookupTypeByInt(t)) != null) {
//					fr.put(FuzzyAllenIntervalConstraint.lookupTypeByInt(t), Math.max(fr.get(FuzzyAllenIntervalConstraint.lookupTypeByInt(t)), FuzzyAllenIntervalConstraint.getPossibilityDegree(FuzzyAllenIntervalConstraint.freksa_neighbor[type.ordinal()][t])));
//				}
//				else {
//					fr.put(FuzzyAllenIntervalConstraint.lookupTypeByInt(t), FuzzyAllenIntervalConstraint.getPossibilityDegree(FuzzyAllenIntervalConstraint.freksa_neighbor[type.ordinal()][t]));
//				}
//			}
//		return fr;
//	}
    
//	private String getFRelationsString(Vector<Vector<HashMap<FuzzyAllenIntervalConstraint.Type, Double>>> frelations) {
//		String ret = "";
//		for(int i = 0; i < frelations.size(); i++){
//			for(int j = 0; j < frelations.size(); j++)
//			{
//				if(i == j) continue;
//				ret += (i + " --> " +  j + " :");
//				ret += (frelations.get(i).get(j) + "\n");
//			}
//		}
//		return ret;
//	}

	
	private boolean isInTheSubGraph(Variable varTmp) {	
		for (int i = 0; i < subs.size(); i++) {
			if(subs.get(i).getInternalVariables()[0].getID() == varTmp.getID()) return true;
		}
		return false;
	}

	

	
	private Constraint[] extractSubGraphCons(Constraint[] c) {
		
		if(!isSubGraph) return c;
 		Vector<Constraint> ctmp = new Vector<Constraint>();
		for (int i = 0; i < c.length; i++) {
			if(isInTheSubGraph(c[i].getScope()[0]) && isInTheSubGraph(c[i].getScope()[1])){
					ctmp.add(c[i]);
			}
		}
		Constraint[] ret = ctmp.toArray(new Constraint[ctmp.size()]);
		return ret;
	}


	//Return value contains possibilities of all 13 relations between all intervals 
	// -- each hashmap is the possibility degree of the relation (i,j), and
	// -- the i-th internal vector contains the relations between i and all other variables 
	private Vector<Vector<HashMap<FuzzyAllenIntervalConstraint.Type, Double>>> createFuzzyCompleteNetwork(Constraint[] cns) {	
		
		if(subs != null){
			if(subs.size() > 0) 
				isSubGraph = true;
		}
		Constraint[] c = extractSubGraphCons(cns);
		
/*		if(isSubGraph){
			
			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			for (int i = 0; i < cns.length; i++) {
				System.out.println("cns: " + cns[i]);	
			}
			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			for (int i = 0; i < subs.size(); i++) {
				System.out.println("sb: " + subs.get(i));
			}
			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			for (int i = 0; i < c.length; i++) {
				System.out.println("c: " + c[i]);	
			}
		} //Iran
*/
		setNumVars(c);
		
		Vector<Vector<HashMap<FuzzyAllenIntervalConstraint.Type, Double>>> frelations = new Vector<Vector<HashMap<FuzzyAllenIntervalConstraint.Type, Double>>>();
		
		final class Coord {
			public int x, y;
			public Coord(int x, int y) { this.x = x; this.y = y; }
			public boolean equals(Object o) {
				if (!(o instanceof Coord)) return false;
				Coord oc = (Coord)o;
				return ((oc.x == x) && (oc.y == y));
			}
			public int hashCode() {
				return (x*31)^y;
			}
		}
		
		HashMap<Coord, Vector<FuzzyAllenIntervalConstraint>> multiple = new HashMap<Coord, Vector<FuzzyAllenIntervalConstraint>>();
		
		
		FuzzyAllenIntervalConstraint[][] tmp = new FuzzyAllenIntervalConstraint[getNumVars()][getNumVars()];
		
	
		
		int row = 0 , col = 0;
		for(int i = 0; i < c.length; i++){
			if(!isSubGraph){
				row = this.getID(c[i].getScope()[0]);
				col = this.getID(c[i].getScope()[1]);
			}
			else{
				row = (int)varIndex.get(c[i].getScope()[0].getID());
				col = (int)varIndex.get(c[i].getScope()[1].getID());
			}
			if (tmp[row][col] == null)
				tmp[row][col] = ((FuzzyAllenIntervalConstraint)c[i]);
			else {
				Coord coord = new Coord(row,col);
				Vector<FuzzyAllenIntervalConstraint> vec = multiple.get(coord);
				if (vec == null) {
					vec = new Vector<FuzzyAllenIntervalConstraint>();
					vec.add(tmp[row][col]);
					multiple.put(coord, vec);
				}
				vec.add((FuzzyAllenIntervalConstraint)c[i]);
			}
		}
		boolean isACrispCons = false;
		for(int i = 0; i < tmp.length; i++){
			Vector<HashMap<FuzzyAllenIntervalConstraint.Type, Double>> con = new Vector<HashMap<FuzzyAllenIntervalConstraint.Type, Double>>(); 
			for(int j = 0; j < tmp.length; j++){
				if(crispCons != null && tmp[i][j] != null){
					if(isACrispConstrint(tmp[i][j])) isACrispCons = true;
				}
				if(tmp[i][j] != null) {
					//add poss (i,j)
					/*
					System.out.println("----------------------------------");
					System.out.println("LOOKING AT " + tmpToConstraints.get(new Coords(i,j)) + ":\n" + (tmpToConstraints.get(new Coords(i,j))).getPossibilities());
					System.out.println("----------------------------------");
					*/
					Coord coord = new Coord(i,j);
					if (!multiple.containsKey(coord)) {
						if(!isACrispCons)
							con.add(tmp[i][j].getPossibilities());
						else
							con.add(tmp[i][j].makeCrispRel());
					}
					else {
						if(!isACrispCons){
							HashMap<FuzzyAllenIntervalConstraint.Type, Double> poss = multiple.get(coord).get(0).getPossibilities();
							for (int h = 1; h < multiple.get(coord).size(); h++) {
								updateRelation(poss, multiple.get(coord).get(h).getPossibilities());
							}
							con.add(poss);
						}
						else{
							HashMap<FuzzyAllenIntervalConstraint.Type, Double> poss = multiple.get(coord).get(0).makeCrispRel();
							for (int h = 1; h < multiple.get(coord).size(); h++) {
								updateRelation(poss, multiple.get(coord).get(h).makeCrispRel());
							}
							con.add(poss);
						}
					}
				}
				else if (tmp[j][i] != null) {
					//add inverse of (i,j)
					/*
					System.out.println("----------------------------------");
					System.out.println("LOOKING AT (I) " + tmpToConstraints.get(new Coords(j,i)) + ":\n" + (tmpToConstraints.get(new Coords(j,i))).getInversePossibilities());
					System.out.println("----------------------------------");
					*/
					Coord coord = new Coord(j,i);
					if(!isACrispCons){
						if (!multiple.containsKey(coord)) {
							con.add(tmp[j][i].getInversePossibilities());
						}
						else {
							HashMap<FuzzyAllenIntervalConstraint.Type, Double> poss = multiple.get(coord).get(0).getInversePossibilities();
							for (int h = 1; h < multiple.get(coord).size(); h++) {
								updateRelation(poss, multiple.get(coord).get(h).getInversePossibilities());
							}
							con.add(poss);
						}
					}
					else{
						if (!multiple.containsKey(coord)) {
							con.add(tmp[j][i].getCrispInverse());
						}
						else {
							HashMap<FuzzyAllenIntervalConstraint.Type, Double> poss = multiple.get(coord).get(0).getCrispInverse();
							for (int h = 1; h < multiple.get(coord).size(); h++) {
								updateRelation(poss, multiple.get(coord).get(h).getCrispInverse());
							}
							con.add(poss);
						}
					}
				}
				
				//if no relation exists, add [all possibilities <-- 1.0]
				else con.add(createAllFuzzyAllenRelation());
			}
			isACrispCons = false;
			frelations.add(con);
		}	
		
		return frelations;
	}
	

		
	

	private boolean isACrispConstrint(
			FuzzyAllenIntervalConstraint fc) {
		// TODO Auto-generated method stub
		for (int i = 0; i < crispCons.length; i++) {
			if (((fc.getFrom().getID() == (crispCons[i].getScope()[0].getID())) && (fc.getTo().getID() == (crispCons[i].getScope()[1].getID()))))
					return true;
		}
		return false;
	}

	private HashMap<FuzzyAllenIntervalConstraint.Type, Double> createAllFuzzyAllenRelation() {
		HashMap<FuzzyAllenIntervalConstraint.Type, Double> fr = new HashMap<FuzzyAllenIntervalConstraint.Type, Double>();
		for(int t = 0; t <  FuzzyAllenIntervalConstraint.freksa_neighbor[FuzzyAllenIntervalConstraint.Type.Before.ordinal()].length; t++)
			fr.put(FuzzyAllenIntervalConstraint.lookupTypeByInt(t), 1.0);
		return fr;
	}
	
	/*
	private HashMap<FuzzyAllenIntervalConstraint.Type, Double> invertPossibilities(HashMap<FuzzyAllenIntervalConstraint.Type, Double> frelation) {
		HashMap<Type, Double> ret = new HashMap<Type, Double>();
		for (Type t : Type.values()) ret.put(t, 0.0);
		for (Type t : frelation.keySet()) {
			if (frelation.get(t) == 1.0) {
				//??? IS THIS CORRECT ??? --> Seems correct
				//get inverse of each relation that is 1.0 
				Type inverseRelation = FuzzyAllenIntervalConstraint.getComplement(t);
				
				//set poss of each inverse relation to 1.0
				ret.put(inverseRelation, frelation.get(t));
				
				//calculate the Freksa N of each inverse relation
				HashMap<FuzzyAllenIntervalConstraint.Type, Double> fr = new HashMap<FuzzyAllenIntervalConstraint.Type, Double>();
				for(int i = 0; i <  FuzzyAllenIntervalConstraint.freksa_neighbor[inverseRelation.ordinal()].length; i++)
					fr.put(FuzzyAllenIntervalConstraint.lookupTypeByInt(i), FuzzyAllenIntervalConstraint.getPossibilityDegree(FuzzyAllenIntervalConstraint.freksa_neighbor[inverseRelation.ordinal()][i]));
				
				//take the maximum between calculated Freksa N and previously added possibilities
				//(because this is an OR)
				for(FuzzyAllenIntervalConstraint.Type t1: fr.keySet())
					ret.put(t1, Math.max(ret.get(t1), fr.get(t1)));
			}
		}
		return ret;
	}
    */

	private Vector<Vector<HashMap<FuzzyAllenIntervalConstraint.Type, Double>>> simplify(Vector<Vector<HashMap<FuzzyAllenIntervalConstraint.Type, Double>>> frelations) {
		int numVars = getNumVars();
		for (int i = 0; i < numVars; i++) {
			for (int j = 0; j < numVars; j++) {
				if (i != j) {
					//Variable from = this.getVariables()[i];
					//Variable to = this.getVariables()[j];
					Variable from = getSubVariable()[i];
					Variable to = getSubVariable()[j];
					FuzzyAllenIntervalConstraint direct = (FuzzyAllenIntervalConstraint) this.getConstraintNetwork().getConstraint(from, to);
					FuzzyAllenIntervalConstraint inverse = (FuzzyAllenIntervalConstraint) this.getConstraintNetwork().getConstraint(to, from);
					if (direct != null && inverse != null) {
						//System.out.println("DOING " + direct + "\nPLUS " + inverse);
						HashMap<FuzzyAllenIntervalConstraint.Type, Double> inversePossibilities = inverse.getInversePossibilities();
						updateRelation(frelations.get(i).get(j), inversePossibilities);
					}
				}
			}
		}
		return frelations;
	}
	
	/*
	private Vector<Vector<HashMap<FuzzyAllenIntervalConstraint.Type, Double>>> fuzzyArcConsistency(Vector<Vector<HashMap<FuzzyAllenIntervalConstraint.Type, Double>>> frelations) {
		int numVars = this.getVariables().length;
		boolean changed = false;
		do {
			changed = false;
			for (int i = 0; i < numVars; i++) {
				for (int j = 0; j < numVars; j++) {
					if (i != j) {
						HashMap<FuzzyAllenIntervalConstraint.Type, Double> temp = (HashMap<Type, Double>)frelations.get(i).get(j).clone();
						HashMap<FuzzyAllenIntervalConstraint.Type, Double> direct = frelations.get(i).get(j);
						HashMap<FuzzyAllenIntervalConstraint.Type, Double> inverse = invertPossibilities(frelations.get(j).get(i));
						updateRelation(direct, inverse);
						if (!compareRelation(direct, temp)) changed = true;
					}
				}
			}
		} while (changed);
		
		return frelations;
	}
	*/
	
	private Vector<Vector<HashMap<FuzzyAllenIntervalConstraint.Type, Double>>> fuzzyPathConsistency(Vector<Vector<HashMap<FuzzyAllenIntervalConstraint.Type, Double>>> frelations) {
		int numVars = getNumVars();
		//need to cycle at least (numVars^2 - numVars) times
		int counter = numVars*numVars - numVars;
		boolean[][] mark = new boolean[numVars][numVars];
		
		for(int i = 0; i < numVars; i++) {
			for(int j = 0; j < numVars; j++) {
				if(i == j) mark[i][j] = false;
				else mark[i][j] = true;
			}
		}	
		
		while(counter != 0) { // while the set is not empty
			for(int i = 0; i < numVars; i++) {
				for(int j = 0; j < numVars; j++) {
					if(i == j) continue;
					if(mark[i][j]){
						mark[i][j] = false;	
						counter--; //remove from set
						for(int k = 0; k < numVars; k++) {
							if(k == i || k == j) continue;
							//process relation (k,j)
							//back up relation (k,j)
							HashMap<FuzzyAllenIntervalConstraint.Type, Double> hashMaptmp = (HashMap<FuzzyAllenIntervalConstraint.Type, Double>) frelations.get(k).get(j).clone();
							//(k,j) <-- (k,j) n (k,i) + (i,j) 
							updateRelation(frelations.get(k).get(j), generateComposition(frelations.get(k).get(i), frelations.get(i).get(j)));
							//if changed, must re-process (k,j)
							if(!compareRelation(frelations.get(k).get(j), hashMaptmp)) {
								if(!mark[k][j]) {
									mark[k][j] = true;
									counter++;
								}
							}
							//process relation (i,k)
							//back up relation (i,k)
							hashMaptmp = (HashMap<FuzzyAllenIntervalConstraint.Type, Double>)frelations.get(i).get(k).clone();
							//(i,k) <-- (i,k) n (i,j) + (j,k)
							updateRelation(frelations.get(i).get(k), generateComposition(frelations.get(i).get(j), frelations.get(j).get(k)));
							//if changed, must re-process (i,k)
							if(!compareRelation(frelations.get(i).get(k), hashMaptmp)) {
								if(!mark[i][k]) {
									mark[i][k] = true;
									counter++;
								}
							}
						}//end of k loop	
					}
				}
			}
		}
		return frelations;
	}

	private void updateGlobalPossibilityDegree(Vector<Vector<HashMap<FuzzyAllenIntervalConstraint.Type, Double>>> frelations) {
		Vector<Double> max = new Vector<Double>();
		//calculate global possibility degree
		for(int i = 0; i < frelations.size(); i++){ 
			for(int j = 0; j < frelations.get(i).size(); j++){
				max.add(Collections.max(frelations.get(i).get(j).values()));
			}
		}	
		globalPossibilityDegree = Collections.min(max);
	}

	private boolean compareRelation(HashMap<FuzzyAllenIntervalConstraint.Type, Double> hashMap1,
			HashMap<FuzzyAllenIntervalConstraint.Type, Double> hashMap2) {
		for(FuzzyAllenIntervalConstraint.Type t: hashMap1.keySet())
			if(Double.compare(hashMap1.get(t), hashMap2.get(t)) != 0) return false;
		return true;
	}

	private void updateRelation(HashMap<FuzzyAllenIntervalConstraint.Type, Double> hashMap1,
			HashMap<FuzzyAllenIntervalConstraint.Type, Double> hashMap2) {
		for(FuzzyAllenIntervalConstraint.Type t: hashMap1.keySet())
			hashMap1.put(t, Math.min(hashMap1.get(t), hashMap2.get(t)));		
	}

	private HashMap<FuzzyAllenIntervalConstraint.Type, Double> generateComposition(HashMap<FuzzyAllenIntervalConstraint.Type, Double> hashMap1,
			HashMap<FuzzyAllenIntervalConstraint.Type, Double> hashMap2) {
		double tmpValue = 0;
		HashMap<FuzzyAllenIntervalConstraint.Type, Double> cmprelation =  new HashMap<FuzzyAllenIntervalConstraint.Type, Double>();
		for(FuzzyAllenIntervalConstraint.Type t: hashMap1.keySet()){
			for(FuzzyAllenIntervalConstraint.Type t2: hashMap2.keySet()){
				FuzzyAllenIntervalConstraint.Type[] tmpType = FuzzyAllenIntervalConstraint.transitionTable[t.ordinal()][t2.ordinal()];
				for(FuzzyAllenIntervalConstraint.Type t3: tmpType){
					if(cmprelation.containsKey(t3)){
						tmpValue = Math.max(cmprelation.get(t3), Math.min(hashMap1.get(t), hashMap2.get(t2)));	
						cmprelation.put(t3, tmpValue);
					}
					else{
						cmprelation.put(t3, Math.min(hashMap1.get(t), hashMap2.get(t2)));
					}
				}	
			}
		}
		return cmprelation;
	}

	@Override
	protected void removeConstraintsSub(Constraint[] c) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void removeVariablesSub(Variable[] v) {
		// TODO Auto-generated method stub
	}

	/**
	 * IRAN: please comment this.
	 * @param fas
	 */
	public void setVarOfSubGraph(Vector<FuzzyActivity> fas) {
		this.subs = fas;  
	}

	private int getNumVars(){
		return numVars;				
	}
	
	private void setNumVars(Constraint[] c) {
		varIndex.clear();
		subVariables.clear();
		numVars = this.getVariables().length;
		for (int i = 0; i < this.getVariables().length; i++) {
			subVariables.add(this.getVariables()[i]);
		}
		if(isSubGraph){
			Vector<Variable> vars = new Vector<Variable>();
			for (int i = 0; i < c.length; i++) {
				if(!vars.contains(c[i].getScope()[0])){
					vars.add(c[i].getScope()[0]);
					varIndex.put(c[i].getScope()[0].getID(), vars.size() - 1);
				}
				if(!vars.contains(c[i].getScope()[1])){
					vars.add(c[i].getScope()[1]);	
					varIndex.put(c[i].getScope()[1].getID(), vars.size() - 1);
				}
			}
			numVars = vars.size();
			subVariables = vars;
		}
	}
	
	private Variable[] getSubVariable(){
		return subVariables.toArray(new Variable[subVariables.size()]);
	}

	/**
	 * Sets the given {@link FuzzyAllenIntervalConstraint}s to be considered as crisp
	 * (meaning that the desired possibility degrees of the constraint should
	 * not change, and the possibility degrees of all other relation types should be 0).
	 * @param crispCons The constraints to set as crisp.
	 */
	public void setCrispCons(Constraint[] crispCons) {
		this.crispCons  = crispCons;
	}

	@Override
	public void registerValueChoiceFunctions() {
		ValueChoiceFunction vcf = new ValueChoiceFunction(){
			@Override
			public Object getValue(Domain dom) {
				return ((SimpleInterval)dom).getIntervalName();
			}};
		Domain.registerValueChoiceFunction(SimpleInterval.class, vcf, "ID");		
	}
	
	

	

}

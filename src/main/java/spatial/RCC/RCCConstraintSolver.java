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
package spatial.RCC;

import java.util.HashMap;
import java.util.Vector;

import framework.Constraint;
import framework.ConstraintNetwork;
import framework.ConstraintSolver;
import framework.Variable;

/**
 * This {@link ConstraintSolver} implements a  path consistency 
 * algorithm, to check the consistency of constraint network based of on RCC (Region Connection calculi) constraints
 * 
 * @author Iran Mansouri
 *
 */
public class RCCConstraintSolver extends ConstraintSolver{

	/**
	 * 
	 */
	private static final long serialVersionUID = 9130340233823443991L;
	private int IDs = 0;
	private HashMap<Integer, Variable> getVaribaleById = new HashMap<Integer, Variable>();
	
	public RCCConstraintSolver() {
		super(new Class[]{RCCConstraint.class}, Region.class);
		this.setOptions(OPTIONS.AUTO_PROPAGATE);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean propagate() {
		
		if(this.getConstraints().length == 0) return true;
		for (int i = 0; i < this.getVariables().length; i++) {
			getVaribaleById.put(this.getVariables()[i].getID(), this.getVariables()[i]);
		}
		Vector<Vector<RCCConstraint>> rccRels = createRCCCompleteNetwork(this.getConstraints());
		System.out.println(PrintSpatialRelation(rccRels));
		if(RCCPathConsistency(rccRels)) 
			return true;
		//RCCPathConsistency(rccRels);
		//System.out.println(PrintSpatialRelation(rccRels));
		return false;
	}
	
	private boolean RCCPathConsistency(
			Vector<Vector<RCCConstraint>> rccRels) {
		int numVars = this.getVariables().length;
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
							RCCConstraint rcctmp = (RCCConstraint) rccRels.get(k).get(j).clone();
							//(k,j) <-- (k,j) n (k,i) + (i,j) 

							if(!updateRelation(rccRels.get(k).get(j), generateComposition(rccRels.get(k).get(i), rccRels.get(i).get(j))))
								return false;
							//if changed, must re-process (k,j)
							
							if(!compareRelation(rcctmp, rccRels.get(k).get(j))) {
								if(!mark[k][j]) {
									mark[k][j] = true;
									counter++;
								}
							}
							//process relation (i,k)
							//back up relation (i,k)
							rcctmp = (RCCConstraint)rccRels.get(i).get(k).clone();
							//(i,k) <-- (i,k) n (i,j) + (j,k)
							
							if(!updateRelation(rccRels.get(i).get(k), generateComposition(rccRels.get(i).get(j), rccRels.get(j).get(k))))
								return false;
							
							//if changed, must re-process (i,k)
							if(!compareRelation(rcctmp, rccRels.get(i).get(k))) {
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

		return true;
	}
	
	//return true when they are equal
	private boolean compareRelation(RCCConstraint first,
			RCCConstraint second) {
		
		boolean existed = false;
		for (int i = 0; i < first.types.length; i++) {
			existed = false;
			for (int j = 0; j < second.types.length; j++) {
				if(first.types[i] == second.types[j])
					existed = true;
			}
			if(!existed) return false;
		}
		
		return true;
	}

	private boolean updateRelation(RCCConstraint originalRealtions,
			Vector<RCCConstraint.Type> generateComposition) {
		

		Vector<Integer> nonexists = new Vector<Integer>();
		boolean exist = false;
		for (int i = 0; i < originalRealtions.getTypes().length; i++) {
			exist = false;
			for (int j = 0; j < generateComposition.size(); j++) {
				if(originalRealtions.getTypes()[i].equals(generateComposition.get(j))){
					exist = true;
					break;
				}
			}
			if(!exist) nonexists.add(i);
		}
		
		
		RCCConstraint.Type[] t = new RCCConstraint.Type[originalRealtions.getTypes().length - nonexists.size()]; 
		int j = 0;
		for (int i = 0; i < originalRealtions.getTypes().length; i++) {
			if(!nonexists.contains(i)){
				t[j] = originalRealtions.getTypes()[i];
				j++;
			}
		}		
		originalRealtions.setTypes(t);
		if(originalRealtions.getTypes().length == 0) return false;
//		for (int i = originalRealtions.getTypes().length - 1; i >= 0; i--) {
//			if(nonexists.contains(i))
//				originalRealtions.remove(i);
//		}
		return true;
	}

	private Vector<RCCConstraint.Type> generateComposition(RCCConstraint o1,
			RCCConstraint o2) {
		Vector<RCCConstraint.Type> cmprelation =  new Vector<RCCConstraint.Type>();
		for (int t = 0; t < o1.types.length; t++) {
			for (int t2 = 0; t2 < o2.types.length; t2++) {
				RCCConstraint.Type[] tmpType = RCCConstraint.transitionTable[o1.types[t].ordinal()][o2.types[t2].ordinal()];
				for(RCCConstraint.Type t3: tmpType){
					if(!cmprelation.contains(t3))				
						cmprelation.add(t3);
					
				}	
			}
		}
		return cmprelation;
	}
	
	private String PrintSpatialRelation(Vector<Vector<RCCConstraint>> rccRels) {
		String ret = "";
		for(int i = 0; i < rccRels.size(); i++){
			for(int j = 0; j < rccRels.size(); j++){
					if(i == j) continue;
					ret += (i + " --> " +  j + " :");
						ret += (rccRels.get(i).get(j)+ "\n");
				}
		}
		return ret;
	}


	private Vector<Vector<RCCConstraint>> createRCCCompleteNetwork(
			Constraint[] c) {
		
		Vector<Vector<RCCConstraint>> rccRels = new Vector<Vector<RCCConstraint>>();
		
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
		
		HashMap<Coord, Vector<RCCConstraint>> multiple = new HashMap<Coord, Vector<RCCConstraint>>();
		RCCConstraint[][] tmp = new RCCConstraint[this.getVariables().length][this.getVariables().length];
		int row = 0 , col = 0;
		for(int i = 0; i < c.length; i++){
			
			row = this.getID(c[i].getScope()[0]);
			col = this.getID(c[i].getScope()[1]);
			if (tmp[row][col] == null)
				tmp[row][col] = ((RCCConstraint)c[i]);
			else {
				Coord coord = new Coord(row,col);
				Vector<RCCConstraint> vec = multiple.get(coord);
				if (vec == null) {
					vec = new Vector<RCCConstraint>();
					vec.add(tmp[row][col]);
					multiple.put(coord, vec);
				}
				vec.add((RCCConstraint)c[i]);
			}
		}
		

		
		for(int i = 0; i < tmp.length; i++){
			Vector<RCCConstraint> con = new Vector<RCCConstraint>(); 
			for(int j = 0; j < tmp.length; j++){
				if(tmp[i][j] != null) {
					Coord coord = new Coord(i,j);
					if (!multiple.containsKey(coord)) {
						con.add(tmp[i][j]);
					}
					else{//if (a u b u c) u d = (a u b u c u d)
						Vector<RCCConstraint.Type> t = new Vector<RCCConstraint.Type>();
						for (int k = 0; k < multiple.get(coord).size(); k++) {
							for (int k2 = 0; k2 < multiple.get(coord).get(k).getTypes().length; k2++)
								t.add(multiple.get(coord).get(k).getTypes()[k2]);							
						} 
						tmp[i][j].setTypes(t.toArray(new RCCConstraint.Type[t.size()]));
						con.add(tmp[i][j]);
					}
				}
				else if (tmp[j][i] != null) {
					Coord coord = new Coord(j,i);
					if (!multiple.containsKey(coord)) {
						Vector<RCCConstraint.Type> t = new Vector<RCCConstraint.Type>();
						for (int k = 0; k < tmp[j][i].types.length; k++) {
							t.add(RCCConstraint.getInverseRelation(tmp[j][i].types[k]));
						}
						RCCConstraint inverse = new RCCConstraint(t.toArray(new RCCConstraint.Type[t.size()]));
						inverse.setFrom(tmp[j][i].getTo());
						inverse.setTo(tmp[j][i].getFrom());
						con.add(inverse);
					}
					else{
						Vector<RCCConstraint.Type> t = new Vector<RCCConstraint.Type>();
						for (int k = 0; k < multiple.get(coord).size(); k++) {
							for (int k2 = 0; k2 < multiple.get(coord).get(k).types.length; k2++) {
								t.add(RCCConstraint.getInverseRelation(multiple.get(coord).get(k).types[k2]));
							}
						}
						RCCConstraint inverse = new RCCConstraint(t.toArray(new RCCConstraint.Type[t.size()]));
						inverse.setFrom(tmp[j][i].getTo());
						inverse.setTo(tmp[j][i].getFrom());
						con.add(inverse);
					}
				}				
				//if no relation exists
				else{ 
					RCCConstraint universe = new RCCConstraint(createAllRCCRelation());
					universe.setFrom(getVaribaleById.get(i));
					universe.setTo(getVaribaleById.get(j));
					con.add(universe);
				}
			}
			rccRels.add(con);
		}	
		return rccRels;
	}

	private RCCConstraint.Type[] createAllRCCRelation() {
		
		Vector<RCCConstraint.Type> allRCCConstraint = new Vector<RCCConstraint.Type>();
		for (int i = 0; i < RCCConstraint.Type.values().length; i++) {
			allRCCConstraint.add(RCCConstraint.Type.values()[i]);
		}
		return allRCCConstraint.toArray(new RCCConstraint.Type[allRCCConstraint.size()]);
	}

	@Override
	protected boolean addConstraintsSub(Constraint[] c) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected void removeConstraintsSub(Constraint[] c) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected Variable[] createVariablesSub(int num) {
		Region[] ret = new Region[num];
		for (int i = 0; i < num; i++) ret[i] = new Region(this, IDs++);
			return ret;
	}

	@Override
	protected void removeVariablesSub(Variable[] v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void registerValueChoiceFunctions() {
		// TODO Auto-generated method stub
		
	}

}

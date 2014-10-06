package org.metacsp.time.qualitative;

import java.util.HashMap;
import java.util.Vector;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;
import org.metacsp.framework.ConstraintSolver.OPTIONS;



public class QualitativeAllenSolver extends ConstraintSolver {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9130340233823443991L;
	private int IDs = 0;
	private HashMap<Integer, Variable> getVaribaleById = new HashMap<Integer, Variable>();
	
	public QualitativeAllenSolver() {
		super(new Class[]{QualitativeAllenIntervalConstraint.class}, SimpleAllenInterval.class);
		this.setOptions(OPTIONS.AUTO_PROPAGATE);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean propagate() {
		
		if(this.getConstraints().length == 0) return true;
		for (int i = 0; i < this.getVariables().length; i++) {
			getVaribaleById.put(this.getVariables()[i].getID(), this.getVariables()[i]);
		}
		Vector<Vector<QualitativeAllenIntervalConstraint>> qarRels = createRCCCompleteNetwork(this.getConstraints());
		//System.out.println(PrintSpatialRelation(rccRels));
		if(pathConsistency(qarRels)) 
			return true;
		//RCCPathConsistency(rccRels);
		//System.out.println(PrintSpatialRelation(rccRels));
		return false;
	}
	
	private boolean pathConsistency(Vector<Vector<QualitativeAllenIntervalConstraint>> qarRels) {
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
							QualitativeAllenIntervalConstraint rcctmp = (QualitativeAllenIntervalConstraint) qarRels.get(k).get(j).clone();
							//(k,j) <-- (k,j) n (k,i) + (i,j) 

							if(!updateRelation(qarRels.get(k).get(j), generateComposition(qarRels.get(k).get(i), qarRels.get(i).get(j))))
								return false;
							//if changed, must re-process (k,j)
							
							if(!compareRelation(rcctmp, qarRels.get(k).get(j))) {
								if(!mark[k][j]) {
									mark[k][j] = true;
									counter++;
								}
							}
							//process relation (i,k)
							//back up relation (i,k)
							rcctmp = (QualitativeAllenIntervalConstraint)qarRels.get(i).get(k).clone();
							//(i,k) <-- (i,k) n (i,j) + (j,k)
							
							if(!updateRelation(qarRels.get(i).get(k), generateComposition(qarRels.get(i).get(j), qarRels.get(j).get(k))))
								return false;
							
							//if changed, must re-process (i,k)
							if(!compareRelation(rcctmp, qarRels.get(i).get(k))) {
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
	private boolean compareRelation(QualitativeAllenIntervalConstraint first,
			QualitativeAllenIntervalConstraint second) {
		
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

	private boolean updateRelation(QualitativeAllenIntervalConstraint originalRealtions,
			Vector<QualitativeAllenIntervalConstraint.Type> generateComposition) {
		

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
		
		
		QualitativeAllenIntervalConstraint.Type[] t = new QualitativeAllenIntervalConstraint.Type[originalRealtions.getTypes().length - nonexists.size()]; 
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

	private Vector<QualitativeAllenIntervalConstraint.Type> generateComposition(QualitativeAllenIntervalConstraint o1,
			QualitativeAllenIntervalConstraint o2) {
		Vector<QualitativeAllenIntervalConstraint.Type> cmprelation =  new Vector<QualitativeAllenIntervalConstraint.Type>();
		for (int t = 0; t < o1.types.length; t++) {
			for (int t2 = 0; t2 < o2.types.length; t2++) {
				QualitativeAllenIntervalConstraint.Type[] tmpType = QualitativeAllenIntervalConstraint.transitionTable[o1.types[t].ordinal()][o2.types[t2].ordinal()];
				for(QualitativeAllenIntervalConstraint.Type t3: tmpType){
					if(!cmprelation.contains(t3))				
						cmprelation.add(t3);
					
				}	
			}
		}
		return cmprelation;
	}
	
	private String PrintSpatialRelation(Vector<Vector<QualitativeAllenIntervalConstraint>> rccRels) {
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


	private Vector<Vector<QualitativeAllenIntervalConstraint>> createRCCCompleteNetwork(Constraint[] c) {
		
		Vector<Vector<QualitativeAllenIntervalConstraint>> qaRel = new Vector<Vector<QualitativeAllenIntervalConstraint>>();
		
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
		
		HashMap<Coord, Vector<QualitativeAllenIntervalConstraint>> multiple = new HashMap<Coord, Vector<QualitativeAllenIntervalConstraint>>();
		QualitativeAllenIntervalConstraint[][] tmp = new QualitativeAllenIntervalConstraint[this.getVariables().length][this.getVariables().length];
		int row = 0 , col = 0;
		for(int i = 0; i < c.length; i++){
			
			row = this.getID(c[i].getScope()[0]);
			col = this.getID(c[i].getScope()[1]);
			if (tmp[row][col] == null)
				tmp[row][col] = ((QualitativeAllenIntervalConstraint)c[i]);
			else {
				Coord coord = new Coord(row,col);
				Vector<QualitativeAllenIntervalConstraint> vec = multiple.get(coord);
				if (vec == null) {
					vec = new Vector<QualitativeAllenIntervalConstraint>();
					vec.add(tmp[row][col]);
					multiple.put(coord, vec);
				}
				vec.add((QualitativeAllenIntervalConstraint)c[i]);
			}
		}
		

		
		for(int i = 0; i < tmp.length; i++){
			Vector<QualitativeAllenIntervalConstraint> con = new Vector<QualitativeAllenIntervalConstraint>(); 
			for(int j = 0; j < tmp.length; j++){
				if(tmp[i][j] != null) {
					Coord coord = new Coord(i,j);
					if (!multiple.containsKey(coord)) {
						con.add(tmp[i][j]);
					}
					else{//if (a u b u c) u d = (a u b u c u d)
						Vector<QualitativeAllenIntervalConstraint.Type> t = new Vector<QualitativeAllenIntervalConstraint.Type>();
						for (int k = 0; k < multiple.get(coord).size(); k++) {
							for (int k2 = 0; k2 < multiple.get(coord).get(k).getTypes().length; k2++)
								t.add(multiple.get(coord).get(k).getTypes()[k2]);							
						} 
						tmp[i][j].setTypes(t.toArray(new QualitativeAllenIntervalConstraint.Type[t.size()]));
						con.add(tmp[i][j]);
					}
				}
				else if (tmp[j][i] != null) {
					Coord coord = new Coord(j,i);
					if (!multiple.containsKey(coord)) {
						Vector<QualitativeAllenIntervalConstraint.Type> t = new Vector<QualitativeAllenIntervalConstraint.Type>();
						for (int k = 0; k < tmp[j][i].types.length; k++) {
							t.add(QualitativeAllenIntervalConstraint.getInverseRelation(tmp[j][i].types[k]));
						}
						QualitativeAllenIntervalConstraint inverse = new QualitativeAllenIntervalConstraint(t.toArray(new QualitativeAllenIntervalConstraint.Type[t.size()]));
						inverse.setFrom(tmp[j][i].getTo());
						inverse.setTo(tmp[j][i].getFrom());
						con.add(inverse);
					}
					else{
						Vector<QualitativeAllenIntervalConstraint.Type> t = new Vector<QualitativeAllenIntervalConstraint.Type>();
						for (int k = 0; k < multiple.get(coord).size(); k++) {
							for (int k2 = 0; k2 < multiple.get(coord).get(k).types.length; k2++) {
								t.add(QualitativeAllenIntervalConstraint.getInverseRelation(multiple.get(coord).get(k).types[k2]));
							}
						}
						QualitativeAllenIntervalConstraint inverse = new QualitativeAllenIntervalConstraint(t.toArray(new QualitativeAllenIntervalConstraint.Type[t.size()]));
						inverse.setFrom(tmp[j][i].getTo());
						inverse.setTo(tmp[j][i].getFrom());
						con.add(inverse);
					}
				}				
				//if no relation exists
				else{ 
					QualitativeAllenIntervalConstraint universe = new QualitativeAllenIntervalConstraint(createAllRCCRelation());
					universe.setFrom(getVaribaleById.get(i));
					universe.setTo(getVaribaleById.get(j));
					con.add(universe);
				}
			}
			qaRel.add(con);
		}	
		return qaRel;
	}

	private QualitativeAllenIntervalConstraint.Type[] createAllRCCRelation() {
		
		Vector<QualitativeAllenIntervalConstraint.Type> allRCCConstraint = new Vector<QualitativeAllenIntervalConstraint.Type>();
		for (int i = 0; i < QualitativeAllenIntervalConstraint.Type.values().length; i++) {
			allRCCConstraint.add(QualitativeAllenIntervalConstraint.Type.values()[i]);
		}
		return allRCCConstraint.toArray(new QualitativeAllenIntervalConstraint.Type[allRCCConstraint.size()]);
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
		SimpleAllenInterval[] ret = new SimpleAllenInterval[num];
		for (int i = 0; i < num; i++) ret[i] = new SimpleAllenInterval(this, IDs++);
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

package spatial.rectangleAlgebra;

import java.util.HashMap;
import java.util.Vector;

import multi.allenInterval.AllenIntervalConstraint;
import spatial.rectangleAlgebra.QualitativeAllenIntervalConstraint.Type;
import time.Bounds;
import framework.Constraint;
import framework.ConstraintNetwork;
import framework.ConstraintSolver;
import framework.Variable;
/**
 * This class represents Rectangle constraints. Each constraint represents two dimension Allen relations between spatial entities.    
 * In rectangle Algebra, each spatial entity is restricted to be an axes parallel rectangle.
 * 
 * @author Iran Mansouri
 *
 */

public class RectangleConstraintSolver extends ConstraintSolver{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6694598836324746725L;
	private int IDs = 0;
	private HashMap<Integer, Variable> getVaribaleById = new HashMap<Integer, Variable>();
	private boolean debug = false;
	private Vector<Vector<RectangleConstraint>> recRels; //= new Vector<Vector<RectangleConstraint>>(); 
	private Vector<AugmentedRectangleConstraint> boundedCons;// = new Vector<AugmentedRectangleConstraint>();
	private HashMap<String, AugmentedRectangleConstraint> durationByname = new HashMap<String, AugmentedRectangleConstraint>();
	public enum Dimension  {X, Y};
	
	
	public RectangleConstraintSolver() {
		super(new Class[]{RectangleConstraint.class},new Class[]{RectangularRegion.class});
		this.setOptions(OPTIONS.AUTO_PROPAGATE);
	}
	
	public Vector<Vector<RectangleConstraint>> getCompleteRARelations(){
		return recRels;
	}
	
	public Bounds[] getDurationConstraintByVariableName(String name, Dimension di){
		if(di == Dimension.X)
			return durationByname.get(name).getBoundedConstraintX().getBounds();
		if(di == Dimension.Y)
			return durationByname.get(name).getBoundedConstraintY().getBounds();
		return null;
	}
	


	public Vector<AugmentedRectangleConstraint> getBoundedConstraint() {
		return boundedCons;
	}

	@Override
	protected ConstraintNetwork createConstraintNetwork() {
		return new RectangleConstraintNetwork(this);
	}

	

	@Override
	protected  boolean addConstraintSub(Constraint c) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean addConstraintsSub(Constraint[] c) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected void removeConstraintSub(Constraint c) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void removeConstraintsSub(Constraint[] c) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Variable createVariableSub() {
		RectangularRegion ret = new RectangularRegion(this, IDs++);
		return ret;
	}
	
	
	@Override
	protected Variable[] createVariablesSub(int num) {
		RectangularRegion[] ret = new RectangularRegion[num];
		for (int i = 0; i < num; i++) ret[i] = new RectangularRegion(this, IDs++);
			return ret;
	}

	@Override
	protected void removeVariableSub(Variable v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void removeVariablesSub(Variable[] v) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean propagate() {
		
		if(this.getConstraints().length == 0) return true;
		for (int i = 0; i < this.getVariables().length; i++) {
			getVaribaleById.put(this.getVariables()[i].getID(), this.getVariables()[i]);
		}
		
		recRels = new Vector<Vector<RectangleConstraint>>();
		boundedCons = new Vector<AugmentedRectangleConstraint>();
//		recRels.clear();
//		boundedCons.clear();
		recRels = createAllenCompleteNetwork(this.getConstraints());
		
		
		if(debug){
			boolean fail = true;
			System.out.println("Debug Mode:");
			System.out.println("Before: ");
			System.out.println(PrintSpatialRelation(recRels));
			
			
			if(weakPathConsistency(recRels))
				fail = false;
			
			System.out.println("After:");
			System.out.println(PrintSpatialRelation(recRels));
			if(fail) return false;
			else return true;
		}
		
		if(!debug){
			if(weakPathConsistency(recRels))
				return true;
			return false;
		}
		
		return false;
	}
	
	private String PrintSpatialRelation(Vector<Vector<RectangleConstraint>> recRels) {
		
		String ret = "";
		for(int i = 0; i < recRels.size(); i++){
			for(int j = 0; j < recRels.size(); j++){
					if(i == j) continue;
					ret += ( "\n"+ i + " --> " +  j + " :");
					String rectangleRel = "";
					if(recRels.get(i).get(j).getTypes().length != 0 && recRels.get(i).get(j).getTypes().length != 0){
						for (int j2 = 0; j2 < recRels.get(i).get(j).getTypes().length; j2++) {
							if(j2 > 0)
								rectangleRel += " v ";
							rectangleRel += " (" + recRels.get(i).get(j).getTypes()[j2].getAllenType()[0] + ", " +  recRels.get(i).get(j).getTypes()[j2].getAllenType()[1]+") " + " + " 
								+ RectangleConstraint.getRCCConstraint(recRels.get(i).get(j).getTypes()[j2].getAllenType()[0], recRels.get(i).get(j).getTypes()[j2].getAllenType()[1]) + "  ";
						}
					}
					if(rectangleRel == "")
						rectangleRel = " no Constraint ";
					ret +=  recRels.get(i).get(j).getFrom() + rectangleRel + recRels.get(i).get(j).getTo() +"\n";
				}
		}
		return ret;
	}


	private boolean weakPathConsistency(Vector<Vector<RectangleConstraint>> recRels) {
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
							RectangleConstraint rectmp = (RectangleConstraint)recRels.get(k).get(j).clone();
							//(k,j) <-- (k,j) n (k,i) + (i,j) 
							if(!revise(recRels.get(k).get(j), recRels.get(k).get(i), recRels.get(i).get(j)))
								return false;

							//if changed, must re-process (k,j)
							
							if(!compareRelation(rectmp, recRels.get(k).get(j))) {
								
								if(!mark[k][j]) {
									mark[k][j] = true;
									counter++;
								}
							}
							//process relation (i,k)
							//back up relation (i,k)
							rectmp = (RectangleConstraint)recRels.get(i).get(k).clone();
							//(i,k) <-- (i,k) n (i,j) + (j,k)
							
							if(!revise(recRels.get(i).get(k), recRels.get(i).get(j), recRels.get(j).get(k)))
								return false;
					
								
							
							//if changed, must re-process (i,k)
							if(!compareRelation(rectmp, recRels.get(i).get(k))) {
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
	
	//this is not true
	private boolean compareRelation(RectangleConstraint first, RectangleConstraint second) {
				
		boolean existed = false;
		for (int i = 0; i < first.types.length; i++) {
			existed = false;
			for (int j = 0; j < second.types.length; j++) {
				if(first.types[i].getAllenType()[0] == second.types[j].getAllenType()[0] && first.types[i].getAllenType()[1] == second.types[j].getAllenType()[1])
					existed = true;
			}
			if(!existed) return false;
		}
		return true;		
	}


	private boolean revise(RectangleConstraint originalRealtions, RectangleConstraint recCons, RectangleConstraint recCons2) {
	
		Vector<QualitativeAllenIntervalConstraint.Type> xcomRelation = new Vector<QualitativeAllenIntervalConstraint.Type>();
		Vector<QualitativeAllenIntervalConstraint.Type> ycomRelation = new Vector<QualitativeAllenIntervalConstraint.Type>();
		for (int t = 0; t < recCons.types.length; t++) {
			for (int t2 = 0; t2 < recCons2.types.length; t2++) {
				QualitativeAllenIntervalConstraint.Type[] xType = QualitativeAllenIntervalConstraint.transitionTable[recCons.types[t].getAllenType()[0].ordinal()][recCons2.types[t2].getAllenType()[0].ordinal()];
				QualitativeAllenIntervalConstraint.Type[] yType = QualitativeAllenIntervalConstraint.transitionTable[recCons.types[t].getAllenType()[1].ordinal()][recCons2.types[t2].getAllenType()[1].ordinal()];
				for(QualitativeAllenIntervalConstraint.Type t3: xType){
					if(!xcomRelation.contains(t3))				
						xcomRelation.add(t3);
				}
				for(QualitativeAllenIntervalConstraint.Type t3: yType){
						if(!ycomRelation.contains(t3))				
							ycomRelation.add(t3);
				}
			}
		}
		//zero for x and 1 for y
		Vector<Integer> xnonexists = getNonExistedIndices(originalRealtions, xcomRelation, 0);
		Vector<Integer> ynonexists = getNonExistedIndices(originalRealtions, ycomRelation, 1);
		
		//TwoDimensionsAllenConstraint[] t = new TwoDimensionsAllenConstraint[originalRealtions.getTypes().length - Math.min(xnonexists.size(), ynonexists.size())]; 
		Vector<TwoDimensionsAllenConstraint> t = new Vector<TwoDimensionsAllenConstraint>();
		
		for (int i = 0; i < originalRealtions.getTypes().length; i++) 
			if((!xnonexists.contains(i)) && (!ynonexists.contains(i)))
				t.add(originalRealtions.getTypes()[i]);
			
		originalRealtions.setTypes(t.toArray(new TwoDimensionsAllenConstraint[t.size()]));
		//if the result of intersection is empty 
		if(t.size() == 0) return false;
		
		// TODO Auto-generated method stub
		return true;
	}




	private Vector<Integer> getNonExistedIndices(
			RectangleConstraint originalRealtions, Vector<Type> comRelation, int dimension) {
		
		Vector<Integer> nonexists = new Vector<Integer>();
		boolean exist = false;
		for (int i = 0; i < originalRealtions.getTypes().length; i++) {
			exist = false;
			for (int j = 0; j < comRelation.size(); j++) {
				if(originalRealtions.getTypes()[i].getAllenType()[dimension].equals(comRelation.get(j))){
					exist = true;
					break;
				}
			}
			if(!exist) nonexists.add(i);
		}
		
		return nonexists;

	}


	private Vector<Vector<RectangleConstraint>> createAllenCompleteNetwork(Constraint[] c) {

		RectangleConstraint[][] tmp = new RectangleConstraint[this.getVariables().length][this.getVariables().length];
		int row = 0 , col = 0;
		for(int i = 0; i < c.length; i++){
			//this is because the Augmented rectangle Algebra Allow to have During which is need for further process, in this way, we do not iterate again to extract During 
			//if(c[i] instanceof AugmentedRectangleConstraint){
				if(((AugmentedRectangleConstraint)c[i]).getBoundedConstraintX() != null && ((AugmentedRectangleConstraint)c[i]).getBoundedConstraintY() != null){
					boundedCons.add((AugmentedRectangleConstraint)c[i]);
					if(((AugmentedRectangleConstraint)c[i]).getBoundedConstraintX().getType().equals(AllenIntervalConstraint.Type.Duration) && 
							((AugmentedRectangleConstraint)c[i]).getBoundedConstraintY().getType().equals(AllenIntervalConstraint.Type.Duration))			
					durationByname.put(((RectangularRegion)((AugmentedRectangleConstraint)c[i]).getFrom()).getName(), (AugmentedRectangleConstraint)c[i]);
					continue;
				}
			//}
			row = this.getID(c[i].getScope()[0]);
			col = this.getID(c[i].getScope()[1]);
			tmp[row][col] = (RectangleConstraint)c[i];
		}
		
		for(int i = 0; i < tmp.length; i++){
			Vector<RectangleConstraint> con = new Vector<RectangleConstraint>(); 
			for(int j = 0; j < tmp.length; j++){
				if(tmp[i][j] != null)  						
					con.add(tmp[i][j]);
				else if (tmp[j][i] != null){
					Vector<TwoDimensionsAllenConstraint> invers2DAllenRelains = new Vector<TwoDimensionsAllenConstraint>();
					for (int k = 0; k < tmp[j][i].getTypes().length; k++) {
						invers2DAllenRelains.add(new TwoDimensionsAllenConstraint(QualitativeAllenIntervalConstraint.getInverseRelation(tmp[j][i].types[k].getAllenType()[0]), QualitativeAllenIntervalConstraint.getInverseRelation(tmp[j][i].types[k].getAllenType()[1])));
					}
					RectangleConstraint rc = new RectangleConstraint(invers2DAllenRelains.toArray(new TwoDimensionsAllenConstraint[invers2DAllenRelains.size()]));
					rc.setFrom(getVaribaleById.get(i));
					rc.setTo(getVaribaleById.get(j));
					con.add(rc);	
				}
				//if no relation exists
				else{
					RectangleConstraint universe = createUnivreseRectangleRelation();
					universe.setFrom(getVaribaleById.get(i));
					universe.setTo(getVaribaleById.get(j));
					con.add(universe);
				}
			}
			recRels.add(con);
		}
		return recRels;
	}


	private RectangleConstraint createUnivreseRectangleRelation() {
		
		Vector<TwoDimensionsAllenConstraint> tdr = new Vector<TwoDimensionsAllenConstraint>();  
		for (int i = 0; i < QualitativeAllenIntervalConstraint.Type.values().length; i++) 
			for (int j = 0; j < QualitativeAllenIntervalConstraint.Type.values().length; j++)
				tdr.add(new TwoDimensionsAllenConstraint(QualitativeAllenIntervalConstraint.Type.values()[i], QualitativeAllenIntervalConstraint.Type.values()[j]));

		return new RectangleConstraint(tdr.toArray(new TwoDimensionsAllenConstraint[tdr.size()]));
	}	
	
	

}
	
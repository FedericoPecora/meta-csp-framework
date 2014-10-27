package org.metacsp.meta.hybridPlanner;

import java.util.HashMap;
import java.util.Vector;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariablePrototype;
import org.metacsp.framework.meta.MetaVariable;
import org.metacsp.meta.simplePlanner.PlanningOperator;
import org.metacsp.meta.simplePlanner.SimpleDomain;
import org.metacsp.meta.simplePlanner.SimpleOperator;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.spatial.rectangleAlgebra.BoundingBox;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangleConstraint;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangleConstraintSolver;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangularRegion;
import org.metacsp.multi.spatial.rectangleAlgebra.UnaryRectangleConstraint;
import org.metacsp.multi.spatioTemporal.SpatialFluent;
import org.metacsp.multi.spatioTemporal.SpatialFluentSolver;
import org.metacsp.spatial.utility.SpatialRule;
import org.metacsp.time.Bounds;


public class FluentBasedSimpleDomain extends SimpleDomain {
	
	private long timeNow = -1;
	private boolean activeFreeArmHeuristic = false;
	private ManipulationAreaDomain manipulationAreaDomain = null;
	public FluentBasedSimpleDomain(int[] capacities, String[] resourceNames,
			String domainName) {
		super(capacities, resourceNames, domainName);
		manipulationAreaDomain = new ManipulationAreaDomain();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 8380363685271158262L;

	@Override
	public ConstraintNetwork[] getMetaVariables() {
		
		ActivityNetworkSolver groundSolver = (ActivityNetworkSolver)getGroundSolver();//(ActivityNetworkSolver)this.metaCS.getConstraintSolvers()[0];
		Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
		// for every variable that is marked as UNJUSTIFIED a ConstraintNetwork is built
		Vector<Variable> tasks = new Vector<Variable>();
		HashMap<Variable, String> oprParameter = new HashMap<Variable, String>();
		for (Variable task : groundSolver.getVariables()) {
			if (task.getMarking().equals(markings.UNJUSTIFIED)) {
				tasks.add(task);
				oprParameter.put(task, getParameter(task));
			}
		}
		
		HashMap<ConstraintNetwork, Integer> sortedConflict = new HashMap<ConstraintNetwork, Integer>();
		if(((SimpleHybridPlanner)this.metaCS).getConflictRanking() != null){
			for (Variable task : oprParameter.keySet()) {
				ConstraintNetwork nw = new ConstraintNetwork(null);
				nw.addVariable(task);
				if(((SimpleHybridPlanner)this.metaCS).getConflictRanking().get(oprParameter.get(task)) != null)
					sortedConflict.put(nw, ((SimpleHybridPlanner)this.metaCS).getConflictRanking().get(oprParameter.get(task)));
				else 
					sortedConflict.put(nw, 0);
						
				//create constraint network
			}
//			System.out.println("++++++++++++++++++++++++++++++++++++++++++++");
//			System.out.println(sortedConflict);
			sortedConflict = sortHashMapByValues(sortedConflict);
//			System.out.println("+++++++++++++++++++++++++++++++++++++++");
//			System.out.println(sortedConflict);
//			System.out.println("___________________________________________");
			ret.addAll(sortedConflict.keySet());
		}else{
			for (int i = 0; i < tasks.size(); i++) {
				ConstraintNetwork nw = new ConstraintNetwork(null);
				nw.addVariable(tasks.get(i));
				ret.add(nw);				
			}
		}
		
		
		
		return ret.toArray(new ConstraintNetwork[ret.size()]);
	}
	
	@Override
	public ConstraintNetwork[] getMetaValues(MetaVariable metaVariable) {
		Vector<ConstraintNetwork> retPossibleConstraintNetworks = new Vector<ConstraintNetwork>();
		ConstraintNetwork problematicNetwork = metaVariable.getConstraintNetwork();
		Activity problematicActivity = (Activity)problematicNetwork.getVariables()[0]; 
		
		Vector<ConstraintNetwork> operatorsConsNetwork = new Vector<ConstraintNetwork>();
		Vector<ConstraintNetwork> unificationConsNetwork = new Vector<ConstraintNetwork>();
		
		//If it's a sensor, it needs to be unified
		if (isSensor(problematicActivity.getComponent())) {
			return super.getUnifications(problematicActivity);
		}
		
		
//		System.out.println("+++++++++++++++++++++++++++++++++++++++++");
		//If it's a controllable sensor, it needs to be unified (or expanded, see later) 
		if (isControllable(problematicActivity.getComponent())) {
			ConstraintNetwork[] unifications = getUnifications(problematicActivity);
//			System.out.println("I AM AT LEAST CONTRAOLLABLE");
			if(unifications != null){
//				System.out.println("TRYING: " + problematicActivity);
				for (int i = 0; i < unifications.length; i++) {
					//add if it is not the key and is true					
					Activity unifiedAct = null;
					for (int j = 0; j < unifications[i].getVariables().length; j++) {
						if(!((Activity)unifications[i].getVariables()[j]).equals(problematicActivity))
							unifiedAct = (Activity)unifications[i].getVariables()[j];
					}
					if(!unificationTrack.keySet().contains(unifiedAct)){						
						unificationConsNetwork.add(unifications[i]);
						unificationTrack.put(problematicActivity, unifiedAct);
//						System.out.println("UNIFIED: " + unifiedAct);
					}
					else{						
//						System.out.println("SKIPED: " +unifiedAct);						
					}
				}
			}
		}

//		System.out.println("+++++++++++++++++++++++++++++++++++++++++");
		
		
		

//		//If it's a controllable sensor, it needs to be unified (or expanded, see later) 
//		if (isControllable(problematicActivity.getComponent())) {
//			ConstraintNetwork[] unifications = getUnifications(problematicActivity);
//			if(unifications != null){
//				for (int i = 0; i < unifications.length; i++) {
//					System.out.println(unifications[i]);
//					unificationConsNetwork.add(unifications[i]);
//				}
//			}
//		}
		
		//If it's a context var, it needs to be unified (or expanded, see later) 
		if (isContextVar(problematicActivity.getComponent())) {
			ConstraintNetwork[] unifications = getUnifications(problematicActivity);
			if (unifications != null) {
				System.out.println("lenght: " + unifications.length);
				for (ConstraintNetwork oneUnification : unifications) {
					retPossibleConstraintNetworks.add(oneUnification);
					oneUnification.setAnnotation(2);
				}
			}
		}
		
		

		
		VariablePrototype manipulationAreaPrototype = null;
		
		//Find all expansions
		for (SimpleOperator r : operators) {
			String problematicActivitySymbolicDomain = problematicActivity.getSymbolicVariable().getSymbols()[0];
			String operatorHead = r.getHead();
			String opeatorHeadComponent = operatorHead.substring(0, operatorHead.indexOf("::"));
			String operatorHeadSymbol = operatorHead.substring(operatorHead.indexOf("::")+2, operatorHead.length());
			if (opeatorHeadComponent.equals(problematicActivity.getComponent())) {
				if (problematicActivitySymbolicDomain.contains(operatorHeadSymbol)) {
					ConstraintNetwork newResolver = expandOperator(r,problematicActivity);
					for (int i = 0; i < newResolver.getVariables().length; i++) {
						if(newResolver.getVariables()[i] instanceof VariablePrototype){
							String symbol = (String)((VariablePrototype) newResolver.getVariables()[i]).getParameters()[1];
							if(symbol.contains("at_robot1_manipulationArea")){
//								System.out.println("symbol: " + symbol);
								manipulationAreaPrototype = ((VariablePrototype) newResolver.getVariables()[i]);
								break;
							}							
						}
					}
					newResolver.setAnnotation(1);
					newResolver.setSpecilizedAnnotation(r);
					operatorsConsNetwork.add(newResolver);
					//retPossibleConstraintNetworks.add(newResolver);					
				}
			}
			
			
//			System.out.println("__________________________________");
//			System.out.println(operatorsConsNetwork);
//			System.out.println("__________________________________");

			if (r instanceof PlanningOperator) {
				for (String reqState : r.getRequirementActivities()) {
					String operatorEffect = reqState;
					String opeatorEffectComponent = operatorEffect.substring(0, operatorEffect.indexOf("::"));
					String operatorEffectSymbol = operatorEffect.substring(operatorEffect.indexOf("::")+2, operatorEffect.length());
					if (((PlanningOperator)r).isEffect(reqState)) {
						if (opeatorEffectComponent.equals(problematicActivity.getComponent())) {
							if (problematicActivitySymbolicDomain.contains(operatorEffectSymbol)) {
								ConstraintNetwork newResolver = expandOperator(r,problematicActivity);
								newResolver.annotation = r;
								newResolver.setAnnotation(1);
								retPossibleConstraintNetworks.add(newResolver);
							}
						}
					}
				}
			}
		}
		
		if(problematicActivity.getComponent().compareTo("RobotAction") == 0){
			if(manipulationAreaPrototype != null){
				ConstraintNetwork spatialConstraintNet = getSpatialConstraintNet(problematicActivity, manipulationAreaPrototype);
				if(spatialConstraintNet == null) {
					operatorsConsNetwork.lastElement().setSpecilizedAnnotation(false);;
				}
				else{
					operatorsConsNetwork.lastElement().join(spatialConstraintNet);	
				}
			}
		}
		
		activeFreeArmHeuristic = false;
		if(!activeFreeArmHeuristic ){			
			retPossibleConstraintNetworks.addAll(unificationConsNetwork);
			retPossibleConstraintNetworks.addAll(operatorsConsNetwork);				
		}
		else{
			retPossibleConstraintNetworks.addAll(unificationConsNetwork);		
			HashMap<ConstraintNetwork, Integer> sortedResolvers = new HashMap<ConstraintNetwork, Integer>();
			for (int j = 0; j < operatorsConsNetwork.size(); j++) {
				if(operatorsConsNetwork.get(j).getSpecilizedAnnotation() != null)
					sortedResolvers.put(operatorsConsNetwork.get(j), super.operatorsLevels.get(operatorsConsNetwork.get(j).getSpecilizedAnnotation()));
			}
			//sortedResolvers.putAll(rankedUnification);
			sortedResolvers = sortHashMapByValues(sortedResolvers);
//			System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
//			System.out.println(sortedResolvers);
//			System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
			retPossibleConstraintNetworks.addAll(sortedResolvers.keySet());
		}
		
		//this is hard coded, it has to be unhard coded
		//this is for the situation we have to force unification of manipulation activity with spatial flunets and avoid self unification (like duration)
		//ReachabilityContraintSolver rchCs = (ReachabilityContraintSolver)((SpatialFluentSolver)this.getGroundSolver()).getConstraintSolvers()[2];
//		if(problematicActivity.getSymbolicVariable().getSymbols()[0].contains("at_robot1_manipulationArea")) {
////			System.out.println("*****" + retPossibleConstraintNetworks.get(retPossibleConstraintNetworks.size() - 1));
//			retPossibleConstraintNetworks.removeElementAt(retPossibleConstraintNetworks.size() - 1);
//		}

		
		
//		System.out.println("============================================");
//		System.out.println("..." + retPossibleConstraintNetworks);
//		System.out.println("============================================");
		
		if (!retPossibleConstraintNetworks.isEmpty()) return retPossibleConstraintNetworks.toArray(new ConstraintNetwork[retPossibleConstraintNetworks.size()]);
		else if (isControllable(problematicActivity.getComponent())) {
			ConstraintNetwork nullActivityNetwork = new ConstraintNetwork(null);
			nullActivityNetwork.setSpecilizedAnnotation(false);
			return new ConstraintNetwork[] {nullActivityNetwork};
			
		}
		ConstraintNetwork nullActivityNetwork = new ConstraintNetwork(null);
		return new ConstraintNetwork[] {nullActivityNetwork};
	}
	

	
	private ConstraintNetwork getSpatialConstraintNet(Activity problematicActivity, VariablePrototype manipulationAreaPrototype) {
		
		ConstraintNetwork ret = new ConstraintNetwork(null);
		String mainString = problematicActivity.getSymbolicVariable().getSymbols()[0];
//		//place_cup1_RA_west_table
		String obj = getParameter(problematicActivity);

		
		int last_index = mainString.lastIndexOf("_", mainString.length());
		String armAndDirection = mainString.substring(mainString.indexOf(obj)+obj.length()+1,last_index); //e.g., LA_north
		String supporter = mainString.substring(last_index+1, mainString.length()-2); //e.g., table1
		
		//we can extract relevant spatial fluent in two ways: based on the temporal or spatial relation
		//for spatial: bounded rectangle are those in future and unbounded in the past
		SpatialFluent objectFleunt = null;
		SpatialFluent supportFluent = null;
		boolean isPlace = true;
		for (int i = 0; i < ((SpatialFluentSolver)metaCS.getConstraintSolvers()[0]).getVariables().length; i++) {
			SpatialFluent tempFluent = ((SpatialFluent)((SpatialFluentSolver)metaCS.getConstraintSolvers()[0]).getVariables()[i]);
			if(tempFluent.getName().compareTo("at_"+supporter+"_"+supporter) == 0){
				supportFluent = tempFluent;
			}
			if(mainString.contains("pick")){
				if(tempFluent.getName().compareTo("at_"+obj+"_"+supporter) == 0 && 	
				tempFluent.getActivity().getTemporalVariable().getEST() == tempFluent.getActivity().getTemporalVariable().getLST()){ 
					//it is observed but it has be the last spatial fluent which has this property in case of online pick and place  						
					objectFleunt = tempFluent;
					isPlace = false;
//					System.out.println("pick --->" + tempFluent);
				}
			}
			else if(mainString.contains("place")){
				if(tempFluent.getName().compareTo("at_"+obj+"_"+supporter) == 0 && 
						tempFluent.getActivity().getTemporalVariable().getEST() != tempFluent.getActivity().getTemporalVariable().getLST()){ 
					//it is observed but it has be the last spatial fluent which has this property in case of online pick and place  						
					objectFleunt = tempFluent;
//					System.out.println("place --->" + tempFluent);
				}
			}
			
		}
		
		if(objectFleunt == null) return null;
		//ret.addVariable(objectFleunt);
		//ret.addVariable(supportFluent);

		Vector<Constraint> allConstraints = new Vector<Constraint>();
		Vector<SpatialRule> srules = manipulationAreaDomain.getSpatialRulesByRelation(armAndDirection);
		
		RectangleConstraintSolver recSolver = (RectangleConstraintSolver)((SpatialFluentSolver) this.metaCS.getConstraintSolvers()[0]).getConstraintSolvers()[0];
		RectangularRegion placingRecVar = (RectangularRegion) recSolver.createVariable();
		if(isPlace)
			placingRecVar.setName("placingArea_"+obj+"_"+armAndDirection);
		else
			placingRecVar.setName("pickingArea_"+obj+"_"+armAndDirection);
			
		ret.addVariable(placingRecVar);
		
		
		//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
		//creating spatial constraint
		//the order is fixed and based on the fixed order which manipulation domain has defined
		//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
		//"manipulationArea", "manipulationArea",
		Bounds[] sizeBounds = new Bounds[srules.get(0).getUnaryRAConstraint().getBounds().length];
		for (int j = 0; j < sizeBounds.length; j++) {
			Bounds bSize = new Bounds(
					srules.get(0).getUnaryRAConstraint().getBounds()[j].min,
					srules.get(0).getUnaryRAConstraint().getBounds()[j].max);
			sizeBounds[j] = bSize;
		}
		UnaryRectangleConstraint sizemanipulationArea = new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.Size, sizeBounds);
		sizemanipulationArea.setFrom(manipulationAreaPrototype);
		sizemanipulationArea.setTo(manipulationAreaPrototype);
		allConstraints.add(sizemanipulationArea);

		//"placingArea", "placingArea", 
		Bounds[] sizeBoundsPlacing = new Bounds[srules.get(1).getUnaryRAConstraint().getBounds().length];
		for (int j = 0; j < sizeBoundsPlacing.length; j++) {
			Bounds bSize = new Bounds(
					srules.get(1).getUnaryRAConstraint().getBounds()[j].min,
					srules.get(1).getUnaryRAConstraint().getBounds()[j].max);
			sizeBoundsPlacing[j] = bSize;
		}
		UnaryRectangleConstraint sizePlacingArea = new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.Size, sizeBoundsPlacing);
		sizePlacingArea.setFrom(placingRecVar);
		sizePlacingArea.setTo(placingRecVar);
		allConstraints.add(sizePlacingArea);
		
		
		//"manipulationArea", "table", 		
		RectangleConstraint manipulationAreaTOtable = null;
		if(srules.get(4).getBinaryRAConstraint().getInternalAllenIntervalConstraints()[0].getTypes().length == 1){
			
			Bounds[] allenBoundsX = new Bounds[(srules.get(4).getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[0].getBounds().length];
			for (int j = 0; j < allenBoundsX.length; j++) {
				Bounds bx = new Bounds(
						(srules.get(4).getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[0].getBounds()[j].min, (srules.get(4)
								.getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[0].getBounds()[j].max);
				allenBoundsX[j] = bx;
			}
			AllenIntervalConstraint xAllenCon = new AllenIntervalConstraint((srules.get(4).getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[0].getTypes()[0], allenBoundsX);
			if((srules.get(4).getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[0].getBounds().length == 0)
				xAllenCon = (AllenIntervalConstraint)(srules.get(4).getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[0].clone();
			AllenIntervalConstraint yAllenCon = manipulationAreaDomain.getConvexifyBeforeAndAfter(); 
			manipulationAreaTOtable = new RectangleConstraint(xAllenCon, yAllenCon);
			
		}
		else{
			
			Bounds[] allenBoundsY = new Bounds[(srules.get(4).getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[1].getBounds().length];
			for (int j = 0; j < allenBoundsY.length; j++) {
				Bounds bx = new Bounds(
						(srules.get(4).getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[1].getBounds()[j].min, (srules.get(4)
								.getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[1].getBounds()[j].max);
				allenBoundsY[j] = bx;
			}
			AllenIntervalConstraint yAllenCon = new AllenIntervalConstraint((srules.get(4).getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[1].getTypes()[0], allenBoundsY);
			if((srules.get(4).getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[1].getBounds().length == 0)
				yAllenCon = (AllenIntervalConstraint)(srules.get(4).getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[1].clone();
			AllenIntervalConstraint xAllenCon = manipulationAreaDomain.getConvexifyBeforeAndAfter(); 
//					new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, AllenIntervalConstraint.Type.Meets, AllenIntervalConstraint.Type.Overlaps, AllenIntervalConstraint.Type.During, 
//					AllenIntervalConstraint.Type.OverlappedBy, AllenIntervalConstraint.Type.MetBy, AllenIntervalConstraint.Type.After);

			manipulationAreaTOtable = new RectangleConstraint(xAllenCon, yAllenCon);

			
		}		
		manipulationAreaTOtable.setFrom(manipulationAreaPrototype);
		manipulationAreaTOtable.setTo(supportFluent.getRectangularRegion());
		allConstraints.add(manipulationAreaTOtable);
						
		
		//we already the order of the rules, from manipulation domain description
		for (int i = 2; i < 4; i++) {
			
			//general rule
			Bounds[] allenBoundsX = new Bounds[(srules.get(i).getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[0].getBounds().length];
			for (int j = 0; j < allenBoundsX.length; j++) {
				Bounds bx = new Bounds(
						(srules.get(i).getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[0].getBounds()[j].min, (srules.get(i)
								.getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[0].getBounds()[j].max);
				allenBoundsX[j] = bx;
			}

			Bounds[] allenBoundsY = new Bounds[(srules.get(i).getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[1].getBounds().length];
			for (int j = 0; j < allenBoundsY.length; j++) {
				Bounds by = new Bounds(
						(srules.get(i).getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[1]
								.getBounds()[j].min, (srules.get(i).getBinaryRAConstraint())
								.getInternalAllenIntervalConstraints()[1].getBounds()[j].max);
				allenBoundsY[j] = by;
			}

			AllenIntervalConstraint xAllenCon = new AllenIntervalConstraint((srules.get(i).getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[0].getTypes()[0], allenBoundsX);
			AllenIntervalConstraint yAllenCon = new AllenIntervalConstraint(
					(srules.get(i).getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[1].getTypes()[0], allenBoundsY);


			//This part is for the Allen intervals do not have any bounds e.g., Equals
			if((srules.get(i).getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[0].getBounds().length == 0)
				xAllenCon = (AllenIntervalConstraint)(srules.get(i).getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[0].clone();
			if((srules.get(i).getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[1].getBounds().length == 0)
				yAllenCon = (AllenIntervalConstraint)(srules.get(i).getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[1].clone();
			
			
			//"placingArea", "manipulationArea",
			if(i == 2){
				RectangleConstraint placingAreaTOmanipulationArea = new RectangleConstraint(xAllenCon, yAllenCon);
				placingAreaTOmanipulationArea.setFrom(placingRecVar);
				placingAreaTOmanipulationArea.setTo(manipulationAreaPrototype);
				allConstraints.add(placingAreaTOmanipulationArea);
				
			}

			
			//"object", "placingArea", 
			else if(i == 3){
				RectangleConstraint objectToPlacingArea = new RectangleConstraint(xAllenCon, yAllenCon);
				objectToPlacingArea.setFrom(objectFleunt.getRectangularRegion());
				objectToPlacingArea.setTo(placingRecVar);
				allConstraints.add(objectToPlacingArea);
				
			}
			
		}
		
		
		boolean skip = false;
		//add at constraint in the fluent belongs to past, it will affect on how the occuoiedConstraintWork
		if(objectFleunt.getRectangularRegion().isUnbounded() ){
			if(((SimpleHybridPlanner)this.metaCS).getOldRectangularRegion()!= null){
				for (String str : ((SimpleHybridPlanner)this.metaCS).getOldRectangularRegion().keySet()) {
					if(objectFleunt.getName().compareTo(str) == 0){
						BoundingBox unboundBB = ((SimpleHybridPlanner)this.metaCS).getOldRectangularRegion().get(str);
						Bounds xLB = new Bounds(unboundBB.getxLB().min, unboundBB.getxLB().max);
						Bounds xUB = new Bounds(unboundBB.getxUB().min, unboundBB.getxUB().max);
						Bounds yLB = new Bounds(unboundBB.getyLB().min, unboundBB.getyLB().max);
						Bounds yUB = new Bounds(unboundBB.getyUB().min, unboundBB.getyUB().max);
						UnaryRectangleConstraint atObjInstance = new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.At, 
								xLB, xUB, yLB, yUB);
						atObjInstance.setFrom(objectFleunt);
						atObjInstance.setTo(objectFleunt);
						allConstraints.add(atObjInstance);						
					}
				}			
			}else skip = true;
		}
		
		if(!skip){
			for (Constraint con : allConstraints) {
	//			System.out.println("con: " + con);
				ret.addConstraint(con);
			}
		}
		
		
		return ret;
	}

	private String getParameter(Variable task) {
		
		String ret = "";
		String sym = ((Activity)task).getSymbolicVariable().getSymbols()[0];
		
		if(sym.contains("hold")){
			
			if(sym.lastIndexOf("_") == sym.indexOf("_")){
				ret = sym.substring(sym.indexOf("_"), sym.indexOf("(")).concat("_table1");
			}else{
				ret = sym.substring(sym.indexOf("_")+6, sym.indexOf("(")).concat("_table1");
			}
		}
		else if(sym.contains("sensing")){
			ret = sym.substring(sym.indexOf("_")+16, sym.indexOf("("));
		}
		else if(sym.contains("manipulationArea")){
			ret = sym.substring(sym.indexOf("_")+25, sym.indexOf("("));
		}
		else{
			String first_ = sym.substring(sym.indexOf("_")+1, sym.length());
			ret = first_.substring(0, first_.indexOf("_"));
		}
		
//		System.out.println("task: " + task + " -- " + ret);
		
		return ret;
	}

	@Override
	public ConstraintSolver getGroundSolver() {
		return ((SpatialFluentSolver)metaCS.getConstraintSolvers()[0]).getConstraintSolvers()[1];
	}
	
		
	public void updateTimeNow(long timeNow) {
		this.timeNow = timeNow;
	}
	
	public void activeHeuristic(boolean active){
		this.activeFreeArmHeuristic = active;
	}
}

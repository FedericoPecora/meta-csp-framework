package org.metacsp.meta.hybridPlanner;

import java.awt.Rectangle;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Level;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.VariableOrderingH;
import org.metacsp.framework.meta.MetaConstraint;
import org.metacsp.framework.meta.MetaVariable;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenInterval;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.spatial.rectangleAlgebra.BoundingBox;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangleConstraint;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangleConstraintSolver;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangularRegion;
import org.metacsp.multi.spatial.rectangleAlgebra.UnaryRectangleConstraint;
import org.metacsp.multi.spatioTemporal.SpatialFluent;
import org.metacsp.multi.spatioTemporal.SpatialFluentSolver;
import org.metacsp.spatial.reachability.ConfigurationVariable;
import org.metacsp.spatial.reachability.ReachabilityContraintSolver;
import org.metacsp.spatial.utility.SpatialAssertionalRelation;
import org.metacsp.spatial.utility.SpatialRule;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;

public class MetaInverseReachabilityConstraint extends MetaConstraint{

	private long origin = 0, horizon = 100000;
	private Vector<SpatialAssertionalRelation> sAssertionalRels = new Vector<SpatialAssertionalRelation>();
	public MetaInverseReachabilityConstraint(VariableOrderingH varOH, ValueOrderingH valOH) {
		super(varOH, valOH);
		// TODO Auto-generated constructor stub
	}

	
	public void setSpatialAssertionalRelations(Vector<SpatialAssertionalRelation> saRelations) {
		
		this.sAssertionalRels.clear();
		this.sAssertionalRels = saRelations;
		
	}
	
	@Override
	public ConstraintNetwork[] getMetaVariables() {
		
		Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
		ConstraintNetwork nw = new ConstraintNetwork(null);
		for (int i = 0; i < getGroundSolver().getVariables().length; i++) {
			SpatialFluent manFlunet = ((SpatialFluent)getGroundSolver().getVariables()[i]);
			AllenInterval intervalX = ((AllenInterval)manFlunet.getRectangularRegion().getInternalVariables()[0]);
			AllenInterval intervalY = ((AllenInterval)manFlunet.getRectangularRegion().getInternalVariables()[1]);
			if(!manFlunet.getActivity().getSymbolicVariable().getSymbols()[0].contains("manipulationArea"))//has to replaced with proper typed constraint checking no hard coding!!!!
				continue;
			if(isUnboundedBoundingBox(
					new Bounds(intervalX.getEST(), intervalX.getLST()), new Bounds(intervalX.getEET(), intervalX.getLET()), 
					new Bounds(intervalY.getEST(), intervalY.getLST()), new Bounds(intervalY.getEET(), intervalY.getLET()))){
//				System.out.println("SPATIALLY UNBOUND Manipulation Fluent: " + manFlunet);				
				nw.addVariable(manFlunet);
				break;		
			}
		}
		if(nw.getVariables().length != 0){
			ret.add(nw);
			return ret.toArray(new ConstraintNetwork[ret.size()]);
		}
		else{
			return (new ConstraintNetwork[0]);
		}
	}


	private boolean isUnboundedBoundingBox(Bounds xLB, Bounds xUB, Bounds yLB, Bounds yUB) {

		long horizon = ((ActivityNetworkSolver)((SpatialFluentSolver)this.metaCS.getConstraintSolvers()[0]).getConstraintSolvers()[1]).getHorizon();

		if( (xLB.min == 0 && xLB.max == horizon) && (xUB.min == 0&& xUB.max == horizon) &&
				(yLB.min == 0 && yLB.max == horizon) &&(yLB.min == 0 && yUB.max == horizon))
			return true;

		return false;
	}
	@Override
	public ConstraintNetwork[] getMetaValues(MetaVariable metaVariable) {

		Vector<SpatialRule> srules = new Vector<SpatialRule>();
		HashMap<ConfigurationVariable, SpatialFluent> confvarToSpatialFleunt = new HashMap<ConfigurationVariable, SpatialFluent>();
		Vector<ConstraintNetwork> ret = new Vector<ConstraintNetwork>();
		
		for (int i = 0; i < ((SpatialFluentSolver)this.metaCS.getConstraintSolvers()[0]).getVariables().length; i++) {
			SpatialFluent sf = (SpatialFluent)((SpatialFluentSolver)this.metaCS.getConstraintSolvers()[0]).getVariables()[i];
			confvarToSpatialFleunt.put(sf.getConfigurationVariable(), sf);

		}

		
		//constraint is like this from obj to manipulationArea
		SpatialFluent objecSpatialFleunt = null;
		SpatialFluent conflict = (SpatialFluent)metaVariable.getConstraintNetwork().getVariables()[0];
//		System.out.println("@@@@@@@@CONFLICT@@@@@@@@@@" + conflict);
		ReachabilityContraintSolver rchCs = (ReachabilityContraintSolver)((SpatialFluentSolver)this.metaCS.getConstraintSolvers()[0]).getConstraintSolvers()[2];
		for (int j = 0; j < rchCs.getConstraints().length; j++) {			
			if(((ConfigurationVariable)rchCs.getConstraints()[j].getScope()[1]).compareTo((conflict.getConfigurationVariable()))== 0){
				objecSpatialFleunt = confvarToSpatialFleunt.get(((ConfigurationVariable)rchCs.getConstraints()[j].getScope()[0]));
			}
		}
		
		getSpatialKnowledge(srules);
		for (int i = 1; i < srules.size(); i++) {
		
			Vector<Constraint> allConstraints = new Vector<Constraint>();
			RectangleConstraintSolver iterSolver = new RectangleConstraintSolver(origin, horizon);
			RectangularRegion objRec = (RectangularRegion) iterSolver.createVariable();
			objRec.setName("object");
			RectangularRegion manRec = (RectangularRegion) iterSolver.createVariable();
			manRec.setName("manipulationArea");
			
			//size
			Bounds[] sizeBounds = new Bounds[srules.get(0).getUnaryRAConstraint().getBounds().length];
			for (int j = 0; j < sizeBounds.length; j++) {
				Bounds bSize = new Bounds(
						srules.get(0).getUnaryRAConstraint().getBounds()[j].min,
						srules.get(0).getUnaryRAConstraint().getBounds()[j].max);
				sizeBounds[j] = bSize;
			}
			UnaryRectangleConstraint sizemanp = new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.Size, sizeBounds);
			sizemanp.setFrom(manRec);
			sizemanp.setTo(manRec);
			allConstraints.add(sizemanp);
			
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

			AllenIntervalConstraint xAllenCon = new AllenIntervalConstraint((srules.get(i).getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[0].getType(), allenBoundsX);
			AllenIntervalConstraint yAllenCon = new AllenIntervalConstraint(
					(srules.get(i).getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[1].getType(), allenBoundsY);


			//This part is for the Allen intervals do not have any bounds e.g., Equals
			if((srules.get(i).getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[0].getBounds().length == 0)
				xAllenCon = (AllenIntervalConstraint)(srules.get(i).getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[0].clone();
			if((srules.get(i).getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[1].getBounds().length == 0)
				yAllenCon = (AllenIntervalConstraint)(srules.get(i).getBinaryRAConstraint()).getInternalAllenIntervalConstraints()[1].clone();


			RectangleConstraint infrontof = new RectangleConstraint(xAllenCon, yAllenCon);
			infrontof.setFrom(manRec);
			infrontof.setTo(objRec);
			allConstraints.add(infrontof);
			
			
			//unary at constraint
			RectangularRegion objInstance = (RectangularRegion)iterSolver.createVariable();
			objInstance.setName(objecSpatialFleunt.getName());
			
			//check if is spatially bounded or not
			
			AllenInterval intervalX = ((AllenInterval)objecSpatialFleunt.getRectangularRegion().getInternalVariables()[0]);
			AllenInterval intervalY = ((AllenInterval)objecSpatialFleunt.getRectangularRegion().getInternalVariables()[1]);
			
			if(isUnboundedBoundingBox(
					new Bounds(intervalX.getEST(), intervalX.getLST()), new Bounds(intervalX.getEET(), intervalX.getLET()), 
					new Bounds(intervalY.getEST(), intervalY.getLST()), new Bounds(intervalY.getEET(), intervalY.getLET()))){
//				System.out.println("SPATIALLY UNBOUND Manipulation Fluent: " + objecSpatialFleunt);				

				
				for (String str : ((SimpleHybridPlanner)this.metaCS).getOldRectangularRegion().keySet()) {
					if(objecSpatialFleunt.getName().compareTo(str) == 0){
						BoundingBox unboundBB = ((SimpleHybridPlanner)this.metaCS).getOldRectangularRegion().get(str);
						UnaryRectangleConstraint atObjInstance = new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.At, 
								unboundBB.getxLB(), unboundBB.getxUB(), 
								unboundBB.getyLB(), unboundBB.getyUB());
						atObjInstance.setFrom(objInstance);
						atObjInstance.setTo(objInstance);
						allConstraints.add(atObjInstance);					}
				}

			}
			else{
				UnaryRectangleConstraint atObjInstance = new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.At, new Bounds(intervalX.getEST(), intervalX.getLST()), new Bounds(intervalX.getEET(), intervalX.getLET()), 
						new Bounds(intervalY.getEST(), intervalY.getLST()), new Bounds(intervalY.getEET(), intervalY.getLET()));
				atObjInstance.setFrom(objInstance);
				atObjInstance.setTo(objInstance);
				allConstraints.add(atObjInstance);
			}
			
			

			
			RectangularRegion manpInstance = (RectangularRegion)iterSolver.createVariable();
			manpInstance.setName(conflict.getName());
			UnaryRectangleConstraint atManipInstance = new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.At, new Bounds(0, APSPSolver.INF), new Bounds(0, APSPSolver.INF), new Bounds(0, APSPSolver.INF), new Bounds(0, APSPSolver.INF));
			atManipInstance.setFrom(manpInstance);
			atManipInstance.setTo(manpInstance);
			allConstraints.add(atManipInstance);
			
			
			//Assertional Rule
			RectangleConstraint manAssertion = new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.Equals), new AllenIntervalConstraint(AllenIntervalConstraint.Type.Equals));
			manAssertion.setFrom(manpInstance);
			manAssertion.setTo(manRec);
			allConstraints.add(manAssertion);

			RectangleConstraint objAssertion = new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.Equals), new AllenIntervalConstraint(AllenIntervalConstraint.Type.Equals));
			objAssertion.setFrom(objInstance);
			objAssertion.setTo(objRec);
			allConstraints.add(objAssertion);
			
			Constraint[] allConstraintsArray = allConstraints.toArray(new Constraint[allConstraints.size()]);
			iterSolver.addConstraints(allConstraintsArray);
			
//			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~" );
			HashMap<String, Rectangle> recs = new HashMap<String, Rectangle>(); 
			for (String str : iterSolver.extractAllBoundingBoxesFromSTPs().keySet()) {				
//					System.out.println(str + " --> " +iterSolver.extractAllBoundingBoxesFromSTPs().get(str).getAlmostCentreRectangle().getCenterX() + " -- "
//							+ iterSolver.extractAllBoundingBoxesFromSTPs().get(str).getAlmostCentreRectangle().getCenterY());
					recs.put( str,iterSolver.extractAllBoundingBoxesFromSTPs().get(str).getAlmostCentreRectangle());
			}   
			
			BufferedWriter finalPlot = null;
			String finalLayoutPlot = "";
			finalLayoutPlot =iterSolver.drawAlmostCentreRectangle(500, recs);	
			String PATH_FINAL_PLOT = "/home/iran/Desktop/manArea/";
			try{
				
				finalPlot = new BufferedWriter(new FileWriter(PATH_FINAL_PLOT + i+ "_final"+".dat", false));
				finalPlot.write(finalLayoutPlot);
				finalPlot.newLine();
				finalPlot.flush();
			}				
			catch (IOException ioe) {
				ioe.printStackTrace();
			}
			
			
			//remove those rectangle which is overlapped by nonmovable area (e.g., chair, table)
			AllenInterval x = (AllenInterval)manpInstance.getInternalVariables()[0];
			AllenInterval y = ((AllenInterval)manpInstance.getInternalVariables()[1]);
			BoundingBox manipulationBB = new BoundingBox(new Bounds(x.getEST(), x.getLST()), new Bounds(x.getEET(), x.getLET()), 
					new Bounds(y.getEST(), y.getLST()), new Bounds(y.getEET(), y.getLET()));
			Rectangle manipulationRec = manipulationBB.getAlmostCentreRectangle(); 
//			System.out.println("To be checked >" + manipulationRec);
			boolean overlapped = false;
			for (int j = 0; j < sAssertionalRels.size(); j++) {
				if(!sAssertionalRels.get(j).getOntologicalProp().isMovable()){
					BoundingBox bb = new BoundingBox(sAssertionalRels.get(j).getUnaryAtRectangleConstraint().getBounds()[0], 
							sAssertionalRels.get(j).getUnaryAtRectangleConstraint().getBounds()[1],
							sAssertionalRels.get(j).getUnaryAtRectangleConstraint().getBounds()[2],
							sAssertionalRels.get(j).getUnaryAtRectangleConstraint().getBounds()[3]);
					if(manipulationRec.intersects(bb.getAlmostCentreRectangle())){
						overlapped = true;
						break;
					}
				}
				
			}
			if(!overlapped){
//				System.out.println("selected: " + manipulationRec.getCenterX() + "--" + manipulationRec.getCenterY());
				ConstraintNetwork nw = new ConstraintNetwork(null);				
				UnaryRectangleConstraint atCon = new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.At, manipulationBB.getxLB(), manipulationBB.getxUB(),
						manipulationBB.getyLB(), manipulationBB.getyUB());
				atCon.setFrom(conflict.getRectangularRegion());
				atCon.setTo(conflict.getRectangularRegion());
				nw.addConstraint(atCon);
				ret.add(nw);
			}
				
//			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~" );
			
			
		}
		
		return ret.toArray(new ConstraintNetwork[ret.size()]);
	}

	

	
	private static void getSpatialKnowledge(Vector<SpatialRule> srules){

		Bounds manArea_size_x = new Bounds(60, 60);
		Bounds manArea_size_y = new Bounds(60, 60);
		
		long min_distance = 30;
		long max_distance = 35;
		


		SpatialRule r1 = new SpatialRule("manipulationArea", "manipulationArea", 
				new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.Size, manArea_size_x, manArea_size_y));
		srules.add(r1);

		////
		
		SpatialRule r2 = new SpatialRule("manipulationArea", "Object", 
				new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.After , new Bounds(min_distance,max_distance)),
				new AllenIntervalConstraint(AllenIntervalConstraint.Type.Contains, AllenIntervalConstraint.Type.Contains.getDefaultBounds()))
				);
		srules.add(r2);

		SpatialRule r3 = new SpatialRule("manipulationArea", "Object", 
				new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.Contains , AllenIntervalConstraint.Type.Contains.getDefaultBounds()),
				new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, new Bounds(min_distance,max_distance)))

				);
		srules.add(r3);

		SpatialRule r4 = new SpatialRule("manipulationArea", "Object", 
				new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, new Bounds(min_distance,max_distance)),
				new AllenIntervalConstraint(AllenIntervalConstraint.Type.Contains , AllenIntervalConstraint.Type.Contains.getDefaultBounds()))
				);
		srules.add(r4);

		SpatialRule r5 = new SpatialRule("manipulationArea", "Object", 
				new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.Contains , AllenIntervalConstraint.Type.Contains.getDefaultBounds()),
				new AllenIntervalConstraint(AllenIntervalConstraint.Type.After, new Bounds(min_distance,max_distance)))

				);
		srules.add(r5);


	}

	
	@Override
	public ConstraintNetwork[] getMetaValues(MetaVariable metaVariable,
			int initial_time) {
		// TODO Auto-generated method stub
		return getMetaValues(metaVariable);
	}

	@Override
	public void markResolvedSub(MetaVariable metaVariable,
			ConstraintNetwork metaValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void draw(ConstraintNetwork network) {
		// TODO Auto-generated method stub

	}

	@Override
	public ConstraintSolver getGroundSolver() {
		return ((SpatialFluentSolver)metaCS.getConstraintSolvers()[0]);
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "MetaInverseReachabilityConstraint";
	}

	@Override
	public String getEdgeLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEquivalent(Constraint c) {
		// TODO Auto-generated method stub
		return false;
	}





}

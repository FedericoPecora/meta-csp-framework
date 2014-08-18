package org.metacsp.meta.hybridPlanner;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Vector;

import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.VariableOrderingH;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.allenInterval.AllenInterval;
import org.metacsp.multi.spatial.rectangleAlgebra.BoundingBox;
import org.metacsp.multi.spatioTemporal.SpatialFluent;
import org.metacsp.time.Bounds;

public class MetaOccupiedTimesBasedConstraint extends MetaOccupiedConstraint{

	public MetaOccupiedTimesBasedConstraint(VariableOrderingH varOH, ValueOrderingH valOH) {
		super(varOH, valOH);

	}

	@Override
	public boolean isConflicting(Activity[] peak, HashMap<Activity, SpatialFluent> activityToFluent) {
		
		if(peak.length == 1) return false;
		for (int i = 0; i < peak.length; i++) {
			if(peak[i].getSymbolicVariable().getSymbols()[0].contains(((SimpleHybridPlanner)this.metaCS).getManipulationAreaEncoding())) return false;
		}
		
		
//		System.out.println("_________________________________________________");
//		for (int i = 0; i < peak.length; i++) {
//			System.out.println("peak: " + activityToFluent.get(peak[i]));
//		}
//		System.out.println("_________________________________________________");
//		System.out.println("==============================================================");
		
		Vector<SpatialFluent> unboundedsf = new Vector<SpatialFluent>();
		Vector<SpatialFluent> boundedsf = new Vector<SpatialFluent>();
		//here only the time bounded and unbounded is considered
		for (int i = 0; i < peak.length; i++) {
			if (((Activity)activityToFluent.get(peak[i]).getActivity()).getTemporalVariable().getEST() == ((Activity)activityToFluent.get(peak[i]).getActivity()).getTemporalVariable().getLST()){
				boundedsf.add(activityToFluent.get(peak[i]));
//				System.out.println("--isbounded--: " + activityToFluent.get(peak[i]));
			}
			else{
				unboundedsf.add(activityToFluent.get(peak[i]));
//				System.out.println("--isunbounded--: " + activityToFluent.get(peak[i]));
			}
//			if(isUnboundedBoundingBox(
//							new Bounds(((AllenInterval)activityToFluent.get(peak[i]).getRectangularRegion().getInternalVariables()[0]).getEST(), ((AllenInterval)activityToFluent.get(peak[i]).getRectangularRegion().getInternalVariables()[0]).getLST()),
//							new Bounds(((AllenInterval)activityToFluent.get(peak[i]).getRectangularRegion().getInternalVariables()[0]).getEET(), ((AllenInterval)activityToFluent.get(peak[i]).getRectangularRegion().getInternalVariables()[0]).getLET()), 
//							new Bounds(((AllenInterval)activityToFluent.get(peak[i]).getRectangularRegion().getInternalVariables()[1]).getEST(), ((AllenInterval)activityToFluent.get(peak[i]).getRectangularRegion().getInternalVariables()[1]).getLST()), 
//							new Bounds(((AllenInterval)activityToFluent.get(peak[i]).getRectangularRegion().getInternalVariables()[1]).getEET(), ((AllenInterval)activityToFluent.get(peak[i]).getRectangularRegion().getInternalVariables()[1]).getLET()))							
//							 //&& (((Activity)activityToFluent.get(peak[i]).getActivity()).getTemporalVariable().getEST() != ((Activity)activityToFluent.get(peak[i]).getActivity()).getTemporalVariable().getLST())
//							){//it was bouneded
//					System.out.println("--isunbounded--: " + activityToFluent.get(peak[i]));
//				unboundedsf.add(activityToFluent.get(peak[i]));
//			}
//			else{ 
////				if (((Activity)activityToFluent.get(peak[i]).getActivity()).getTemporalVariable().getEST() != ((Activity)activityToFluent.get(peak[i]).getActivity()).getTemporalVariable().getLST())
//					boundedsf.add(activityToFluent.get(peak[i]));
////			}
//
//				System.out.println("--isbounded--: " + activityToFluent.get(peak[i]));
//				
//			}
		}
		
//		System.out.println("==============================================================");
		if(unboundedsf.size() == 0 || boundedsf.size() == 0) return false;
		
		if(unboundedsf.lastElement().getName().compareTo(boundedsf.lastElement().getName()) == 0) return false; 
		
		
		
		if(unboundedsf.get(0).getRectangularRegion().isUnbounded()) {
			return false;
		}
		
//		System.out.println(unboundedsf.get(0));
//		System.out.println(boundedsf.get(0));
		
		
//		Rectangle  rec1 = null;
//		for (String str : ((SimpleHybridPlanner)this.metaCS).getOldRectangularRegion().keySet()) {
//			if(boundedsf.get(0).getRectangularRegion().getName().compareTo(str) == 0){
//				rec1 = ((SimpleHybridPlanner)this.metaCS).getOldRectangularRegion().get(str).getAlmostCentreRectangle();
////				System.out.println("rec2: "+ str + " -- "+((SimpleHybridPlanner)this.metaCS).getOldRectangularRegion().get(str).getAlmostCentreRectangle());
//			}
//		}
		
		
		Rectangle rec1 = new BoundingBox(
				new Bounds(((AllenInterval)boundedsf.get(0).getRectangularRegion().getInternalVariables()[0]).getEST(), ((AllenInterval)boundedsf.get(0).getRectangularRegion().getInternalVariables()[0]).getLST()),
				new Bounds(((AllenInterval)boundedsf.get(0).getRectangularRegion().getInternalVariables()[0]).getEET(), ((AllenInterval)boundedsf.get(0).getRectangularRegion().getInternalVariables()[0]).getLET()), 
				new Bounds(((AllenInterval)boundedsf.get(0).getRectangularRegion().getInternalVariables()[1]).getEST(), ((AllenInterval)boundedsf.get(0).getRectangularRegion().getInternalVariables()[1]).getLST()), 
				new Bounds(((AllenInterval)boundedsf.get(0).getRectangularRegion().getInternalVariables()[1]).getEET(), ((AllenInterval)boundedsf.get(0).getRectangularRegion().getInternalVariables()[1]).getLET())).getAlmostCentreRectangle();
//		System.out.println("rec1 -- boundedsf: " + rec1);

		
		Rectangle rec2 = new BoundingBox(
				new Bounds(((AllenInterval)unboundedsf.get(0).getRectangularRegion().getInternalVariables()[0]).getEST(), ((AllenInterval)unboundedsf.get(0).getRectangularRegion().getInternalVariables()[0]).getLST()),
				new Bounds(((AllenInterval)unboundedsf.get(0).getRectangularRegion().getInternalVariables()[0]).getEET(), ((AllenInterval)unboundedsf.get(0).getRectangularRegion().getInternalVariables()[0]).getLET()), 
				new Bounds(((AllenInterval)unboundedsf.get(0).getRectangularRegion().getInternalVariables()[1]).getEST(), ((AllenInterval)unboundedsf.get(0).getRectangularRegion().getInternalVariables()[1]).getLST()), 
				new Bounds(((AllenInterval)unboundedsf.get(0).getRectangularRegion().getInternalVariables()[1]).getEET(), ((AllenInterval)unboundedsf.get(0).getRectangularRegion().getInternalVariables()[1]).getLET())).getAlmostCentreRectangle();
//		System.out.println("rec1 -- unboundedsf: " + rec2);

		
		Rectangle r1new = new Rectangle(((int)rec1.getMinX()) - pad, ((int)rec1.getMinY()) - pad, (int)rec1.getWidth() + (2 * pad), (int)rec1.getHeight() + (2 * pad));
		Rectangle r2new = new Rectangle(((int)rec2.getMinX()) - pad, ((int)rec2.getMinY()) - pad, (int)rec2.getWidth() + (2 * pad), (int)rec2.getHeight() + (2 * pad));
		
//		System.out.println("=================================================");
//		System.out.println("rec1: " + rec1);
//		System.out.println("rec1new: " + r1new);
//		System.out.println("rec2: " + rec2);
//		System.out.println("rec2new: " + r2new);
//		System.out.println("=================================================");
		
		if(r1new.intersects(r2new)){
//			System.out.println("--These are conflicting--");
//			System.out.println("---------------------------");
			return true;
		}
		else{
//			System.out.println("These are not conflicting");
//			System.out.println("---------------------------");
		}
		
		

		
		return false;
	}


}

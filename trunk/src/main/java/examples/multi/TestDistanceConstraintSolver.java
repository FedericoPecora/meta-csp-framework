package examples.multi;

import multi.TCSP.DistanceConstraint;
import multi.TCSP.DistanceConstraintSolver;
import multi.TCSP.MultiTimePoint;
import time.APSPSolver;
import time.Bounds;
import framework.Constraint;
import framework.ConstraintNetwork;
import framework.multi.MultiConstraint;


public class TestDistanceConstraintSolver {
	
	public static void main(String args[]) {
		/*
		TCSPSolver metaSolver = new TCSPSolver(0, 100, 0);
		DistanceConstraintSolver groundSolver = (DistanceConstraintSolver)metaSolver.getConstraintSolvers()[0];
		*/
		DistanceConstraintSolver groundSolver = new DistanceConstraintSolver(0, 100);
		APSPSolver groundGroundSolver = (APSPSolver)groundSolver.getConstraintSolvers()[0];

		/*
		 * John travels to work either by car (30-40 min) or by bus (at least 60 min).
		 * Fred goes to work either by car (20-30 min) or in a carpool (40-50 min).
		 * Today John left home between 7:10 and 7:20 AM, and Fred arrived at work
		 * between 8:00 and 8:10 AM. John arrived at work 10-20 min after Fred left home.
		 */
		
		MultiTimePoint johnGoesToWork = (MultiTimePoint)groundSolver.createVariable();
		MultiTimePoint johnArrivesAtWork = (MultiTimePoint)groundSolver.createVariable();
		MultiTimePoint fredGoesToWork = (MultiTimePoint)groundSolver.createVariable();
		MultiTimePoint fredArrivesAtWork = (MultiTimePoint)groundSolver.createVariable();
		
		ConstraintNetwork.draw(groundSolver.getConstraintNetwork());
		
		DistanceConstraint johnTakesCarOrBus = new DistanceConstraint(new Bounds(30, 40), new Bounds(60, APSPSolver.INF));
		johnTakesCarOrBus.setFrom(johnGoesToWork);
		johnTakesCarOrBus.setTo(johnArrivesAtWork);
		
		/**/
		DistanceConstraint johnTakesCar = new DistanceConstraint(new Bounds(30, 40));
		johnTakesCar.setFrom(johnGoesToWork);
		johnTakesCar.setTo(johnArrivesAtWork);

		DistanceConstraint johnTakesBus = new DistanceConstraint(new Bounds(60, APSPSolver.INF));
		johnTakesBus.setFrom(johnGoesToWork);
		johnTakesBus.setTo(johnArrivesAtWork);
		/**/
		
		
		DistanceConstraint fredTakesCarOrCarpool = new DistanceConstraint(new Bounds(20, 30), new Bounds(40, 50));
		fredTakesCarOrCarpool.setFrom(fredGoesToWork);
		fredTakesCarOrCarpool.setTo(fredArrivesAtWork);

		/**/
		DistanceConstraint fredTakesCar = new DistanceConstraint(new Bounds(20, 30));
		fredTakesCar.setFrom(fredGoesToWork);
		fredTakesCar.setTo(fredArrivesAtWork);

		DistanceConstraint fredTakesCarpool = new DistanceConstraint(new Bounds(40, 50));
		fredTakesCarpool.setFrom(fredGoesToWork);
		fredTakesCarpool.setTo(fredArrivesAtWork);
		/**/
		
		DistanceConstraint johnLeaves = new DistanceConstraint(new Bounds(10, 20));
		johnLeaves.setFrom(groundSolver.getSource());
		johnLeaves.setTo(johnGoesToWork);
		
		DistanceConstraint fredArrives = new DistanceConstraint(new Bounds(60, 70));
		fredArrives.setFrom(groundSolver.getSource());
		fredArrives.setTo(fredArrivesAtWork);
		
		DistanceConstraint johnArrives = new DistanceConstraint(new Bounds(10, 20));
		johnArrives.setFrom(johnArrivesAtWork);
		johnArrives.setTo(fredGoesToWork);
		
		//groundSolver.addConstraints(new DistanceConstraint[] {johnTakesCarOrBus,fredTakesCarOrCarpool,johnLeaves,fredArrives,johnArrives});
		
		//groundSolver.addConstraint(johnTakesCarOrBus);
		//groundSolver.addConstraint(fredTakesCarOrCarpool);
		//THIS LABELING DOES NOT WORK
		groundSolver.addConstraint(johnTakesBus);
		groundSolver.addConstraint(fredTakesCarpool);
		
		groundSolver.addConstraint(johnLeaves);
		groundSolver.addConstraint(fredArrives);
		groundSolver.addConstraint(johnArrives);
				
		for (Constraint c : groundSolver.getConstraints()) {
			if (c instanceof MultiConstraint) {
				System.out.println(c + " (prop = " + ((MultiConstraint) c).propagateImmediately() + ")");
			}
		}
			
		System.out.println(groundSolver.getDescription());
		System.out.println(fredGoesToWork.getDescription());
	}

}

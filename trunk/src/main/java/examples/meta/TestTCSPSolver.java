package examples.meta;

import java.util.logging.Level;

import meta.TCSP.MostConstrainedFirstVarOH;
import meta.TCSP.TCSPLabeling;
import meta.TCSP.TCSPSolver;
import meta.TCSP.WidestIntervalFirstValOH;
import multi.TCSP.DistanceConstraint;
import multi.TCSP.DistanceConstraintSolver;
import multi.TCSP.MultiTimePoint;
import time.APSPSolver;
import time.Bounds;
import utility.logging.LoggerNotDefined;
import utility.logging.MetaCSPLogging;
import framework.ConstraintNetwork;
import framework.ValueOrderingH;
import framework.VariableOrderingH;


public class TestTCSPSolver {
	
	public static void main(String args[]) {
		
		TCSPSolver metaSolver = new TCSPSolver(0, 100, 0);
		DistanceConstraintSolver groundSolver = (DistanceConstraintSolver)metaSolver.getConstraintSolvers()[0];
		
//		APSPSolver groundGroundSolver = (APSPSolver)groundSolver.getConstraintSolvers()[0];
		
		MetaCSPLogging.setLevel(Level.FINEST);
		try {
			MetaCSPLogging.setLevel(metaSolver.getClass(), Level.FINEST);
		} catch (LoggerNotDefined e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		
		DistanceConstraint fredTakesCarOrCarpool = new DistanceConstraint(new Bounds(40, 50), new Bounds(20, 30));
		fredTakesCarOrCarpool.setFrom(fredGoesToWork);
		fredTakesCarOrCarpool.setTo(fredArrivesAtWork);
		
		DistanceConstraint johnLeaves = new DistanceConstraint(new Bounds(10, 20));
		johnLeaves.setFrom(groundSolver.getSource());
		johnLeaves.setTo(johnGoesToWork);
		
		DistanceConstraint fredArrives = new DistanceConstraint(new Bounds(60, 70));
		fredArrives.setFrom(groundSolver.getSource());
		fredArrives.setTo(fredArrivesAtWork);
		
		DistanceConstraint johnArrives = new DistanceConstraint(new Bounds(10, 20));
		johnArrives.setFrom(johnArrivesAtWork);
		johnArrives.setTo(fredGoesToWork);
		
		groundSolver.addConstraints(new DistanceConstraint[] {johnTakesCarOrBus,fredTakesCarOrCarpool,johnLeaves,fredArrives,johnArrives});
		
		VariableOrderingH varOH = new MostConstrainedFirstVarOH();
		
		ValueOrderingH valOH = new WidestIntervalFirstValOH();

		TCSPLabeling metaCons = new TCSPLabeling(varOH, valOH);
		metaSolver.addMetaConstraint(metaCons);
		
		System.out.println("Solved? " + metaSolver.backtrack());
		
//		for (Variable tp : groundGroundSolver.getVariables()) {
//			System.out.println("TP " + tp);
//		}
		
		//groundGroundSolver.draw();
		metaSolver.draw();
		
		System.out.println(metaSolver.getDescription());
		System.out.println(metaCons.getDescription());
		
		//TU Delft
		//Leon Planken - see his website for temporal reasoning survey (2007)
		//See his JAIR 2012 for ICAPS work
		
	}

}
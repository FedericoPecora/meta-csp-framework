package org.metacsp.examples;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.metacsp.booleanSAT.BooleanConstraint;
import org.metacsp.booleanSAT.BooleanSatisfiabilitySolver;
import org.metacsp.booleanSAT.BooleanVariable;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.utility.logging.MetaCSPLogging;

public class TestBooleanSatisfiabilitySolverUNSAT {
	
	public static void main(String[] args) {
		
		BooleanSatisfiabilitySolver solver = new BooleanSatisfiabilitySolver(10, 10);
		MetaCSPLogging.setLevel(BooleanSatisfiabilitySolver.class, Level.FINEST);
		Logger logger = MetaCSPLogging.getLogger(TestBooleanSatisfiabilitySolverUNSAT.class);
		
		ConstraintNetwork.draw(solver.getConstraintNetwork());
		
		//[(x1)] Temporal Clause (= Activity)
		//[(x2)] Temporal Clause (= Activity)
		
		//[(~x1 v x2)] Temporal Clause
		// =
		// {x1, x2}    Predicates
		// +
		//[[]      []] AllenInterval
		// +
		// . -----> .  Pair of TPs
		
		//(~x1 v x2) ^ (x1 v x2) ^ (x1 v ~x2) ^ (~x1 v ~x2)
		BooleanVariable[] vars = (BooleanVariable[])solver.createVariables(2);
		BooleanConstraint clause1 = new BooleanConstraint(new BooleanVariable[] {vars[0],vars[1]}, new boolean[] {false,true});
		BooleanConstraint clause2 = new BooleanConstraint(new BooleanVariable[] {vars[0],vars[1]}, new boolean[] {true,true});
		BooleanConstraint clause3 = new BooleanConstraint(new BooleanVariable[] {vars[0],vars[1]}, new boolean[] {true,false});
		BooleanConstraint clause4 = new BooleanConstraint(new BooleanVariable[] {vars[0],vars[1]}, new boolean[] {false,false});
		
		//This will succeed
		logger.info("SAT? "+solver.addConstraints(new BooleanConstraint[] {clause1,clause2,clause3}));
		logger.info(Arrays.toString(solver.getVariables()));
		
		try { Thread.sleep(2000); }
		catch (InterruptedException e) { e.printStackTrace(); }

		//This will fail
		logger.info("SAT? "+solver.addConstraint(clause4));
		logger.info(Arrays.toString(solver.getVariables()));

		try { Thread.sleep(40000); }
		catch (InterruptedException e) { e.printStackTrace(); }

		solver.removeConstraint(clause3);
		logger.info(Arrays.toString(solver.getVariables()));
		
		try { Thread.sleep(2000); }
		catch (InterruptedException e) { e.printStackTrace(); }
		
		//(~x1 v x3) ^ (x2 v ~x3)
		BooleanVariable[] newVars = (BooleanVariable[])solver.createVariables(1);
		BooleanConstraint clause5 = new BooleanConstraint(new BooleanVariable[] {vars[0],newVars[0]}, new boolean[] {false,false});
		BooleanConstraint clause6 = new BooleanConstraint(new BooleanVariable[] {vars[1],newVars[0]}, new boolean[] {true,false});

		//This will succeed
		logger.info("SAT? "+solver.addConstraints(new BooleanConstraint[] {clause5,clause6}));
		logger.info(Arrays.toString(solver.getVariables()));

		try { Thread.sleep(2000); }
		catch (InterruptedException e) { e.printStackTrace(); }

		solver.removeConstraints(new BooleanConstraint[] {clause5,clause6});
		logger.info(Arrays.toString(solver.getVariables()));
		
		try { Thread.sleep(2000); }
		catch (InterruptedException e) { e.printStackTrace(); }
		
		solver.removeVariable(newVars[0]);
		logger.info(Arrays.toString(solver.getVariables()));
		
		logger.info("Chosen value for " + vars[0] + " is " + vars[0].getDomain().chooseValue("model1"));
		
	}

}

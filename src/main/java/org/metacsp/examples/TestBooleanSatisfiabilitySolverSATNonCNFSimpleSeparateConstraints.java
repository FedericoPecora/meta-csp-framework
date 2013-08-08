package org.metacsp.examples;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.metacsp.booleanSAT.BooleanConstraint;
import org.metacsp.booleanSAT.BooleanSatisfiabilitySolver;
import org.metacsp.booleanSAT.BooleanVariable;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.utility.logging.MetaCSPLogging;

public class TestBooleanSatisfiabilitySolverSATNonCNFSimpleSeparateConstraints {
	
	public static void main(String[] args) {
		
		BooleanSatisfiabilitySolver solver = new BooleanSatisfiabilitySolver(10, 10);
		MetaCSPLogging.setLevel(BooleanSatisfiabilitySolver.class, Level.FINEST);
		MetaCSPLogging.setLevel(BooleanConstraint.class, Level.FINEST);
		Logger logger = MetaCSPLogging.getLogger(TestBooleanSatisfiabilitySolverSATNonCNFSimpleSeparateConstraints.class);
		
		ConstraintNetwork.draw(solver.getConstraintNetwork());

		BooleanVariable[] vars = (BooleanVariable[])solver.createVariables(4);
	
//		String wff = "((((x1 ^ x2) ^ (x2 v (~x3 ^ x4))) ^ (~x1 v x3)) ^ (x2 v (~x3 ^ ~x4)))";
//		BooleanConstraint[] cons = BooleanConstraint.createBooleanConstraintsNew(new BooleanVariable[] {vars[0],vars[1],vars[2],vars[3]}, wff);
//		logger.info("SAT? "+solver.addConstraints(cons));
		
		//EQUIVALENT TO THE ABOVE
		String wff = "((x1 ^ x2) ^ (~x1 v x3))";
		BooleanConstraint[] cons = BooleanConstraint.createBooleanConstraints(new BooleanVariable[] {vars[0],vars[1],vars[2]}, wff);
		logger.info("SAT? "+solver.addConstraints(cons));
		
		wff = "(x1 v (~x2 ^ x3))";
		BooleanConstraint[] cons1 = BooleanConstraint.createBooleanConstraints(new BooleanVariable[] {vars[1],vars[2],vars[3]}, wff);
		logger.info("SAT? "+solver.addConstraints(cons1));
		
		wff = "(x1 v (~x2 ^ ~x3))";
		BooleanConstraint[] cons2 = BooleanConstraint.createBooleanConstraints(new BooleanVariable[] {vars[1],vars[2],vars[3]}, wff);
		logger.info("SAT? "+solver.addConstraints(cons2));
		
		logger.info(Arrays.toString(vars));
	}

}

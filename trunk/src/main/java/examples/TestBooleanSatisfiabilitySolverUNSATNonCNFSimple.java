package examples;

import java.util.logging.Level;
import java.util.logging.Logger;

import utility.logging.MetaCSPLogging;
import java.util.Arrays;
import framework.ConstraintNetwork;
import framework.ConstraintSolver;
import booleanSAT.BooleanConstraint;
import booleanSAT.BooleanSatisfiabilitySolver;
import booleanSAT.BooleanVariable;

public class TestBooleanSatisfiabilitySolverUNSATNonCNFSimple {
	
	public static void main(String[] args) {
		
		BooleanSatisfiabilitySolver solver = new BooleanSatisfiabilitySolver(10, 10);
		MetaCSPLogging.setLevel(BooleanSatisfiabilitySolver.class, Level.FINEST);
		MetaCSPLogging.setLevel(BooleanConstraint.class, Level.FINEST);
		Logger logger = MetaCSPLogging.getLogger(TestBooleanSatisfiabilitySolverUNSATNonCNFSimple.class);
		
		ConstraintNetwork.draw(solver.getConstraintNetwork());

		BooleanVariable[] vars = (BooleanVariable[])solver.createVariables(1);
		//String wff = "(x4) ^ ((x1 ^ x2) v (x2 v !x3 ^ x4)) ^ ((~x1 v x3) v (x2 ^ ~x3 ^ ~x4))";
		//String wff = "(x1 ^ x2) ^ (x2 v !x3 ^ x4) ^ (~x1 v x3) ^ (x2 v ~x3 ^ ~x4)";
		//String wff = "(x1 <-> ((x2 v ~x3) ^ ~(x2 v ~x3))) ^ (x4 v ~x4)";
		String wff = "(x1 ^ ~x1)";
		BooleanConstraint[] cons = BooleanConstraint.createBooleanConstraints(vars, wff);

		logger.info("SAT? "+solver.addConstraints(cons));
		logger.info(Arrays.toString(vars));
		
	}

}

package examples;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import utility.logging.MetaCSPLogging;
import framework.ConstraintNetwork;
import booleanSAT.BooleanConstraint;
import booleanSAT.BooleanSatisfiabilitySolver;
import booleanSAT.BooleanVariable;

public class TestBooleanSatisfiabilitySolverSATNonCNFSimple {
	
	public static void main(String[] args) {
		
		BooleanSatisfiabilitySolver solver = new BooleanSatisfiabilitySolver(10, 10);
		MetaCSPLogging.setLevel(BooleanSatisfiabilitySolver.class, Level.FINEST);
		MetaCSPLogging.setLevel(BooleanConstraint.class, Level.FINEST);
		Logger logger = MetaCSPLogging.getLogger(TestBooleanSatisfiabilitySolverSATNonCNFSimple.class);
		
		ConstraintNetwork.draw(solver.getConstraintNetwork());

		BooleanVariable[] vars = (BooleanVariable[])solver.createVariables(4);
		String wff = "(x1 ^ x2) ^ (x2 v ~x3 ^ x4) ^ (~x1 v x3) ^ (x2 v ~x3 ^ ~x4)";
		BooleanConstraint[] cons = BooleanConstraint.createBooleanConstraints(vars, wff);

		logger.info("SAT? "+solver.addConstraints(cons));
		logger.info(Arrays.toString(vars));
		
	}

}

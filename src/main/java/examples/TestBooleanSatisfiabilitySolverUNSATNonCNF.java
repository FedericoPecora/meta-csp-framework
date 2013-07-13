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

public class TestBooleanSatisfiabilitySolverUNSATNonCNF {
	
	public static void main(String[] args) {
		
		BooleanSatisfiabilitySolver solver = new BooleanSatisfiabilitySolver(10, 10);
		MetaCSPLogging.setLevel(BooleanSatisfiabilitySolver.class, Level.FINEST);
		MetaCSPLogging.setLevel(BooleanConstraint.class, Level.FINEST);
		Logger logger = MetaCSPLogging.getLogger(TestBooleanSatisfiabilitySolverUNSATNonCNF.class);
		
		ConstraintNetwork.draw(solver.getConstraintNetwork());

		BooleanVariable[] vars = (BooleanVariable[])solver.createVariables(4);
		String wff = "(x1 <-> ((x2 v ~x3) ^ ~(x2 v ~x3))) ^ (x3 v ~x4) ^ (~x2)";
		BooleanConstraint[] cons = BooleanConstraint.createBooleanConstraints(vars, wff);

		logger.info("SAT? "+solver.addConstraints(cons));
		logger.info(Arrays.toString(vars));
		
	}

}

package examples;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import utility.logging.MetaCSPLogging;
import framework.ConstraintNetwork;
import framework.ConstraintSolver;
import booleanSAT.BooleanConstraint;
import booleanSAT.BooleanSatisfiabilitySolver;
import booleanSAT.BooleanVariable;

public class TestBooleanSatisfiabilitySolverSATAlternative2 {
	
	public static void main(String[] args) {
		
		BooleanSatisfiabilitySolver solver = new BooleanSatisfiabilitySolver(10, 10);
		MetaCSPLogging.setLevel(BooleanSatisfiabilitySolver.class, Level.FINEST);
		Logger logger = MetaCSPLogging.getLogger(TestBooleanSatisfiabilitySolverSATAlternative2.class);
		
		ConstraintNetwork.draw(solver.getConstraintNetwork());
		
		//(~x1 v x2 v x4) ^ (x1 v ~x2 v x3) ^ (x1 v x2 v ~x4) ^ (x2 v x3) ^
		//(x1 v x2 v ~x3 v x4) ^ (x1 v ~x2 v ~x3 v x4) ^ (x1 v ~x2 v ~x3 v ~x4) ^
		//(~x1 v ~x2 v ~x3 v x4) ^ (~x1 v x2 v ~x3 v ~x4) ^ (~x1 v ~x2 v ~x3 v ~x4)
		BooleanVariable[] vars = (BooleanVariable[])solver.createVariables(4);
		String cnf = "(~x1 v x2 v x4) ^ (x1 v ~x2 v x3) ^ (x1 v x2 v ~x4) ^ (x2 v x3) ^ (x1 v x2 v ~x3 v x4) ^ (x1 v ~x2 v ~x3 v x4) ^ (x1 v ~x2 v ~x3 v ~x4) ^ (~x1 v ~x2 v ~x3 v x4) ^ (~x1 v x2 v ~x3 v ~x4) ^ (~x1 v ~x2 v ~x3 v ~x4)";
		BooleanConstraint[] cons = BooleanConstraint.createBooleanConstraints(vars, cnf);
		logger.info("SAT? "+solver.addConstraints(cons));
		logger.info(Arrays.toString(vars));
		
	}

}

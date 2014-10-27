package org.metacsp.examples;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.metacsp.booleanSAT.BooleanConstraint;
import org.metacsp.booleanSAT.BooleanSatisfiabilitySolver;
import org.metacsp.booleanSAT.BooleanVariable;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.utility.logging.MetaCSPLogging;

public class TestBooleanSatisfiabilitySolverSATAlternative1 {
	
	public static void main(String[] args) {
		
		BooleanSatisfiabilitySolver solver = new BooleanSatisfiabilitySolver(10, 10);
		MetaCSPLogging.setLevel(Level.FINEST);
		Logger logger = MetaCSPLogging.getLogger(TestBooleanSatisfiabilitySolverSATAlternative1.class);
		
		ConstraintNetwork.draw(solver.getConstraintNetwork());
		
		//(~x1 v x2 v x4) ^ (x1 v ~x2 v x3) ^ (x1 v x2 v ~x4) ^ (x2 v x3) ^
		//(x1 v x2 v ~x3 v x4) ^ (x1 v ~x2 v ~x3 v x4) ^ (x1 v ~x2 v ~x3 v ~x4) ^
		//(~x1 v ~x2 v ~x3 v x4) ^ (~x1 v x2 v ~x3 v ~x4) ^ (~x1 v ~x2 v ~x3 v ~x4)
		BooleanVariable[] vars = (BooleanVariable[])solver.createVariables(4);
		BooleanConstraint clause1 = new BooleanConstraint(new BooleanVariable[] {vars[0],vars[1],vars[3]}, new boolean[] {false,true,true});
		BooleanConstraint clause2 = new BooleanConstraint(new BooleanVariable[] {vars[0],vars[1],vars[2]}, new boolean[] {true,false,true});
		BooleanConstraint clause3 = new BooleanConstraint(new BooleanVariable[] {vars[0],vars[1],vars[3]}, new boolean[] {true,true,false});
		BooleanConstraint clause4 = new BooleanConstraint(new BooleanVariable[] {vars[1],vars[2]}, new boolean[] {true,true});

		BooleanConstraint clause5 = new BooleanConstraint(new BooleanVariable[] {vars[0],vars[1],vars[2],vars[3]}, new boolean[] {true,true,false,true});
		BooleanConstraint clause6 = new BooleanConstraint(new BooleanVariable[] {vars[0],vars[1],vars[2],vars[3]}, new boolean[] {true,false,false,true});
		BooleanConstraint clause7 = new BooleanConstraint(new BooleanVariable[] {vars[0],vars[1],vars[2],vars[3]}, new boolean[] {true,false,false,false});

		BooleanConstraint clause8 = new BooleanConstraint(new BooleanVariable[] {vars[0],vars[1],vars[2],vars[3]}, new boolean[] {false,false,false,true});
		BooleanConstraint clause9 = new BooleanConstraint(new BooleanVariable[] {vars[0],vars[1],vars[2],vars[3]}, new boolean[] {false,true,false,false});
		BooleanConstraint clause10 = new BooleanConstraint(new BooleanVariable[] {vars[0],vars[1],vars[2],vars[3]}, new boolean[] {false,false,false,false});

		logger.info("SAT? "+solver.addConstraints(new BooleanConstraint[] {clause1,clause2,clause3,clause4,clause5,clause6,clause7,clause8,clause9,clause10}));
		logger.info(Arrays.toString(vars));
		
		logger.info("Chosen value for " + vars[1] + " is " + (Boolean)vars[1].getDomain().chooseValue("model1"));
		
	}

}

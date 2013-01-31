package examples;

import spatial.RCC.RCCConstraint;
import spatial.RCC.RCCConstraintSolver;
import spatial.RCC.Region;
import framework.ConstraintNetwork;
import framework.Variable;


public class TestRCCConstraintNetworkSolver {

	public static void main(String[] args) {
		
		RCCConstraintSolver solver = new RCCConstraintSolver(); 
		Variable[] vars = solver.createVariables(3);
		
		Region re0 = (Region)vars[0];
		Region re1 = (Region)vars[1];
		Region re2 = (Region)vars[2];
		
		RCCConstraint con0 = new RCCConstraint(RCCConstraint.Type.NTPP, RCCConstraint.Type.PO);
		con0.setFrom(re0);
		con0.setTo(re1);
		System.out.println("Adding constraint " + con0 + ": " + solver.addConstraint(con0));
		
		RCCConstraint con1 = new RCCConstraint(RCCConstraint.Type.DC);
		con1.setFrom(re1);
		con1.setTo(re2);
		System.out.println("Adding constraint " + con1 + ": " + solver.addConstraint(con1));
		
		RCCConstraint con2 = new RCCConstraint(RCCConstraint.Type.NTPPI);
		con2.setFrom(re2);
		con2.setTo(re0);
		System.out.println("Adding constraint " + con2 + ": " + solver.addConstraint(con2));
		
//		RCCConstraint[] allConstraints = {con0, con1, con2};
//		if (!solver.addConstraints(allConstraints)) { 
//			System.out.println("Failed to add constraints!");
//			System.exit(0);
//		}
		
		ConstraintNetwork.draw(solver.getConstraintNetwork());
	}
	

}




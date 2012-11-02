package examples;

import framework.ConstraintNetwork;
import fuzzyAllenInterval.FuzzyAllenIntervalConstraint;
import fuzzyAllenInterval.FuzzyAllenIntervalNetworkSolver;
import fuzzyAllenInterval.SimpleAllenInterval;

public class TestFuzzyAllenIntervalNetworkSolverSyroco {
	
	public static void main(String[] args) {
		FuzzyAllenIntervalNetworkSolver solver = new FuzzyAllenIntervalNetworkSolver();
		
		//dirty
		SimpleAllenInterval sai0 = (SimpleAllenInterval)solver.createVariable("aComponent");
		
		//empty
		SimpleAllenInterval sai1 = (SimpleAllenInterval)solver.createVariable("aComponent");
		
		//hasfood
		SimpleAllenInterval sai2 = (SimpleAllenInterval)solver.createVariable("aComponent");
		
		//dirty Equals empty
		FuzzyAllenIntervalConstraint con0 = new FuzzyAllenIntervalConstraint(FuzzyAllenIntervalConstraint.Type.Equals);
		con0.setFrom(sai0);
		con0.setTo(sai1);

		//hasfood before empty
		FuzzyAllenIntervalConstraint con1 = new FuzzyAllenIntervalConstraint(FuzzyAllenIntervalConstraint.Type.Before, FuzzyAllenIntervalConstraint.Type.Meets);
		con1.setFrom(sai2);
		con1.setTo(sai1);
		
		//dirty After hasfood
		FuzzyAllenIntervalConstraint con2 = new FuzzyAllenIntervalConstraint(FuzzyAllenIntervalConstraint.Type.After);
		con2.setFrom(sai0);
		con2.setTo(sai2);
		
		
		ConstraintNetwork.draw(solver.getConstraintNetwork());
		
//		solver.addConstraint(con0);
//		solver.addConstraint(con1);
//		solver.addConstraint(con2);
		
		FuzzyAllenIntervalConstraint[] allConstraints = {con0,con1,con2};
		if (!solver.addConstraints(allConstraints)) { 
			System.out.println("Failed to add constraints!");
			System.exit(0);
		}
		
		
		System.out.println("Poss: " + solver.getPosibilityDegree());

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		solver.removeConstraint(con1);
		con1 = new FuzzyAllenIntervalConstraint(FuzzyAllenIntervalConstraint.Type.Meets);
		con1.setFrom(sai2);
		con1.setTo(sai1);
		
		solver.addConstraint(con1);

		System.out.println("Poss: " + solver.getPosibilityDegree());
		
		System.out.println(solver.getDescription());
		System.out.println(sai1.getDescription());
		
	}
	

}

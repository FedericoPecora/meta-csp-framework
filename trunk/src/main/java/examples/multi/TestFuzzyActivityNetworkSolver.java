package examples.multi;

import multi.fuzzyActivity.FuzzyActivity;
import multi.fuzzyActivity.FuzzyActivityNetworkSolver;
import symbols.SymbolicValueConstraint;
import symbols.fuzzySymbols.FuzzySymbolicVariableConstraintSolver;
import framework.Constraint;
import framework.ConstraintNetwork;
import fuzzyAllenInterval.FuzzyAllenIntervalConstraint;
import fuzzyAllenInterval.FuzzyAllenIntervalNetworkSolver;

public class TestFuzzyActivityNetworkSolver {
	
	
	public static void main(String[] args) {
		FuzzyActivityNetworkSolver solver = new FuzzyActivityNetworkSolver();

		FuzzyActivity act1 = (FuzzyActivity)solver.createVariable();
		act1.setDomain(new String[] {"A", "B", "C"}, new double[] {0.1,0.4,0.8});
		
		FuzzyActivity act2 = (FuzzyActivity)solver.createVariable();
		act2.setDomain(new String[] {"A", "B", "C"}, new double[] {0.8,0.2,0.7});

		ConstraintNetwork.draw(solver.getConstraintNetwork());
		
		SymbolicValueConstraint con1 = new SymbolicValueConstraint(SymbolicValueConstraint.Type.EQUALS);
		con1.setFrom(act1);
		con1.setTo(act2);
		//solver.addConstraint(con1);
		
		FuzzyAllenIntervalConstraint con2 = new FuzzyAllenIntervalConstraint(FuzzyAllenIntervalConstraint.Type.Before);
		con2.setFrom(act1);
		con2.setTo(act2);
		//solver.addConstraint(con2);
		
		Constraint[] cons = new Constraint[]{con1,con2};
		solver.addConstraints(cons);
				
		System.out.println(solver.getDescription());
		System.out.println(act1.getDescription());

		System.out.println("---------------------------------");
		System.out.println("Temporal Possibility:" + solver.getTemporalConsistency());
		System.out.println("Value Possibility:" + solver.getValueConsistency());
		System.out.println("---------------------------------");

		boolean add = false;
		
		while (true) {
			for (int i = 0; i < cons.length; i++) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (add) {
					solver.addConstraint(cons[i]);
					System.out.println("Added " + cons[i].getClass().getSimpleName() + " type");
				}
				else {
					solver.removeConstraint(cons[i]);
					System.out.println("Removed " + cons[i].getClass().getSimpleName() + " type");
				}
				System.out.println("Temporal Possibility:" + solver.getTemporalConsistency());
				System.out.println("Value Possibility:" + solver.getValueConsistency());
				System.out.println("---------------------------------");
			}
			add = !add;
		}
		

		
	}

}

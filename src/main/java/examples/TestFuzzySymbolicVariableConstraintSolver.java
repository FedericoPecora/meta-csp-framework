package examples.multi;

import symbols.SymbolicValueConstraint;
import symbols.SymbolicVariableNetwork;
import symbols.fuzzySymbols.FuzzySymbolicVariable;
import symbols.fuzzySymbols.FuzzySymbolicVariableConstraintSolver;
import framework.Variable;

public class TestFuzzySymbolicVariableConstraintSolver {
	
	public static void main(String[] args) {
		FuzzySymbolicVariableConstraintSolver solver = new FuzzySymbolicVariableConstraintSolver();
		Variable[] vars = solver.createVariables(3);
		
		FuzzySymbolicVariable var0 = (FuzzySymbolicVariable)vars[0];
		var0.setDomain(new String[] {"A", "B", "C"}, new double[] {0.1,0.8,1.0});
		
		FuzzySymbolicVariable var1 = (FuzzySymbolicVariable)vars[1];
		var1.setDomain(new String[] {"A", "B", "C"}, new double[] {0.5,0.1,0.2});

		FuzzySymbolicVariable var2 = (FuzzySymbolicVariable )vars[2];
		var2.setDomain(new String[] {"A", "B", "C"}, new double[] {0.9,0.3,0.1});

		SymbolicVariableNetwork.draw(solver.getConstraintNetwork());
		
		SymbolicValueConstraint con1 = new SymbolicValueConstraint(SymbolicValueConstraint.Type.EQUALS);
		con1.setFrom(var0);
		con1.setTo(var1);
//		solver.addConstraint(con1);
		
		SymbolicValueConstraint con2 = new SymbolicValueConstraint(SymbolicValueConstraint.Type.EQUALS);
		con2.setFrom(var1);
		con2.setTo(var2);
//		solver.addConstraint(con2);

		SymbolicValueConstraint[] cons = new SymbolicValueConstraint[] {con1,con2};
		
		while (true) {
			for (SymbolicValueConstraint con : cons) {
				solver.addConstraint(con);
				System.out.println("Poss: " + solver.getUpperBound());
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			for (SymbolicValueConstraint con : cons) {
				solver.removeConstraint(con);
				System.out.println("Poss: " + solver.getUpperBound());
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		

				
//		/***/
//		System.out.println("---------------------");
//
//		SymbolicValueConstraint con3 = new SymbolicValueConstraint(solver,SymbolicValueConstraint.Type.UNARYEQUALS);
//		con3.setFrom(var2);
//		con3.setTo(var2);
//		con3.setUnaryValue("B");
//		solver.addConstraint(con3);
//
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		System.out.println(var0);
//		System.out.println(var1);
//		System.out.println(var2);

	}

}

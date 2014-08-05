import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.multi.symbols.SymbolicValueConstraint;
import org.metacsp.multi.symbols.SymbolicVariable;
import org.metacsp.multi.symbols.SymbolicVariableConstraintSolver;
import org.metacsp.multi.symbols.SymbolicValueConstraint.Type;

public class TestingMetaCSP {

	public static void main(String[] args) {
		
		SymbolicVariableConstraintSolver solver = new SymbolicVariableConstraintSolver(new String[] {"Hello", "world"},  100);
		SymbolicVariable var1 = (SymbolicVariable)solver.createVariable();
		SymbolicVariable var2 = (SymbolicVariable)solver.createVariable();
		
		SymbolicValueConstraint con1 = new SymbolicValueConstraint(Type.UNARYDIFFERENT);
		con1.setUnaryValue(new boolean[] {true,false});
		con1.setFrom(var1);
		con1.setTo(var1);
		solver.addConstraint(con1);

		SymbolicValueConstraint con2 = new SymbolicValueConstraint(Type.UNARYEQUALS);
		con2.setUnaryValue(new boolean[] {true,false});
		con2.setFrom(var2);
		con2.setTo(var2);
		solver.addConstraint(con2);
		
		SymbolicValueConstraint con3 = new SymbolicValueConstraint(Type.DIFFERENT);
		con3.setFrom(var2);
		con3.setTo(var1);
		if (!solver.addConstraint(con3)) System.out.println("Something went wrong!");
		
		ConstraintNetwork.draw(solver.getConstraintNetwork());
		System.out.println("Var1: " + var1);
		System.out.println("Var2: " + var2);
		
	}
}

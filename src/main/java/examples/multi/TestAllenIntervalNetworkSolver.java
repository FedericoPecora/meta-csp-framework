package examples.multi;

import java.util.logging.Level;

import multi.allenInterval.AllenInterval;
import multi.allenInterval.AllenIntervalConstraint;
import multi.allenInterval.AllenIntervalNetworkSolver;
import time.Bounds;
import utility.logging.MetaCSPLogging;
import framework.Constraint;
import framework.ConstraintNetwork;

public class TestAllenIntervalNetworkSolver {
	
	
	public static void main(String[] args) {
		
		AllenIntervalNetworkSolver solver = new AllenIntervalNetworkSolver(0, 100);
		AllenInterval[] intervals = (AllenInterval[])solver.createVariables(3);

		MetaCSPLogging.setLevel(Level.FINEST);
//		MetaCSPLogging.setLevel(solver.getClass(), Level.FINEST);
//		MetaCSPLogging.setLevel(solver.getConstraintSolvers()[0].getClass(), Level.FINEST);
		
		//DRAW IT!
		ConstraintNetwork.draw(solver.getConstraintNetwork());
		
		AllenIntervalConstraint con1 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, new Bounds(10, 20));
		con1.setFrom(intervals[0]);
		con1.setTo(intervals[1]);

		AllenIntervalConstraint con2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, new Bounds(10, 20));
		con2.setFrom(intervals[1]);
		con2.setTo(intervals[2]);
		
		AllenIntervalConstraint con3 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, AllenIntervalConstraint.Type.After.getDefaultBounds());
		con3.setFrom(intervals[2]);
		con3.setTo(intervals[0]);
		
		Constraint[] cons = new Constraint[]{con1,con2,con3};
		System.out.println(solver.addConstraintsDebug(cons));


	}

}

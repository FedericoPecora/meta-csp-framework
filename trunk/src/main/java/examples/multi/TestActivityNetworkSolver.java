package examples.multi;

import java.util.logging.Level;

import multi.activity.Activity;
import multi.activity.ActivityNetworkSolver;
import multi.allenInterval.AllenIntervalConstraint;
import symbols.SymbolicValueConstraint;
import time.Bounds;
import utility.logging.MetaCSPLogging;
import utility.timelinePlotting.TimelinePublisher;
import framework.Constraint;
import framework.ConstraintNetwork;

public class TestActivityNetworkSolver {
	
	public void forDebug() {
		ActivityNetworkSolver solver = new ActivityNetworkSolver(0,100);
		Activity act1 = (Activity)solver.createVariable();
		act1.setSymbolicDomain("A", "B", "C");
		Activity act2 = (Activity)solver.createVariable();
		act2.setSymbolicDomain("B", "C");

		//DRAW IT!
		ConstraintNetwork.draw(solver.getConstraintNetwork());
		
		SymbolicValueConstraint con1 = new SymbolicValueConstraint(SymbolicValueConstraint.Type.EQUALS);
		con1.setFrom(act1);
		con1.setTo(act2);
		//solver.addConstraint(con1);
		
		AllenIntervalConstraint con2 = new AllenIntervalConstraint( AllenIntervalConstraint.Type.Before, new Bounds(10, 20));
		con2.setFrom(act1);
		con2.setTo(act2);
		//solver.addConstraint(con2);
		
		Constraint[] cons = new Constraint[]{con1,con2};
		solver.addConstraints(cons);
		
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		
		boolean add = false;
		
		while (true) {
			for (int i = 0; i < cons.length; i++) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (add) solver.addConstraint(cons[i]);
				else solver.removeConstraint(cons[i]);
			}
			add = !add;
		}
		
	}
	
	public static void main(String[] args) {
		ActivityNetworkSolver solver = new ActivityNetworkSolver(0,100);
		Activity act1 = (Activity)solver.createVariable();
		act1.setSymbolicDomain("A", "B", "C");
		Activity act2 = (Activity)solver.createVariable();
		act2.setSymbolicDomain("B", "C");
		
		ConstraintNetwork.draw(solver.getConstraintNetwork());

		MetaCSPLogging.setLevel(Level.FINEST);
//		MetaCSPLogging.setLevel(solver.getClass(), Level.FINEST);
//		MetaCSPLogging.setLevel(solver.getConstraintSolvers()[0].getClass(), Level.FINEST);

		SymbolicValueConstraint con1 = new SymbolicValueConstraint(SymbolicValueConstraint.Type.EQUALS);
		con1.setFrom(act1);
		con1.setTo(act2);
		//solver.addConstraint(con1);
		
		AllenIntervalConstraint con2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, new Bounds(10, 20));
		con2.setFrom(act1);
		con2.setTo(act2);
		//solver.addConstraint(con2);
		
		Constraint[] cons = new Constraint[]{con1,con2};
		solver.addConstraints(cons);
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(solver.getDescription());
		System.out.println(act1.getDescription());		


	}

}

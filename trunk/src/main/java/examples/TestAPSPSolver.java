package examples;

import java.util.logging.Level;

import time.APSPSolver;
import time.SimpleDistanceConstraint;
import time.TimePoint;
import utility.logging.MetaCSPLogging;
import framework.ConstraintNetwork;
import framework.Variable;


public class TestAPSPSolver {
	
	public static void main(String[] args) {
				
		APSPSolver solver = new APSPSolver(50, 500);
		
		MetaCSPLogging.setLevel(solver.getClass(), Level.FINE);

		System.out.println("1 " + ((TimePoint)solver.getVariable(0)));

		//solver.setOptions(framework.ConstraintSolver.OPTIONS.AUTO_PROPAGATE);
		Variable[] vars = solver.createVariables(3);
		Variable one = vars[0];
		Variable two = vars[1];
		Variable three = vars[2];

		System.out.println("2 " + ((TimePoint)solver.getVariable(0)));

		ConstraintNetwork.draw(solver.getConstraintNetwork());

		SimpleDistanceConstraint con1 = new SimpleDistanceConstraint();
		con1.setFrom(solver.getVariable(0));
		con1.setTo(one);
		con1.setMinimum(60);
		con1.setMaximum(75);
		
		SimpleDistanceConstraint con2 = new SimpleDistanceConstraint();
		con2.setFrom(one);
		con2.setTo(two);
		con2.setMinimum(7);
		con2.setMaximum(9);
		
		SimpleDistanceConstraint con3 = new SimpleDistanceConstraint();
		con3.setFrom(solver.getVariable(0));
		con3.setTo(two);
		con3.setMinimum(68);
		con3.setMaximum(70);

//		System.out.println(solver.addConstraint(con1)); //O(n^2)
//		System.out.println(solver.addConstraint(con2)); //O(n^2)
//		System.out.println(solver.addConstraint(con3)); //O(n^2)

		System.out.println(solver.addConstraints(new SimpleDistanceConstraint[] {con1,con2,con3}));
		
//		System.out.println("(Domain,Value) of " + one.getID() + ": (" + one.getDomain() + "," + one.getDomain().chooseValue("ET") + ")");
//		System.out.println("(*Domain,Value) of " + two.getID() + ": (" + two.getDomain() + "," + two.getDomain().chooseValue("LT") + ")");		
		
		SimpleDistanceConstraint con4 = new SimpleDistanceConstraint();
		con4.setFrom(two);
		con4.setTo(three);
		con4.setMinimum(56);
		con4.setMaximum(100);

		System.out.println(solver.addConstraint(con4));

		SimpleDistanceConstraint con5 = new SimpleDistanceConstraint();
		con5.setFrom(one);
		con5.setTo(three);
		con5.setMinimum(70);
		con5.setMaximum(100);
		
		while (true) {
			solver.addConstraint(con5);
			System.out.println("Rigidity: " + solver.getRMSRigidity());
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			solver.removeConstraint(con2);
			System.out.println("Rigidity: " + solver.getRMSRigidity());
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			solver.removeConstraint(con5);
			System.out.println("Rigidity: " + solver.getRMSRigidity());
	
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
			solver.addConstraint(con2);
			System.out.println("Rigidity: " + solver.getRMSRigidity());
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
		//solver.draw();

	}

}

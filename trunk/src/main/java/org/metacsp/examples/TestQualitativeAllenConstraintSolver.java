package org.metacsp.examples;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.time.qualitative.QualitativeAllenIntervalConstraint;
import org.metacsp.time.qualitative.QualitativeAllenSolver;
import org.metacsp.time.qualitative.SimpleAllenInterval;


public class TestQualitativeAllenConstraintSolver {
	
public static void main(String[] args) {
		
		QualitativeAllenSolver solver = new QualitativeAllenSolver(); 
		Variable[] vars = solver.createVariables(3);
		
		SimpleAllenInterval re0 = (SimpleAllenInterval)vars[0];
		SimpleAllenInterval re1 = (SimpleAllenInterval)vars[1];
		SimpleAllenInterval re2 = (SimpleAllenInterval)vars[2];
		
		QualitativeAllenIntervalConstraint con0 = new QualitativeAllenIntervalConstraint(QualitativeAllenIntervalConstraint.Type.Before, QualitativeAllenIntervalConstraint.Type.Meets);
		con0.setFrom(re0);
		con0.setTo(re1);
//		System.out.println("Adding constraint " + con0 + ": " + solver.addConstraint(con0));
		
		QualitativeAllenIntervalConstraint con1 = new QualitativeAllenIntervalConstraint(QualitativeAllenIntervalConstraint.Type.After);
		con1.setFrom(re1);
		con1.setTo(re2);
//		System.out.println("Adding constraint " + con1 + ": " + solver.addConstraint(con1));
		
		QualitativeAllenIntervalConstraint con2 = new QualitativeAllenIntervalConstraint(QualitativeAllenIntervalConstraint.Type.Finishes);
		con2.setFrom(re2);
		con2.setTo(re0);
//		System.out.println("Adding constraint " + con2 + ": " + solver.addConstraint(con2));
		
		QualitativeAllenIntervalConstraint[] allConstraints = {con0, con1, con2};
		if (!solver.addConstraints(allConstraints)) { 
			System.out.println("Failed to add constraints!");
			System.exit(0);
		}
		
		ConstraintNetwork.draw(solver.getConstraintNetwork());
	}
	

}

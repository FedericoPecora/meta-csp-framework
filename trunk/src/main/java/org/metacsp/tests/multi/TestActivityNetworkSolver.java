package org.metacsp.tests.multi;

import java.util.logging.Level;

import junit.framework.TestCase;
import org.metacsp.meta.symbolsAndTime.Scheduler;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenInterval;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.allenInterval.AllenIntervalNetworkSolver;
import org.metacsp.multi.symbols.SymbolicValueConstraint;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;

public class TestActivityNetworkSolver extends TestCase {
	
	@Override
	public void setUp() throws Exception {
		MetaCSPLogging.setLevel(Level.OFF);
	}

	@Override
	public void tearDown() throws Exception {
	}

	public void testConsistency() {
		ActivityNetworkSolver solver = new ActivityNetworkSolver(0,500, new String[] {"A","B","C","D","E","F"});
		Activity act1 = (Activity)solver.createVariable();
		act1.setSymbolicDomain("A", "B", "C");
		Activity act2 = (Activity)solver.createVariable();
		act2.setSymbolicDomain("B", "C");
		
		SymbolicValueConstraint con1 = new SymbolicValueConstraint(SymbolicValueConstraint.Type.EQUALS);
		con1.setFrom(act1);
		con1.setTo(act2);
		
		AllenIntervalConstraint con2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, new Bounds(10, 20));
		con2.setFrom(act1);
		con2.setTo(act2);

		AllenIntervalConstraint con3 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(5, 5));
		con3.setFrom(act1);
		con3.setTo(act1);

		AllenIntervalConstraint con4 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(5, 5));
		con4.setFrom(act2);
		con4.setTo(act2);

		AllenIntervalConstraint con5 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(13, solver.getHorizon()));
		con5.setFrom(act2);
		con5.setTo(act2);

		AllenIntervalConstraint con5a = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(13, solver.getHorizon()));
		con5a.setFrom(act2);
		con5a.setTo(act2);

		Constraint[] cons = new Constraint[]{con1,con2,con3,con4,con5,con5a};
		assertTrue(solver.addConstraints(cons));
		
		assertTrue(act1.getTemporalVariable().getEST() == 0);
		assertTrue(act1.getTemporalVariable().getLST() == 480);
		assertTrue(act1.getTemporalVariable().getEET() == 5);
		assertTrue(act1.getTemporalVariable().getLET() == 485);
		
		assertTrue(act2.getTemporalVariable().getEST() == 15);
		assertTrue(act2.getTemporalVariable().getLST() == 495);
		assertTrue(act2.getTemporalVariable().getEET() == 20);
		assertTrue(act2.getTemporalVariable().getLET() == 500);
	}
	
	
}
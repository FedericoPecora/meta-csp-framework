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

public class TestAllenIntervalNetworkSolver extends TestCase {
	
	@Override
	public void setUp() throws Exception {
		MetaCSPLogging.setLevel(Level.OFF);
	}

	@Override
	public void tearDown() throws Exception {
	}

	public void testConsistency() {
		 AllenIntervalNetworkSolver solver = new AllenIntervalNetworkSolver(0, 100);
         AllenInterval[] intervals = (AllenInterval[])solver.createVariables(3);

         AllenIntervalConstraint con1 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.During, AllenIntervalConstraint.Type.During.getDefaultBounds());
         con1.setFrom(intervals[0]);
         con1.setTo(intervals[1]);
         
         AllenIntervalConstraint con2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(30, 40));
         con2.setFrom(intervals[0]);
         con2.setTo(intervals[0]);

         AllenIntervalConstraint con3 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Overlaps, AllenIntervalConstraint.Type.Overlaps.getDefaultBounds());
         con3.setFrom(intervals[1]);
         con3.setTo(intervals[2]);

         Constraint[] cons = new Constraint[]{con1,con2,con3};
         assertTrue(solver.addConstraints(cons));
         
         assertTrue(intervals[2].getEST() == 1);
         assertTrue(intervals[2].getLST() == 98);
         assertTrue(intervals[2].getEET() == 33);
         assertTrue(intervals[2].getLET() == 100);
         
         assertTrue(intervals[1].getEST() == 0);
         assertTrue(intervals[1].getLST() == 67);
         assertTrue(intervals[1].getEET() == 32);
         assertTrue(intervals[1].getLET() == 99);

         assertTrue(intervals[0].getEST() == 1);
         assertTrue(intervals[0].getLST() == 68);
         assertTrue(intervals[0].getEET() == 31);
         assertTrue(intervals[0].getLET() == 98);

	}
	
	
}
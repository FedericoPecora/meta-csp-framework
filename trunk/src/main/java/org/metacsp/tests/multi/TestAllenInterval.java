package org.metacsp.tests.multi;

import java.util.logging.Level;

import junit.framework.TestCase;

import org.metacsp.framework.Constraint;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenInterval;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.allenInterval.AllenIntervalNetworkSolver;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;

public class TestAllenInterval extends TestCase {
	
	@Override
	public void setUp() throws Exception {
		MetaCSPLogging.setLevel(Level.OFF);
	}

	@Override
	public void tearDown() throws Exception {
	}

	/**
	 * A bug caused by creating AllenIntervals on AllenIntervalNetworkSolver
	 * bypassing the creation of default bounds.
	 */
	public void testNoDefaultBounds() {
		
		AllenIntervalNetworkSolver solver = new AllenIntervalNetworkSolver(0, 200);
        AllenInterval[] intervals = (AllenInterval[])solver.createVariables(3);
        
        Bounds problemBounds = new Bounds(5, APSPSolver.INF);
        
        AllenIntervalConstraint con1 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.During, problemBounds, problemBounds);
        con1.setFrom(intervals[0]);
        con1.setTo(intervals[1]);
        
        AllenIntervalConstraint con2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.At, new Bounds(0, 0), new Bounds(100, 100));
        con2.setFrom(intervals[1]);
        con2.setTo(intervals[1]);

        Constraint[] cons = new Constraint[]{con1,con2};
        solver.addConstraints(cons);
        
        assertTrue(  intervals[0].getEST() == 5 );
        assertTrue(  intervals[0].getLST() == 95 );
        assertTrue(  intervals[0].getEET() == 5 );
        assertTrue(  intervals[0].getLET() == 95 );
        
        assertTrue(  intervals[1].getEST() == 0 );
        assertTrue(  intervals[1].getLST() == 0 );
        assertTrue(  intervals[1].getEET() == 100 );
        assertTrue(  intervals[1].getLET() == 100 );        
	}
	
	/**
	 * A bug caused by creating AllenIntervals on AllenIntervalNetworkSolver
	 * bypassing the creation of default bounds.
	 * 
	 * This test-case assure that it still works when creating on the
	 * ActivityNetworkSolver
	 */
	public void testNoDefaultBoundsOnActivityNetworkSolver() {
		
		ActivityNetworkSolver solver = new ActivityNetworkSolver(0, 200);
        solver.createVariables(2);
        
        Activity intervals[] = new Activity[2];
        
        intervals[0] = (Activity)solver.getVariable(0);
        intervals[1] = (Activity)solver.getVariable(1);
        
        Bounds problemBounds = new Bounds(5, APSPSolver.INF);
        
        AllenIntervalConstraint con1 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.During, problemBounds, problemBounds);
        con1.setFrom(intervals[0]);
        con1.setTo(intervals[1]);
        
        AllenIntervalConstraint con2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.At, new Bounds(0, 0), new Bounds(100, 100));
        con2.setFrom(intervals[1]);
        con2.setTo(intervals[1]);

        Constraint[] cons = new Constraint[]{con1,con2};
        solver.addConstraints(cons);
        
        assertTrue(  intervals[0].getTemporalVariable().getEST() == 5 );
        assertTrue(  intervals[0].getTemporalVariable().getLST() == 95 );
        assertTrue(  intervals[0].getTemporalVariable().getEET() == 5 );
        assertTrue(  intervals[0].getTemporalVariable().getLET() == 95 );
        
        assertTrue(  intervals[1].getTemporalVariable().getEST() == 0 );
        assertTrue(  intervals[1].getTemporalVariable().getLST() == 0 );
        assertTrue(  intervals[1].getTemporalVariable().getEET() == 100 );
        assertTrue(  intervals[1].getTemporalVariable().getLET() == 100 );        
	}
	
}
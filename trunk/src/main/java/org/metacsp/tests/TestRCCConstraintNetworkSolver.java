package org.metacsp.tests;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;
import org.metacsp.meta.symbolsAndTime.Scheduler;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenInterval;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.allenInterval.AllenIntervalNetworkSolver;
import org.metacsp.spatial.RCC.RCCConstraint;
import org.metacsp.spatial.RCC.RCCConstraintSolver;
import org.metacsp.spatial.RCC.Region;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;
import org.metacsp.time.SimpleDistanceConstraint;
import org.metacsp.time.TimePoint;
import org.metacsp.utility.logging.MetaCSPLogging;
import org.metacsp.booleanSAT.BooleanConstraint;
import org.metacsp.booleanSAT.BooleanDomain;
import org.metacsp.booleanSAT.BooleanSatisfiabilitySolver;
import org.metacsp.booleanSAT.BooleanVariable;
import org.metacsp.examples.TestBooleanSatisfiabilitySolverSATAlternative1;
import org.metacsp.examples.TestBooleanSatisfiabilitySolverSATNonCNF;
import org.metacsp.examples.TestBooleanSatisfiabilitySolverUNSAT;
import org.metacsp.examples.TestBooleanSatisfiabilitySolverUNSATNonCNF;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;

public class TestRCCConstraintNetworkSolver extends TestCase {
	
	@Override
	public void setUp() throws Exception {
		MetaCSPLogging.setLevel(Level.OFF);
	}

	@Override
	public void tearDown() throws Exception {
	}

	public void testConsistency() {
		RCCConstraintSolver solver = new RCCConstraintSolver(); 
		Variable[] vars = solver.createVariables(3);
		
		Region re0 = (Region)vars[0];
		Region re1 = (Region)vars[1];
		Region re2 = (Region)vars[2];
		
		RCCConstraint con0 = new RCCConstraint(RCCConstraint.Type.NTPP, RCCConstraint.Type.PO);
		con0.setFrom(re0);
		con0.setTo(re1);
		assertTrue(solver.addConstraint(con0));
		
		RCCConstraint con1 = new RCCConstraint(RCCConstraint.Type.DC);
		con1.setFrom(re1);
		con1.setTo(re2);
		assertTrue(solver.addConstraint(con1));
	}
	
	public void testInconsistency() {
		RCCConstraintSolver solver = new RCCConstraintSolver(); 
		Variable[] vars = solver.createVariables(3);
		
		Region re0 = (Region)vars[0];
		Region re1 = (Region)vars[1];
		Region re2 = (Region)vars[2];
		
		RCCConstraint con0 = new RCCConstraint(RCCConstraint.Type.NTPP, RCCConstraint.Type.PO);
		con0.setFrom(re0);
		con0.setTo(re1);
		
		RCCConstraint con1 = new RCCConstraint(RCCConstraint.Type.DC);
		con1.setFrom(re1);
		con1.setTo(re2);
		
		RCCConstraint con2 = new RCCConstraint(RCCConstraint.Type.NTPPI);
		con2.setFrom(re2);
		con2.setTo(re0);
		assertFalse(solver.addConstraints(con0,con1,con2));		
	}
	
}
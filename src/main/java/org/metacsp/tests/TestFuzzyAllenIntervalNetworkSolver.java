package org.metacsp.tests;

import java.util.logging.Level;

import junit.framework.TestCase;

import org.metacsp.fuzzyAllenInterval.FuzzyAllenIntervalConstraint;
import org.metacsp.fuzzyAllenInterval.FuzzyAllenIntervalNetworkSolver;
import org.metacsp.time.qualitative.SimpleAllenInterval;
import org.metacsp.utility.logging.MetaCSPLogging;

public class TestFuzzyAllenIntervalNetworkSolver extends TestCase {
	
	@Override
	public void setUp() throws Exception {
		MetaCSPLogging.setLevel(Level.OFF);
	}

	@Override
	public void tearDown() throws Exception {
	}

	public void testPossibilityDegree() {
		FuzzyAllenIntervalNetworkSolver solver = new FuzzyAllenIntervalNetworkSolver();
		SimpleAllenInterval act0 = (SimpleAllenInterval)solver.createVariable();
		SimpleAllenInterval act1 = (SimpleAllenInterval)solver.createVariable();
		SimpleAllenInterval act2 = (SimpleAllenInterval)solver.createVariable();

		FuzzyAllenIntervalConstraint con0 = new FuzzyAllenIntervalConstraint(FuzzyAllenIntervalConstraint.Type.After);
		con0.setFrom(act0);
		con0.setTo(act1);
		
		FuzzyAllenIntervalConstraint con1 = new FuzzyAllenIntervalConstraint(FuzzyAllenIntervalConstraint.Type.Contains);
		con1.setFrom(act1);
		con1.setTo(act2);

		FuzzyAllenIntervalConstraint con3 = new FuzzyAllenIntervalConstraint(FuzzyAllenIntervalConstraint.Type.Meets);
		con3.setFrom(act2);
		con3.setTo(act0);
		
		FuzzyAllenIntervalConstraint[] allConstraints = {con0,con1,con3};
		assertTrue(solver.addConstraints(allConstraints));
		assertTrue(solver.getPosibilityDegree() == 0.8);
	}
	
}
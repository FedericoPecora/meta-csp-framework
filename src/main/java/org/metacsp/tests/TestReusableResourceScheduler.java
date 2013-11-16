package org.metacsp.tests;

import java.util.logging.Level;

import junit.framework.TestCase;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.VariableOrderingH;
import org.metacsp.meta.symbolsAndTime.ReusableResource;
import org.metacsp.meta.symbolsAndTime.Scheduler;
import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint.Type;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;

public class TestReusableResourceScheduler extends TestCase {
	
	@Override
	public void setUp() throws Exception {
		MetaCSPLogging.setLevel(Level.OFF);
	}

	@Override
	public void tearDown() throws Exception {
	}

	public void testSchedulingConflictResolution() {
		
		final Scheduler metaSolver = new Scheduler(0,600,0);
		final ActivityNetworkSolver groundSolver = (ActivityNetworkSolver)metaSolver.getConstraintSolvers()[0];
		
		Activity one = (Activity)groundSolver.createVariable("comp1");
		one.setSymbolicDomain("2");
		Activity two = (Activity)groundSolver.createVariable("comp1");
		two.setSymbolicDomain("1");
		Activity three = (Activity)groundSolver.createVariable("comp1");
		three.setSymbolicDomain("3");
		
		//DURATIONS
		AllenIntervalConstraint dur1 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(35, 55));
		dur1.setFrom(one);
		dur1.setTo(one);
		AllenIntervalConstraint dur2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(35, 55));
		dur2.setFrom(two);
		dur2.setTo(two);
		AllenIntervalConstraint dur3 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(35, 55));
		dur3.setFrom(three);
		dur3.setTo(three);

		//PRECEDENCES
		AllenIntervalConstraint con1 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, Type.Before.getDefaultBounds());
		con1.setFrom(one);
		con1.setTo(two);
		
		//Add the constraints
		Constraint[] cons = new Constraint[]{dur1,dur2,dur3,con1/*,eqOne,eqTwo,eqThree*/};
		groundSolver.addConstraints(cons);
		
		//Most critical conflict is the one with most activities (largest peak)
		VariableOrderingH varOH = new VariableOrderingH() {
			@Override
			public int compare(ConstraintNetwork arg0, ConstraintNetwork arg1) {
				// TODO Auto-generated method stub
				return arg1.getVariables().length - arg0.getVariables().length;
			}

			@Override
			public void collectData(ConstraintNetwork[] allMetaVariables) {
				// TODO Auto-generated method stub
				
			}
		};
		
		ValueOrderingH valOH = new ValueOrderingH() {
			@Override
			public int compare(ConstraintNetwork o1, ConstraintNetwork o2) {
				// TODO Auto-generated method stub
				return 0;
			}
		};
		
		ReusableResource rr1 = new ReusableResource(varOH, valOH, 4);
		ReusableResource rr2 = new ReusableResource(varOH, valOH, 3);
		rr1.setUsage(one,two,three);
		//rr2.setUsage(oneA,twoA,threeA);
		rr2.setUsage(two,three);
		metaSolver.addMetaConstraint(rr1);
		metaSolver.addMetaConstraint(rr2);

		assertTrue(one.getTemporalVariable().getEST() == three.getTemporalVariable().getEST());
		metaSolver.backtrack();
		assertTrue(one.getTemporalVariable().getEST() != three.getTemporalVariable().getEST());
	
	}
	
}
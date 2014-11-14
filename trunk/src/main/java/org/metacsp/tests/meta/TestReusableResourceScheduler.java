package org.metacsp.tests.meta;

import java.util.logging.Level;

import junit.framework.TestCase;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.VariableOrderingH;
import org.metacsp.meta.symbolsAndTime.ReusableResource;
import org.metacsp.meta.symbolsAndTime.Schedulable.PEAKCOLLECTION;
import org.metacsp.meta.symbolsAndTime.Scheduler;
import org.metacsp.multi.activity.SymbolicVariableActivity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint.Type;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;
import org.metacsp.utility.timelinePlotting.TimelinePublisher;
import org.metacsp.utility.timelinePlotting.TimelineVisualizer;

public class TestReusableResourceScheduler extends TestCase {
	
	@Override
	public void setUp() throws Exception {
		MetaCSPLogging.setLevel(Level.OFF);
	}

	@Override
	public void tearDown() throws Exception {
	}
	
	/**
	 * Bug: When two activities meet they create a peak and scheduling fails.
	 */
	public void testMeetsCausesOverUsage() {
		final Scheduler metaSolver = new Scheduler(0,600,0);
		final ActivityNetworkSolver groundSolver = (ActivityNetworkSolver)metaSolver.getConstraintSolvers()[0];
		
		SymbolicVariableActivity one = (SymbolicVariableActivity)groundSolver.createVariable("comp1");
		one.setSymbolicDomain("1");
		SymbolicVariableActivity two = (SymbolicVariableActivity)groundSolver.createVariable("comp1");
		two.setSymbolicDomain("1");
		
		//DURATIONS
		AllenIntervalConstraint dur1 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(35, 55));
		dur1.setFrom(one);
		dur1.setTo(one);
		AllenIntervalConstraint dur2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(35, 55));
		dur2.setFrom(two);
		dur2.setTo(two);

		//PRECEDENCES
		AllenIntervalConstraint con1 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Meets);
		con1.setFrom(one);
		con1.setTo(two);
	
		//Add the constraints
		Constraint[] cons = new Constraint[]{dur1,dur2,con1/*,eqOne,eqTwo,eqThree*/};
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
		
		ReusableResource rr1 = new ReusableResource(varOH, valOH, 1);

		rr1.setUsage(one,two);

		metaSolver.addMetaConstraint(rr1);
		
		assertTrue( metaSolver.backtrack() );
	
	}
	
	public void testResourcesOrig() {
		final Scheduler metaSolver = new Scheduler(0,600,0);
		final ActivityNetworkSolver groundSolver = (ActivityNetworkSolver)metaSolver.getConstraintSolvers()[0];
		
		SymbolicVariableActivity one = (SymbolicVariableActivity)groundSolver.createVariable("comp1");
		one.setSymbolicDomain("2");
		SymbolicVariableActivity two = (SymbolicVariableActivity)groundSolver.createVariable("comp1");
		two.setSymbolicDomain("1");
		SymbolicVariableActivity three = (SymbolicVariableActivity)groundSolver.createVariable("comp1");
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
		rr2.setUsage(two,three);
		metaSolver.addMetaConstraint(rr1);
		metaSolver.addMetaConstraint(rr2);
	
		
		assertTrue( metaSolver.backtrack() );
	
	}

	/**
	 * Sampling peak collection misses this conflict. The problem disappears when removing Activity a1 and a3 (see next test case)
	 * @throws Exception
	 */
	public void testPeakCollectionSamplingBug() throws Exception {
		final Scheduler metaSolver = new Scheduler(0,5000,0);
		
		final ActivityNetworkSolver groundSolver = (ActivityNetworkSolver)metaSolver.getConstraintSolvers()[0];
		
		SymbolicVariableActivity a1 = (SymbolicVariableActivity)groundSolver.createVariable("comp1");
		a1.setSymbolicDomain("1");
		SymbolicVariableActivity a2 = (SymbolicVariableActivity)groundSolver.createVariable("comp1");
		a2.setSymbolicDomain("1");
		SymbolicVariableActivity a3 = (SymbolicVariableActivity)groundSolver.createVariable("comp1");
		a3.setSymbolicDomain("1");
		SymbolicVariableActivity a4 = (SymbolicVariableActivity)groundSolver.createVariable("comp1");
		a4.setSymbolicDomain("1");
		SymbolicVariableActivity a5 = (SymbolicVariableActivity)groundSolver.createVariable("comp1");
		a5.setSymbolicDomain("1");
				
		
//		liftUsage(slow0) -> liftUsage(slow0)::<SymbolicVariable 16: [1]>U<AllenInterval 16 (I-TP: 34 35 ) [[39, 4799], [97, 4857]]>
//		liftUsage(slow0) -> liftUsage(slow0)::<SymbolicVariable 19: [1]>U<AllenInterval 19 (I-TP: 40 41 ) [[39, 4799], [5000, 5000]]>
//		liftUsage(slow0) -> liftUsage(slow0)::<SymbolicVariable 32: [1]>U<AllenInterval 32 (I-TP: 66 67 ) [[137, 4897], [191, 4951]]>
//		liftUsage(slow0) -> liftUsage(slow0)::<SymbolicVariable 40: [1]>U<AllenInterval 40 (I-TP: 82 83 ) [[192, 4952], [5000, 5000]]>
//		liftUsage(slow0) -> liftUsage(slow0)::<SymbolicVariable 47: [1]>U<AllenInterval 47 (I-TP: 96 97 ) [[239, 4999], [5000, 5000]]>
		AllenIntervalConstraint rel1 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(39, 4799));
		AllenIntervalConstraint dead1 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, new Bounds(97, 4857));
		rel1.setFrom(a1); 	rel1.setTo(a1);  dead1.setFrom(a1); 	dead1.setTo(a1);
		
		AllenIntervalConstraint rel2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(39, 4799));
		AllenIntervalConstraint dead2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, new Bounds(5000, 5000));
		rel2.setFrom(a2); 	rel2.setTo(a2);  dead2.setFrom(a2); 	dead2.setTo(a2);

		AllenIntervalConstraint rel3 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(137, 4897));
		AllenIntervalConstraint dead3 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, new Bounds(191, 4951));
		rel3.setFrom(a3); 	rel3.setTo(a3);  dead3.setFrom(a3); 	dead3.setTo(a3);

		AllenIntervalConstraint rel4 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(192, 4952));
		AllenIntervalConstraint dead4 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, new Bounds(5000, 5000));
		rel4.setFrom(a4); 	rel4.setTo(a4);  dead4.setFrom(a4); 	dead4.setTo(a4);

		AllenIntervalConstraint rel5 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(239, 4999));
		AllenIntervalConstraint dead5 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, new Bounds(5000, 5000));
		rel5.setFrom(a5); 	rel5.setTo(a5);  dead5.setFrom(a5); 	dead5.setTo(a5);
	
//		Thread.sleep(20000);
		
		//Add the constraints
		Constraint[] cons = new Constraint[]{rel1, rel2, rel3, rel4, rel5, dead1, dead2, dead3, dead4, dead5 };
		boolean tempCon = groundSolver.addConstraints(cons);
		
		assertTrue( tempCon );
		
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
		
		ReusableResource rr1 = new ReusableResource(varOH, valOH, 2);
		rr1.setPeakCollectionStrategy(PEAKCOLLECTION.SAMPLING);
		
		rr1.setUsage( a1, a2, a3, a4, a5 );
		metaSolver.addMetaConstraint(rr1);
		
		boolean hasSolution = metaSolver.backtrack();
		
//		while(true) {
//			Thread.sleep(100);
//			System.out.print(".");
//		}
		
		assertFalse( hasSolution );		
	}
	
	/**
	 * Same as previous test case with two Activities removed. Now the conflict is found and backtrack() returns false
	 * @throws Exception
	 */
	public void testPeakCollectionSamplingBugDisappears() throws Exception {
		final Scheduler metaSolver = new Scheduler(0,5000,0);
		final ActivityNetworkSolver groundSolver = (ActivityNetworkSolver)metaSolver.getConstraintSolvers()[0];
		
		SymbolicVariableActivity a2 = (SymbolicVariableActivity)groundSolver.createVariable("comp1");
		a2.setSymbolicDomain("1");
		SymbolicVariableActivity a4 = (SymbolicVariableActivity)groundSolver.createVariable("comp1");
		a4.setSymbolicDomain("1");
		SymbolicVariableActivity a5 = (SymbolicVariableActivity)groundSolver.createVariable("comp1");
		a5.setSymbolicDomain("1");
						
		AllenIntervalConstraint rel2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(39, 4799));
		AllenIntervalConstraint dead2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, new Bounds(5000, 5000));
		rel2.setFrom(a2); 	rel2.setTo(a2);  dead2.setFrom(a2); 	dead2.setTo(a2);

		AllenIntervalConstraint rel4 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(192, 4952));
		AllenIntervalConstraint dead4 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, new Bounds(5000, 5000));
		rel4.setFrom(a4); 	rel4.setTo(a4);  dead4.setFrom(a4); 	dead4.setTo(a4);

		AllenIntervalConstraint rel5 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(239, 4999));
		AllenIntervalConstraint dead5 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, new Bounds(5000, 5000));
		rel5.setFrom(a5); 	rel5.setTo(a5);  dead5.setFrom(a5); 	dead5.setTo(a5);

		//Add the constraints
		Constraint[] cons = new Constraint[]{ rel2,  rel4, rel5,  dead2,  dead4, dead5 };
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
		
		TimelinePublisher tlp = new TimelinePublisher(groundSolver, "comp1");
		TimelineVisualizer tv = new TimelineVisualizer(tlp);
		tlp.publish(false, true);
		
		ReusableResource rr1 = new ReusableResource(varOH, valOH, 2);
		rr1.setPeakCollectionStrategy(PEAKCOLLECTION.SAMPLING);
		rr1.setUsage( a2, a4, a5);
		metaSolver.addMetaConstraint(rr1);
				
		boolean hasSolution = metaSolver.backtrack();
				
		assertFalse( hasSolution );		
	}
	
	public void testThreeUsagesOfBinaryResourceFail() {
		final Scheduler metaSolver = new Scheduler(0,5000,0);
		
		final ActivityNetworkSolver groundSolver = (ActivityNetworkSolver)metaSolver.getConstraintSolvers()[0];
		
		SymbolicVariableActivity a1 = (SymbolicVariableActivity)groundSolver.createVariable("comp1");
		a1.setSymbolicDomain("1");
		SymbolicVariableActivity a2 = (SymbolicVariableActivity)groundSolver.createVariable("comp1");
		a2.setSymbolicDomain("1");
		SymbolicVariableActivity a3 = (SymbolicVariableActivity)groundSolver.createVariable("comp1");
		a3.setSymbolicDomain("1");
				
		
		AllenIntervalConstraint rel1 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(4994, 4997));
		AllenIntervalConstraint dead1 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, new Bounds(4996, 4999));
		rel1.setFrom(a1); 	rel1.setTo(a1);  dead1.setFrom(a1); 	dead1.setTo(a1);
		
		AllenIntervalConstraint rel2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(4994, 4997));
		AllenIntervalConstraint dead2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, new Bounds(4996, 4999));
		rel2.setFrom(a2); 	rel2.setTo(a2);  dead2.setFrom(a2); 	dead2.setTo(a2);

		AllenIntervalConstraint rel3 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(4994, 4997));
		AllenIntervalConstraint dead3 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Deadline, new Bounds(4996, 4999));
		rel3.setFrom(a3); 	rel3.setTo(a3);  dead3.setFrom(a3); 	dead3.setTo(a3);

		//Add the constraints
		Constraint[] cons = new Constraint[]{rel1, rel2, rel3, dead1, dead2, dead3 };
		boolean tempCon = groundSolver.addConstraints(cons);
		
		assertTrue( tempCon );
		
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
		
		ReusableResource rr1 = new ReusableResource(varOH, valOH, 2);
		rr1.setPeakCollectionStrategy(PEAKCOLLECTION.SAMPLING);
		
		rr1.setUsage( a1, a2, a3 );
		metaSolver.addMetaConstraint(rr1);
		
		boolean hasSolution = metaSolver.backtrack();
				
		assertTrue( hasSolution );		
	}
	
}
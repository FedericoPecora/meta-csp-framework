package examples.meta;

import meta.symbolsAndTime.Scheduler;
import meta.symbolsAndTime.StateVariable;
import meta.symbolsAndTime.SymbolicTimeline;
import multi.activity.Activity;
import multi.activity.ActivityNetworkSolver;
import multi.allenInterval.AllenIntervalConstraint;
import symbols.SymbolicDomain;
import time.APSPSolver;
import time.Bounds;
import utility.UI.Callback;
import utility.timelinePlotting.TimelinePublisher;
import utility.timelinePlotting.TimelineVisualizer;
import framework.Constraint;
import framework.ConstraintNetwork;
import framework.ValueOrderingH;
import framework.VariableOrderingH;

public class TestStateVariableSchedulerSimple {
	
	public static void main(String[] args) {
		
		final Scheduler metaSolver = new Scheduler(0,600,0);
		final ActivityNetworkSolver groundSolver = (ActivityNetworkSolver)metaSolver.getConstraintSolvers()[0];
		
		Activity one = (Activity)groundSolver.createVariable("comp1");
		one.setSymbolicDomain("A", "B");
		Activity two = (Activity)groundSolver.createVariable("comp1");
		two.setSymbolicDomain("A", "B", "C");
		Activity three = (Activity)groundSolver.createVariable("comp1");
		three.setSymbolicDomain("D", "E");

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

		//CONSTRAINTS
		AllenIntervalConstraint con1 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Overlaps, new Bounds(5,10));
		con1.setFrom(one);
		con1.setTo(two);
	
		//Add the constraints
		Constraint[] cons = new Constraint[]{dur1,dur2,dur3,con1};
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
		
		StateVariable sv = new StateVariable(varOH, valOH, metaSolver, new SymbolicDomain(null, "A", "B", "C", "D", "E"));
		sv.setUsage(one,two,three);
		metaSolver.addMetaConstraint(sv);
		
		final TimelinePublisher tp = new TimelinePublisher(groundSolver, "comp1");
		TimelineVisualizer viz = new TimelineVisualizer(tp);
		
		tp.publish(true, true);

		Callback cb = new Callback() {
			@Override
			public void performOperation() {
				System.out.println("SOLVED? " + metaSolver.backtrack());				
				tp.publish(false, true);
				metaSolver.draw();
			}
		};
		ConstraintNetwork.draw(groundSolver.getConstraintNetwork(),cb);
		
	}

}

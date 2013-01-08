//package examples.multi;
//
//import java.util.logging.Level;
//
//import multi.allenInterval.AllenInterval;
//import multi.allenInterval.AllenIntervalConstraint;
//import multi.allenInterval.AllenIntervalNetworkSolver;
//import time.APSPSolver;
//import time.Bounds;
//import utility.logging.MetaCSPLogging;
//import framework.Constraint;
//import framework.ConstraintNetwork;
// 
//public class TestAllenIntervalNetworkSolver {
//	
//	
//	public static void main(String[] args) {
//		
//		System.out.println("fromScratch 1: " + APSPSolver.fromScratchCounter);
//		AllenIntervalNetworkSolver solver = new AllenIntervalNetworkSolver(0, 100);
//		System.out.println("fromScratch 2: " + APSPSolver.fromScratchCounter);
//		AllenInterval[] intervals = (AllenInterval[])solver.createVariables(3);
//		System.out.println("fromScratch 3: " + APSPSolver.fromScratchCounter);
//
//		AllenIntervalConstraint con1 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, new Bounds(10, 20));
//		con1.setFrom(intervals[0]);
//		con1.setTo(intervals[1]);
//		
//		AllenIntervalConstraint con2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, new Bounds(10, 20));
//		con2.setFrom(intervals[1]);
//		con2.setTo(intervals[2]);
//		AllenIntervalConstraint con3 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, AllenIntervalConstraint.Type.After.getDefaultBounds());
//		con3.setFrom(intervals[2]);
//		con3.setTo(intervals[0]);
//		
//		System.out.println( solver.addConstraint(con1) );
//		
//		System.out.println("fromScratch 4: " + APSPSolver.fromScratchCounter);
//
//		
////		solver.printMatrix();
//		System.out.println(solver.bookmark());
//		
//		System.out.println( solver.addConstraint(con2) );
//		
//		System.out.println("fromScratch 5: " + APSPSolver.fromScratchCounter);
//
//		
////		solver.printMatrix();
//		System.out.println(solver.bookmark());
//		
//		
//		System.out.println( solver.addConstraint(con3) );
//		
//		System.out.println("fromScratch 6: " + APSPSolver.fromScratchCounter);
//
//		
////		solver.printMatrix();
//		solver.revert(1);
////		System.out.println("..................................");
////		solver.printMatrix();		
//		System.out.println( solver.addConstraint(con3) );
//		
//		System.out.println("fromScratch 7: " + APSPSolver.fromScratchCounter);
//
//		
//		solver.revert(0);
////		solver.printMatrix();
//		System.out.println( solver.addConstraint(con3) );
//		
//		System.out.println("fromScratch 8: " + APSPSolver.fromScratchCounter);
//
//		
////		Constraint[] cons = new Constraint[]{con1,con2,con3};
////		System.out.println(solver.addConstraintsDebug(cons) == null);
//
//		System.out.println("fromScratch 9: " + APSPSolver.fromScratchCounter);
//		System.out.println("inc: " + APSPSolver.incCounter);
//		System.out.println("singleTPCreate: " + APSPSolver.singleTPcreation);
//		System.out.println("multiTOCreate: " + APSPSolver.multiTPcreation);
//		System.out.println("tPDelete: " + APSPSolver.tpDeleteCount);
//		System.out.println("cCreate: " + APSPSolver.cCreate);
//		System.out.println("constructorCalls: " + APSPSolver.constructorCalls);
//		System.out.println("propagations: " + APSPSolver.propagations);
//		
//		System.out.println("sCvar: " + APSPSolver.singleCreateVarSub);
//		System.out.println("mCvar: " + APSPSolver.multiCreateVarSub);
//
//	}
//
//}

package examples.multi;

import java.util.logging.Level;

import multi.allenInterval.AllenInterval;
import multi.allenInterval.AllenIntervalConstraint;
import multi.allenInterval.AllenIntervalNetworkSolver;
import time.Bounds;
import utility.logging.MetaCSPLogging;
import framework.Constraint;
import framework.ConstraintNetwork;

public class TestAllenIntervalNetworkSolver {
        
        
        public static void main(String[] args) {
                
                AllenIntervalNetworkSolver solver = new AllenIntervalNetworkSolver(0, 100);
                AllenInterval[] intervals = (AllenInterval[])solver.createVariables(3);

                MetaCSPLogging.setLevel(Level.FINEST);
//              MetaCSPLogging.setLevel(solver.getClass(), Level.FINEST);
//              MetaCSPLogging.setLevel(solver.getConstraintSolvers()[0].getClass(), Level.FINEST);
                
                //DRAW IT!
                ConstraintNetwork.draw(solver.getConstraintNetwork());
                
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
                System.out.println(solver.addConstraintsDebug(cons) == null);


        }

}

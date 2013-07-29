/*******************************************************************************
 * Copyright (c) 2010-2013 Federico Pecora <federico.pecora@oru.se>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package org.metacsp.examples.multi;

import java.util.logging.Level;

import org.metacsp.multi.allenInterval.AllenInterval;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.allenInterval.AllenIntervalNetworkSolver;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;

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

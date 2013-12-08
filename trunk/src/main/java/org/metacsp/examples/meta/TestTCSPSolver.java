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
package org.metacsp.examples.meta;

import java.util.logging.Level;

import org.metacsp.meta.TCSP.MostConstrainedFirstVarOH;
import org.metacsp.meta.TCSP.TCSPLabeling;
import org.metacsp.meta.TCSP.TCSPSolver;
import org.metacsp.meta.TCSP.WidestIntervalFirstValOH;
import org.metacsp.multi.TCSP.DistanceConstraint;
import org.metacsp.multi.TCSP.DistanceConstraintSolver;
import org.metacsp.multi.TCSP.MultiTimePoint;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.LoggerNotDefined;
import org.metacsp.utility.logging.MetaCSPLogging;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.VariableOrderingH;


public class TestTCSPSolver {
	
	public static void main(String args[]) {
		
		TCSPSolver metaSolver = new TCSPSolver(0, 100, 0);
		DistanceConstraintSolver groundSolver = (DistanceConstraintSolver)metaSolver.getConstraintSolvers()[0];		
		APSPSolver groundGroundSolver = (APSPSolver)groundSolver.getConstraintSolvers()[0];
		
		MetaCSPLogging.setLevel(metaSolver.getClass(), Level.FINEST);
		
		/*
		 * John travels to work either by car (30-40 min) or by bus (at least 60 min).
		 * Fred goes to work either by car (20-30 min) or in a carpool (40-50 min).
		 * Today John left home between 7:10 and 7:20 AM, and Fred arrived at work
		 * between 8:00 and 8:10 AM. John arrived at work 10-20 min after Fred left home.
		 */
		
		MultiTimePoint johnGoesToWork = (MultiTimePoint)groundSolver.createVariable();
		MultiTimePoint johnArrivesAtWork = (MultiTimePoint)groundSolver.createVariable();
		MultiTimePoint fredGoesToWork = (MultiTimePoint)groundSolver.createVariable();
		MultiTimePoint fredArrivesAtWork = (MultiTimePoint)groundSolver.createVariable();
		
		ConstraintNetwork.draw(groundSolver.getConstraintNetwork(), "TCSP");
		ConstraintNetwork.draw(groundGroundSolver.getConstraintNetwork(), "STP");
		
		DistanceConstraint johnTakesCarOrBus = new DistanceConstraint(new Bounds(30, 40), new Bounds(60, APSPSolver.INF));
		johnTakesCarOrBus.setFrom(johnGoesToWork);
		johnTakesCarOrBus.setTo(johnArrivesAtWork);
		
		DistanceConstraint fredTakesCarOrCarpool = new DistanceConstraint(new Bounds(40, 50), new Bounds(20, 30));
		fredTakesCarOrCarpool.setFrom(fredGoesToWork);
		fredTakesCarOrCarpool.setTo(fredArrivesAtWork);
		
		DistanceConstraint johnLeaves = new DistanceConstraint(new Bounds(10, 20));
		johnLeaves.setFrom(groundSolver.getSource());
		johnLeaves.setTo(johnGoesToWork);
		
		DistanceConstraint fredArrives = new DistanceConstraint(new Bounds(60, 70));
		fredArrives.setFrom(groundSolver.getSource());
		fredArrives.setTo(fredArrivesAtWork);
		
		DistanceConstraint johnArrives = new DistanceConstraint(new Bounds(10, 20));
		johnArrives.setFrom(johnArrivesAtWork);
		johnArrives.setTo(fredGoesToWork);
		
		groundSolver.addConstraints(new DistanceConstraint[] {johnTakesCarOrBus,fredTakesCarOrCarpool,johnLeaves,fredArrives,johnArrives});
		
		VariableOrderingH varOH = new MostConstrainedFirstVarOH();
		
		ValueOrderingH valOH = new WidestIntervalFirstValOH();

		TCSPLabeling metaCons = new TCSPLabeling(varOH, valOH);
		metaSolver.addMetaConstraint(metaCons);
		
		System.out.println("Solved? " + metaSolver.backtrack());
				
		metaSolver.draw();
		
		System.out.println(metaSolver.getDescription());
		System.out.println(metaCons.getDescription());
		
		//TU Delft
		//Leon Planken - see his website for temporal reasoning survey (2007)
		//See his JAIR 2012 for ICAPS work
		
	}

}

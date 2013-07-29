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

import java.util.Calendar;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Logger;

import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.utility.logging.MetaCSPLogging;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.Variable;

public class MeltYourCPUTestCaseActivityNetworkSolver {
	
	public static void main(String[] args) {
	
		Logger metaCSPLogger = MetaCSPLogging.getLogger(new MeltYourCPUTestCaseActivityNetworkSolver().getClass());

		ActivityNetworkSolver solver = new ActivityNetworkSolver(0,5000);
		Random rand = new Random(Calendar.getInstance().getTimeInMillis());

		//MetaCSPLogging.setLevel(APSPSolver.class, Level.FINE);
		
		while (true) {
			long timeNow = Calendar.getInstance().getTimeInMillis();
			//create between 50 and 100 vars
			Variable[] vars = solver.createVariables(rand.nextInt(50)+50);
			
			metaCSPLogger.info("Created " + vars.length + " variables (" + (Calendar.getInstance().getTimeInMillis()-timeNow) + " msec)");
			
			for (int i = 0; i < vars.length; i++) {
				Activity act = (Activity)vars[i];
				act.setSymbolicDomain("RandomActivity"+i);
			}
			
			Variable[] allVars = solver.getConstraintNetwork().getVariables();
			Vector<AllenIntervalConstraint> cons = new Vector<AllenIntervalConstraint>();
			for (int i = 0; i < rand.nextInt(allVars.length*allVars.length)+allVars.length; i++) {
				//int type = rand.nextInt(AllenIntervalConstraint.Type.values().length-1);
				int type = rand.nextInt(15);
				AllenIntervalConstraint aic = new AllenIntervalConstraint(AllenIntervalConstraint.Type.values()[type], AllenIntervalConstraint.Type.values()[type].getDefaultBounds());
				Variable from = allVars[rand.nextInt(allVars.length-1)];
				Variable to = allVars[rand.nextInt(allVars.length-1)];
				aic.setFrom(from);
				aic.setTo(to);
				cons.add(aic);
			}
			
			timeNow = Calendar.getInstance().getTimeInMillis();

			Vector<Constraint> added = new Vector<Constraint>();
			for (Constraint con : cons) {
				if (solver.addConstraint(con)) added.add(con);
			}
			metaCSPLogger.info("Added " + added.size() + "/" + cons.size() + " constraints (" + (Calendar.getInstance().getTimeInMillis()-timeNow) + " msec)");

//			if (solver.addConstraints(cons.toArray(new AllenIntervalConstraint[cons.size()]))) {
//				metaCSPLogger.info("Added " + cons.size() + " constraints (" + (Calendar.getInstance().getTimeInMillis()-timeNow) + " msec)");
//			}
//			else {
//				metaCSPLogger.info("Failed to add " + cons.size() + " constraints (" + (Calendar.getInstance().getTimeInMillis()-timeNow) + " msec)");				
//			}
			
			Vector<AllenIntervalConstraint> toRemoveCons = new Vector<AllenIntervalConstraint>();
			Vector<Activity> toRemoveActs = new Vector<Activity>();
			for (int i = 0; i < rand.nextInt((int)Math.floor(allVars.length/2.0))+(int)Math.floor(allVars.length/2.0); i++) {
				toRemoveActs.add((Activity)allVars[i]);
			}
			
			Constraint[] allCons = solver.getConstraintNetwork().getConstraints();
			for (Constraint con : allCons) {
				AllenIntervalConstraint aic = (AllenIntervalConstraint)con;
				if (toRemoveActs.contains(aic.getFrom()) || toRemoveActs.contains(aic.getTo())) {
					toRemoveCons.add(aic);
				}
			}
			
			int numOldCons = solver.getConstraintNetwork().getConstraints().length;
			int numOldActs = solver.getConstraintNetwork().getVariables().length;
			timeNow = Calendar.getInstance().getTimeInMillis();
			solver.removeConstraints(toRemoveCons.toArray(new AllenIntervalConstraint[toRemoveCons.size()]));
			metaCSPLogger.info("Removed " + toRemoveCons.size() + "/" + numOldCons + " constraints (" + (Calendar.getInstance().getTimeInMillis()-timeNow) + " msec)");
			timeNow = Calendar.getInstance().getTimeInMillis();
			solver.removeVariables(toRemoveActs.toArray(new Activity[toRemoveActs.size()]));
			metaCSPLogger.info("Removed " + toRemoveActs.size() + "/" + numOldActs + " variables (" + (Calendar.getInstance().getTimeInMillis()-timeNow) + " msec)");
		}
		
	}

}

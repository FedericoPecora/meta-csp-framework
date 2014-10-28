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
package org.metacsp.examples;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.spatial.RCC.RCCConstraint;
import org.metacsp.spatial.RCC.RCCConstraintSolver;
import org.metacsp.spatial.RCC.Region;


public class TestRCCConstraintNetworkSolver {

	public static void main(String[] args) {
		
		RCCConstraintSolver solver = new RCCConstraintSolver(); 
		Variable[] vars = solver.createVariables(3);
		
		Region re0 = (Region)vars[0];
		Region re1 = (Region)vars[1];
		Region re2 = (Region)vars[2];
		
		RCCConstraint con0 = new RCCConstraint(RCCConstraint.Type.NTPP,  RCCConstraint.Type.TPP);
		con0.setFrom(re0);
		con0.setTo(re1);
		System.out.println("Adding constraint " + con0 + ": " + solver.addConstraint(con0));
		
		RCCConstraint con1 = new RCCConstraint(RCCConstraint.Type.DC);
		con1.setFrom(re1);
		con1.setTo(re2);
		System.out.println("Adding constraint " + con1 + ": " + solver.addConstraint(con1));
		
//		RCCConstraint con2 = new RCCConstraint(RCCConstraint.Type.NTPPI);
//		con2.setFrom(re2);
//		con2.setTo(re0);
//		System.out.println("Adding constraint " + con2 + ": " + solver.addConstraint(con2));
		
		RCCConstraint con3 = new RCCConstraint(RCCConstraint.Type.EC);
		con3.setFrom(re0);
		con3.setTo(re2);
		System.out.println("Adding constraint " + con3 + ": " + solver.addConstraint(con3));

		
//		RCCConstraint[] allConstraints = {con0, con1, con2};
//		if (!solver.addConstraints(allConstraints)) { 
//			System.out.println("Failed to add constraints!");
//			System.exit(0);
//		}
		
		ConstraintNetwork.draw(solver.getConstraintNetwork());
	}
	

}




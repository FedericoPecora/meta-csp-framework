/*******************************************************************************
 * Copyright (c) 2010-2013 Masoumeh Mansouri <masoumeh.mansouri@oru.se>
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

import java.util.Vector;
import java.util.logging.Level;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.spatial.blockAlgebra.BlockAlgebraConstraint;
import org.metacsp.multi.spatial.blockAlgebra.BlockConstraintSolver;
import org.metacsp.multi.spatial.blockAlgebra.RectangularCuboidRegion;
import org.metacsp.multi.spatial.blockAlgebra.UnaryBlockConstraint;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;


public class TestBlockAlgebraConstraintSolverSimple {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		
		BlockConstraintSolver solver = new BlockConstraintSolver(0,1000);
		Vector<Constraint> allConstraints = new Vector<Constraint>();
		
		//..........................................................		
		RectangularCuboidRegion block1 = (RectangularCuboidRegion)solver.createVariable();		
		block1.setName("block1");
		
		RectangularCuboidRegion block2 = (RectangularCuboidRegion)solver.createVariable();		
		block2.setName("block2");

		
		BlockAlgebraConstraint onTopofEachother = new BlockAlgebraConstraint(
				new AllenIntervalConstraint(AllenIntervalConstraint.Type.Equals, AllenIntervalConstraint.Type.Equals.getDefaultBounds()), 
				new AllenIntervalConstraint(AllenIntervalConstraint.Type.Equals, AllenIntervalConstraint.Type.Equals.getDefaultBounds()),
				new AllenIntervalConstraint(AllenIntervalConstraint.Type.MetBy, AllenIntervalConstraint.Type.MetBy.getDefaultBounds())
				);
				
				
		onTopofEachother.setFrom(block1);
		onTopofEachother.setTo(block2);
		allConstraints.add(onTopofEachother);
		

		UnaryBlockConstraint atblock2 = new UnaryBlockConstraint(
				UnaryBlockConstraint.Type.At, new Bounds(20,20), new Bounds(28,28), new Bounds(35,35), new Bounds(42,42), new Bounds(0,0), new Bounds(20,20));
		atblock2.setFrom(block2);
		atblock2.setTo(block2);
		allConstraints.add(atblock2);
		
		UnaryBlockConstraint sizeblock1 = new UnaryBlockConstraint(
				UnaryBlockConstraint.Type.Size, new Bounds(8,8), new Bounds(7,7), new Bounds(30,30));
		sizeblock1.setFrom(block1);
		sizeblock1.setTo(block1);
		allConstraints.add(sizeblock1);
		
		
		Constraint[] allConstraintsArray = allConstraints.toArray(new Constraint[allConstraints.size()]);
		MetaCSPLogging.setLevel(Level.FINEST);
		if (!solver.addConstraints(allConstraintsArray)) { 
			System.out.println("Failed to add constraints!");
			System.exit(0);
		}
		
		
		
		
		System.out.println(solver.extractBoundingBoxesFromSTPs("block1").getAlmostCentreRecCuboid());
		ConstraintNetwork.draw(solver.getConstraintNetwork());
		
		
		

	}

}

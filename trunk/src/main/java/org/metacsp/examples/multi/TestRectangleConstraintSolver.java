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

import java.util.Vector;
import java.util.logging.Level;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangleConstraint;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangleConstraintSolver;
import org.metacsp.multi.spatial.rectangleAlgebra.RectangularRegion;
import org.metacsp.multi.spatial.rectangleAlgebra.UnaryRectangleConstraint;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;

public class TestRectangleConstraintSolver {

	/**
	 * @param args
	 */
	
	public static void main(String[] args) {
	
		RectangleConstraintSolver solver = new RectangleConstraintSolver(0,1000);
		Vector<Constraint> allConstraints = new Vector<Constraint>();
		
		//..........................................................
		//T-BOX Variables
		RectangularRegion knife = (RectangularRegion)solver.createVariable();		
		knife.setName("knife");
		
		RectangularRegion fork = (RectangularRegion)solver.createVariable();
		fork.setName("fork");
		
		RectangularRegion dish = (RectangularRegion)solver.createVariable();
		dish.setName("dish");
		
		RectangularRegion cup = (RectangularRegion)solver.createVariable();
		cup.setName("cup");
		
		RectangularRegion napkin = (RectangularRegion)solver.createVariable();
		napkin.setName("napkin");
		
		//..........................................................
		//A-BOX Variables and constraints
		RectangularRegion napkin1 = (RectangularRegion)solver.createVariable();
		napkin1.setName("napkin1");
		//napkin1.setBoundingBox(new BoundingBox(new Bounds(0, APSPSolver.INF), new Bounds(0, APSPSolver.INF), new Bounds(0, APSPSolver.INF), new Bounds(0, APSPSolver.INF)));
		//NO needd for this, the bounds will be [0,infty) anyway:
		//UnaryRectangleConstraint2 atNapkin1 = new UnaryRectangleConstraint2(UnaryRectangleConstraint2.Type.At, new Bounds(0,APSPSolver.INF), new Bounds(0,APSPSolver.INF), new Bounds(0,APSPSolver.INF), new Bounds(0,APSPSolver.INF));
		//atNapkin1.setFrom(napkin1);
		//atNapkin1.setTo(napkin1);
		
		RectangularRegion knife1 = (RectangularRegion)solver.createVariable();
		knife1.setName("knife1");
		//knife1.setBoundingBox(new BoundingBox(new Bounds(50, 50), new Bounds(55, 55), new Bounds(12, 12), new Bounds(26, 26)));
		UnaryRectangleConstraint atKnife1 = new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.At, new Bounds(50,50), new Bounds(55,55), new Bounds(12,12), new Bounds(26,26));
		atKnife1.setFrom(knife1);
		atKnife1.setTo(knife1);
		allConstraints.add(atKnife1);
		
		RectangularRegion fork1 = (RectangularRegion)solver.createVariable();
		fork1.setName("fork1");
		//fork1.setBoundingBox(new BoundingBox(new Bounds(5, 5), new Bounds(10, 10), new Bounds(14, 14), new Bounds(24, 24)));
		UnaryRectangleConstraint atFork1 = new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.At, new Bounds(5,5), new Bounds(10,10), new Bounds(14,14), new Bounds(24,24));
		atFork1.setFrom(fork1);
		atFork1.setTo(fork1);
		allConstraints.add(atFork1);
		
		RectangularRegion dish1 = (RectangularRegion)solver.createVariable();
		dish1.setName("dish1");
		//dish1.setBoundingBox(new BoundingBox(new Bounds(0, APSPSolver.INF), new Bounds(0, APSPSolver.INF), new Bounds(0, APSPSolver.INF), new Bounds(0, APSPSolver.INF)));
		
		RectangularRegion cup1 = (RectangularRegion)solver.createVariable();
		cup1.setName("cup1");
		//cup1.setBoundingBox(new BoundingBox(new Bounds(20, 20), new Bounds(28, 28), new Bounds(35, 35), new Bounds(42, 42)));
		UnaryRectangleConstraint atCup1 = new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.At, new Bounds(20,20), new Bounds(28,28), new Bounds(35,35), new Bounds(42,42));
		atCup1.setFrom(cup1);
		atCup1.setTo(cup1);
		allConstraints.add(atCup1);

		//..........................................................
		//T-BOX Constraints
		UnaryRectangleConstraint sizeDish = new UnaryRectangleConstraint(UnaryRectangleConstraint.Type.Size, new Bounds(10, 20), new Bounds(10, 20));
		sizeDish.setFrom(dish1);
		sizeDish.setTo(dish1);
		allConstraints.add(sizeDish);
		
		RectangleConstraint cupToDish = new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.During, AllenIntervalConstraint.Type.OverlappedBy), new AllenIntervalConstraint(AllenIntervalConstraint.Type.After));
		cupToDish.setFrom(cup);
		cupToDish.setTo(dish);
		allConstraints.add(cupToDish);

		
		RectangleConstraint knifetoDish = new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.After, new Bounds(4, 10)), new AllenIntervalConstraint(AllenIntervalConstraint.Type.During, AllenIntervalConstraint.Type.During.getDefaultBounds()));
		knifetoDish.setFrom(knife);
		knifetoDish.setTo(dish);
		allConstraints.add(knifetoDish);
		
		RectangleConstraint forktoDish = new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before), new AllenIntervalConstraint(AllenIntervalConstraint.Type.During));
		forktoDish.setFrom(fork);
		forktoDish.setTo(dish);
		allConstraints.add(forktoDish);
		
		//..........................................................
		//A-BOX to T-BOX Constraints
		RectangleConstraint napkinAssertion = new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.Equals), new AllenIntervalConstraint(AllenIntervalConstraint.Type.Equals));
		napkinAssertion.setFrom(napkin1);
		napkinAssertion.setTo(napkin);
		allConstraints.add(napkinAssertion);

		RectangleConstraint cupAssertion = new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.Equals), new AllenIntervalConstraint(AllenIntervalConstraint.Type.Equals));
		cupAssertion.setFrom(cup);
		cupAssertion.setTo(cup1);
		allConstraints.add(cupAssertion);

		
		RectangleConstraint knifAssertion = new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.Equals), new AllenIntervalConstraint(AllenIntervalConstraint.Type.Equals));
		knifAssertion.setFrom(knife);
		knifAssertion.setTo(knife1);
		allConstraints.add(knifAssertion);
		
		RectangleConstraint forkAssertion = new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.Equals), new AllenIntervalConstraint(AllenIntervalConstraint.Type.Equals));
		forkAssertion.setFrom(fork);
		forkAssertion.setTo(fork1);
		allConstraints.add(forkAssertion);

		RectangleConstraint dishAssertion = new RectangleConstraint(new AllenIntervalConstraint(AllenIntervalConstraint.Type.Equals), new AllenIntervalConstraint(AllenIntervalConstraint.Type.Equals));
		dishAssertion.setFrom(dish);
		dishAssertion.setTo(dish1);
		allConstraints.add(dishAssertion);
		
		Constraint[] allConstraintsArray = allConstraints.toArray(new Constraint[allConstraints.size()]);
		MetaCSPLogging.setLevel(Level.FINEST);
		if (!solver.addConstraints(allConstraintsArray)) { 
			System.out.println("Failed to add constraints!");
			System.exit(0);
		}
		
		//String[] st = new String[]{"fork", "knife", "dish", "cup"};
		System.out.println(solver.drawAlmostCentreRectangle(100, dish));
		//System.out.println(solver.drawMinMaxRectangle(30, st));

		ConstraintNetwork.draw(solver.getConstraintNetwork());

	}

}

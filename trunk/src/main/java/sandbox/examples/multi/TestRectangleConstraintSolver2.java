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
package sandbox.examples.multi;

import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import framework.Constraint;
import framework.ConstraintNetwork;

import multi.allenInterval.AllenIntervalConstraint;
import sandbox.spatial.rectangleAlgebra2.RectangleConstraint2;
import sandbox.spatial.rectangleAlgebra2.RectangleConstraintSolver2;
import sandbox.spatial.rectangleAlgebra2.RectangularRegion2;
import sandbox.spatial.rectangleAlgebra2.UnaryRectangleConstraint2;
import time.APSPSolver;
import time.Bounds;

public class TestRectangleConstraintSolver2 {

	/**
	 * @param args
	 */
	
	public static void main(String[] args) {
	
		RectangleConstraintSolver2 solver = new RectangleConstraintSolver2(0,1000);
		Vector<Constraint> allConstraints = new Vector<Constraint>();
		
		//..........................................................
		//T-BOX Variables
		RectangularRegion2 knife = (RectangularRegion2)solver.createVariable();		
		knife.setName("knife");
		
		RectangularRegion2 fork = (RectangularRegion2)solver.createVariable();
		fork.setName("fork");
		
		RectangularRegion2 dish = (RectangularRegion2)solver.createVariable();
		dish.setName("dish");
		
		RectangularRegion2 cup = (RectangularRegion2)solver.createVariable();
		cup.setName("cup");
		
		RectangularRegion2 napkin = (RectangularRegion2)solver.createVariable();
		napkin.setName("napkin");
		
		//..........................................................
		//A-BOX Variables and constraints
		RectangularRegion2 napkin1 = (RectangularRegion2)solver.createVariable();
		napkin1.setName("napkin1");
		//napkin1.setBoundingBox(new BoundingBox(new Bounds(0, APSPSolver.INF), new Bounds(0, APSPSolver.INF), new Bounds(0, APSPSolver.INF), new Bounds(0, APSPSolver.INF)));
		//NO needd for this, the bounds will be [0,infty) anyway:
		//UnaryRectangleConstraint2 atNapkin1 = new UnaryRectangleConstraint2(UnaryRectangleConstraint2.Type.At, new Bounds(0,APSPSolver.INF), new Bounds(0,APSPSolver.INF), new Bounds(0,APSPSolver.INF), new Bounds(0,APSPSolver.INF));
		//atNapkin1.setFrom(napkin1);
		//atNapkin1.setTo(napkin1);
		
		RectangularRegion2 knife1 = (RectangularRegion2)solver.createVariable();
		knife1.setName("knife1");
		//knife1.setBoundingBox(new BoundingBox(new Bounds(50, 50), new Bounds(55, 55), new Bounds(12, 12), new Bounds(26, 26)));
		UnaryRectangleConstraint2 atKnife1 = new UnaryRectangleConstraint2(UnaryRectangleConstraint2.Type.At, new Bounds(50,50), new Bounds(55,55), new Bounds(12,12), new Bounds(26,26));
		atKnife1.setFrom(knife1);
		atKnife1.setTo(knife1);
		allConstraints.add(atKnife1);
		
		RectangularRegion2 fork1 = (RectangularRegion2)solver.createVariable();
		fork1.setName("fork1");
		//fork1.setBoundingBox(new BoundingBox(new Bounds(5, 5), new Bounds(10, 10), new Bounds(14, 14), new Bounds(24, 24)));
		UnaryRectangleConstraint2 atFork1 = new UnaryRectangleConstraint2(UnaryRectangleConstraint2.Type.At, new Bounds(5,5), new Bounds(10,10), new Bounds(14,14), new Bounds(24,24));
		atFork1.setFrom(fork1);
		atFork1.setTo(fork1);
		allConstraints.add(atFork1);
		
		RectangularRegion2 dish1 = (RectangularRegion2)solver.createVariable();
		dish1.setName("dish1");
		//dish1.setBoundingBox(new BoundingBox(new Bounds(0, APSPSolver.INF), new Bounds(0, APSPSolver.INF), new Bounds(0, APSPSolver.INF), new Bounds(0, APSPSolver.INF)));
		
		RectangularRegion2 cup1 = (RectangularRegion2)solver.createVariable();
		cup1.setName("cup1");
		//cup1.setBoundingBox(new BoundingBox(new Bounds(20, 20), new Bounds(28, 28), new Bounds(35, 35), new Bounds(42, 42)));
		UnaryRectangleConstraint2 atCup1 = new UnaryRectangleConstraint2(UnaryRectangleConstraint2.Type.At, new Bounds(20,20), new Bounds(28,28), new Bounds(35,35), new Bounds(42,42));
		atCup1.setFrom(cup1);
		atCup1.setTo(cup1);
		allConstraints.add(atCup1);

		//..........................................................
		//T-BOX Constraints
		UnaryRectangleConstraint2 sizeDish = new UnaryRectangleConstraint2(UnaryRectangleConstraint2.Type.Size, new Bounds(10, 20), new Bounds(10, 20));
		sizeDish.setFrom(dish1);
		sizeDish.setTo(dish1);
		allConstraints.add(sizeDish);
		
		RectangleConstraint2 cupToDish = new RectangleConstraint2(new AllenIntervalConstraint(AllenIntervalConstraint.Type.During, AllenIntervalConstraint.Type.OverlappedBy), new AllenIntervalConstraint(AllenIntervalConstraint.Type.After));
		cupToDish.setFrom(cup);
		cupToDish.setTo(dish);
		allConstraints.add(cupToDish);

		
//		AugmentedRectangleConstraint knifetoDish = new AugmentedRectangleConstraint(new TwoDimensionsAllenConstraint(QualitativeAllenIntervalConstraint.Type.After,
//				QualitativeAllenIntervalConstraint.Type.During));
//		knifetoDish.setFrom(knife);
//		knifetoDish.setTo(dish);
//		allConstraints.add(knifetoDish);
		
		RectangleConstraint2 knifetoDish = new RectangleConstraint2(new AllenIntervalConstraint(AllenIntervalConstraint.Type.After, new Bounds(4, 10)), new AllenIntervalConstraint(AllenIntervalConstraint.Type.During, AllenIntervalConstraint.Type.During.getDefaultBounds()));
		knifetoDish.setFrom(knife);
		knifetoDish.setTo(dish);
		allConstraints.add(knifetoDish);
		
		RectangleConstraint2 forktoDish = new RectangleConstraint2(new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before), new AllenIntervalConstraint(AllenIntervalConstraint.Type.During));
		forktoDish.setFrom(fork);
		forktoDish.setTo(dish);
		allConstraints.add(forktoDish);
		
		//..........................................................
		//A-BOX to T-BOX Constraints
		RectangleConstraint2 napkinAssertion = new RectangleConstraint2(new AllenIntervalConstraint(AllenIntervalConstraint.Type.Equals), new AllenIntervalConstraint(AllenIntervalConstraint.Type.Equals));
		napkinAssertion.setFrom(napkin1);
		napkinAssertion.setTo(napkin);
		allConstraints.add(napkinAssertion);

		RectangleConstraint2 cupAssertion = new RectangleConstraint2(new AllenIntervalConstraint(AllenIntervalConstraint.Type.Equals), new AllenIntervalConstraint(AllenIntervalConstraint.Type.Equals));
		cupAssertion.setFrom(cup);
		cupAssertion.setTo(cup1);
		allConstraints.add(cupAssertion);

		
		RectangleConstraint2 knifAssertion = new RectangleConstraint2(new AllenIntervalConstraint(AllenIntervalConstraint.Type.Equals), new AllenIntervalConstraint(AllenIntervalConstraint.Type.Equals));
		knifAssertion.setFrom(knife);
		knifAssertion.setTo(knife1);
		allConstraints.add(knifAssertion);
		
		RectangleConstraint2 forkAssertion = new RectangleConstraint2(new AllenIntervalConstraint(AllenIntervalConstraint.Type.Equals), new AllenIntervalConstraint(AllenIntervalConstraint.Type.Equals));
		forkAssertion.setFrom(fork);
		forkAssertion.setTo(fork1);
		allConstraints.add(forkAssertion);

		RectangleConstraint2 dishAssertion = new RectangleConstraint2(new AllenIntervalConstraint(AllenIntervalConstraint.Type.Equals), new AllenIntervalConstraint(AllenIntervalConstraint.Type.Equals));
		dishAssertion.setFrom(dish);
		dishAssertion.setTo(dish1);
		allConstraints.add(dishAssertion);
		
		Constraint[] allConstraintsArray = allConstraints.toArray(new Constraint[allConstraints.size()]);
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

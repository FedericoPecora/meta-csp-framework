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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;

import org.metacsp.multi.activity.Activity;
import org.metacsp.multi.activity.ActivityNetworkSolver;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.symbols.SymbolicValueConstraint;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.multi.MultiConstraintSolver;

public class TestSerialization {

	public static void main(String[] args) {
		ActivityNetworkSolver solver = new ActivityNetworkSolver(0,100, new String[] {"A","B","C","D"});
		Activity act1 = (Activity)solver.createVariable();
		act1.setSymbolicDomain("A", "B", "C");
		Activity act2 = (Activity)solver.createVariable();
		act2.setSymbolicDomain("B", "C");
		
		MetaCSPLogging.setLevel(Level.FINEST);
//		MetaCSPLogging.setLevel(solver.getClass(), Level.FINEST);
//		MetaCSPLogging.setLevel(solver.getConstraintSolvers()[0].getClass(), Level.FINEST);

		SymbolicValueConstraint con1 = new SymbolicValueConstraint(SymbolicValueConstraint.Type.EQUALS);
		con1.setFrom(act1);
		con1.setTo(act2);
		//solver.addConstraint(con1);
		
		AllenIntervalConstraint con2 = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Before, new Bounds(10, 20));
		con2.setFrom(act1);
		con2.setTo(act2);
		//solver.addConstraint(con2);
		
		Constraint[] cons = new Constraint[]{con1,con2};
		solver.addConstraints(cons);
		
		//Dump ActivityNetwork network to file
		try {
			FileOutputStream fos = new FileOutputStream("ActivityNetwork.out");
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(solver.getConstraintNetwork());
			out.close();
			fos.close();
		}
		catch (FileNotFoundException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
		
		//Read ActivityNetwork from file
		ConstraintNetwork an = null;
		try {
			FileInputStream fis = new FileInputStream("ActivityNetwork.out");
			ObjectInputStream in = new ObjectInputStream(fis);
			an = (ConstraintNetwork)in.readObject();
			in.close();
			fis.close();
		}
		catch (FileNotFoundException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
		catch (ClassNotFoundException e) { e.printStackTrace(); }
		
		if (an != null) ConstraintNetwork.draw(an);

		
		//Dump AllenIntervalNetwork network to file
		try {
			FileOutputStream fos = new FileOutputStream("AllenIntervalNetwork.out");
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(solver.getConstraintSolvers()[0].getConstraintNetwork());
			out.close();
			fos.close();
		}
		catch (FileNotFoundException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
		
		//Read AllenIntervalNetwork from file
		ConstraintNetwork ain = null;
		try {
			FileInputStream fis = new FileInputStream("AllenIntervalNetwork.out");
			ObjectInputStream in = new ObjectInputStream(fis);
			ain = (ConstraintNetwork)in.readObject();
			in.close();
			fis.close();
		}
		catch (FileNotFoundException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
		catch (ClassNotFoundException e) { e.printStackTrace(); }
		
		if (ain != null) ConstraintNetwork.draw(ain);
		
		
		//Dump APSPNetwork network to file
		try {
			FileOutputStream fos = new FileOutputStream("SimpleTemporalNetwork.out");
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(((MultiConstraintSolver)solver.getConstraintSolvers()[0]).getConstraintSolvers()[0].getConstraintNetwork());
			out.close();
			fos.close();
		}
		catch (FileNotFoundException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
		
		//Read APSPNetwork from file
		ConstraintNetwork apspn = null;
		try {
			FileInputStream fis = new FileInputStream("SimpleTemporalNetwork.out");
			ObjectInputStream in = new ObjectInputStream(fis);
			apspn = (ConstraintNetwork)in.readObject();
			in.close();
			fis.close();
		}
		catch (FileNotFoundException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
		catch (ClassNotFoundException e) { e.printStackTrace(); }
		
		if (apspn != null) ConstraintNetwork.draw(apspn);

	}

}

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
package org.metacsp.time;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.JFrame;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Domain;
import org.metacsp.framework.ValueChoiceFunction;
import org.metacsp.framework.Variable;
import org.metacsp.throwables.ConstraintNotFound;
import org.metacsp.throwables.time.MalformedSimpleDistanceConstraint;
import org.metacsp.utility.UI.PlotSTPTemporalModule;
import org.metacsp.utility.logging.MetaCSPLogging;

/**
 * Simple Temporal Problem solver which uses the Floyd-Warshall
 * all-pairs shortest-path algorithm for constraint propagation.
 * Derived from original implementation by
 * the Planning and Scheduling Team (ISTC-CNR) under project APSI.
 * 
 * @author Federico Pecora and Planning and Scheduling Team
 */
public class APSPSolver extends ConstraintSolver {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5029122662268797937L;
	
	private boolean doFromScratchInsteadOfIncremental = false;
	private boolean addingIndependentConstraints = false;

	private boolean backupDMatrixSimple = false;
	private int cubePropCount = 0;
	private int quadPropCount = 0;

	/**
	 * Public access class variable denoting the value infinity.
	 */
	public static final long INF = Long.MAX_VALUE-1;

	public static final int DEFAULT_MAX_TPS = 2000;

	//MAX number of TPs in temporal network.
	private final int MAX_TPS;

	//All TimePoints in the STP
	private TimePoint[] tPoints = null;

	//Envelope of used timepoints so we can keep size of F-W loops small
	//without too much housekeeping.
	private int MAX_USED = 2;

	//Distance Matrix
	private long[][] distance;
	private long[][] distanceBackupInternal;
	private Vector<long[][]> backups = new Vector<long[][]>();
	private Vector<Constraint[]> backupConstraints = new Vector<Constraint[]>();
	private Vector<Integer> backupMaxUsed = new Vector<Integer>();

	// Roll-back data structures
	private ArrayList<TimePoint[]> tPointsRollback = new ArrayList<TimePoint[]>();
	private ArrayList<long[][]> distanceRollback = new ArrayList<long[][]>();	
	private ArrayList<Integer> maxUsedRollback = new ArrayList<Integer>();
	private ArrayList<ConstraintNetwork> networkRollback = new ArrayList<ConstraintNetwork>();

	//Temporal Horizon
	private long H;

	//Constraint between source (tPoints[0]) and sink (tPoints[1])
	private SimpleDistanceConstraint horizonConstraint;

	//Origin
	private long O;

	//Time Point Counter
	private int tpCounter = 0;


	//rigidity matrix
	private double[] rigidity;

	/**
	 * Create a new APSPSolver with given temporal horizon.
	 * @param origin The start time of the horizon.
	 * @param horizon The end time of the horizon. 
	 */
	public APSPSolver(long origin,long horizon) {
		this(origin, horizon, DEFAULT_MAX_TPS);
	}


	/**
	 * Create a new APSPSolver with given temporal horizon and given maximum number of timepoints.
	 * @param origin The start time of the horizon.
	 * @param horizon The end time of the horizon. 
	 * @param maxTPs maximum number of timepoints in the network 
	 * (excluding the Origin (O) and Horizon (H) timepoint)
	 * (propagation is O((maxTPs+2)^3)). 
	 */
	public APSPSolver (long origin, long horizon, int maxTPs) {
		super(new Class[]{SimpleDistanceConstraint.class}, TimePoint.class);
		this.setOptions(OPTIONS.MANUAL_PROPAGATE);
		this.MAX_TPS = maxTPs+2; //+2 To account for O and H
		tPoints = new TimePoint[MAX_TPS];
		distance = new long[MAX_TPS][MAX_TPS];


		//Init
		H = horizon;
		O = origin;
		
//		for (int i = 0; i < MAX_TPS; i++) {
//			for (int j = 0; j < MAX_TPS; j++) {
//				if (i == j) distance[i][j] = 0;
//				else distance[i][j] = APSPSolver.INF;
//			}
//		}

		for (int i = 0; i < MAX_TPS; i++) {
			if (i == 0) {
				for (int j = 1; j < MAX_TPS; j++) distance[i][j] = H;
			}
			else if (i == 1) {
				distance[i][0] = -H;
				for (int j = 1; j < MAX_TPS; j++) distance[i][j] = 0; 
			}
			else {
				distance[i][0] = 0;
				for (int j = 1; j < MAX_TPS; j++) {
					if (i == j) distance[i][j] = 0;
					else distance[i][j] = H;
				}
			}
		}

		//Edges between reference and horizon
		tPoints[0] = new TimePoint(tpCounter,MAX_TPS,this,O,H);
		tpCounter++;
		tPoints[1] = new TimePoint(tpCounter,MAX_TPS,this,O,H);
		tpCounter++;

		this.theNetwork.addVariable(tPoints[0]);
		this.theNetwork.addVariable(tPoints[1]);

		SimpleDistanceConstraint con = new SimpleDistanceConstraint();
		horizonConstraint = con;

		con.setFrom(this.getVariable(0));
		con.setTo(this.getVariable(1));
		con.setMinimum(H-O);
		con.setMaximum(H-O);
		con.addInterval(new Bounds(H-O,H-O));

		//[lb,ub] = [-di0,d0i]
		tPoints[0].setUsed(true);
		tPoints[0].setLowerBound(O);
		tPoints[0].setUpperBound(O);
		tPoints[1].setUsed(true);
		tPoints[1].setLowerBound(H);
		tPoints[1].setUpperBound(H);

		//Add edge to TimePoints
		tPoints[0].setOut(1,con);

		//Create all time points
		for (int i = 2; i < MAX_TPS; i++) {
			TimePoint tp = new TimePoint(tpCounter,MAX_TPS,this);
			tPoints[i] = tp;
			tpCounter++;

			//Create edge from Origin and Horizon to all TPs	
			SimpleDistanceConstraint conO = new SimpleDistanceConstraint();
			SimpleDistanceConstraint conH = new SimpleDistanceConstraint();
			conO.setFrom(this.getVariable(0));
			conO.setTo(this.getVariable(i));
			conH.setFrom(this.getVariable(i));
			conH.setTo(this.getVariable(1));

			conO.setMinimum(0);
			conO.setMaximum(H-O);
			conH.setMinimum(0);
			conH.setMaximum(H-O);

			conO.addInterval(new Bounds(0,H-O));
			conH.addInterval(new Bounds(0,H-O));

			//[lb,ub] = [-di0,d0i]
			tPoints[i].setLowerBound(O);
			tPoints[i].setUpperBound(H);

			//Add interval to TimePoints
			tPoints[0].setOut(i,conO);
			tPoints[i].setOut(1,conH);
		}
		
//		System.out.println("Init print:\n" + this.printDist());
	}

	//TP creation
	private int tpCreate() {
		logger.finest("Creating 1 TP");
		int i = 2;
		boolean found = false;
		while (i < MAX_TPS && !found) {
			if (!tPoints[i].isUsed()) {
				tPoints[i].setUsed(true);
				found = true;
				if (i == MAX_USED+1) MAX_USED = i;
			} else i++;
		}
		for (int l = 2; l <= MAX_USED; l++) {
			distance[i][l] = H;
			distance[l][i] = H;			
		}
		
		distance[i][i] = 0;
		distance[i][0] = 0;
		distance[i][1] = H;//APSPSolver.INF;
		
		distance[0][i] = H; // Needed for bookmark/revert (Uwe)
		distance[1][i] = 0; // Needed for bookmark/revert (Uwe)
		
		return i;
	}


	//Batch TP creation
	private int[] tpCreate(int n) {
		if (n > MAX_TPS) return null;
		int[] ret = new int[n];
		for (int i = 0; i < n; i++) ret[i] = tpCreate();
		return ret;
	}

	private void saveDMatrix(Constraint[] con) {
		long[][] distanceBackup = new long[MAX_USED+1][MAX_USED+1];
		for (int i = 0; i < MAX_USED+1; i++) {
			for (int j = 0; j < MAX_USED+1; j++) {
				distanceBackup[i][j] = distance[i][j];
			}
		}
		backupConstraints.add(con);
		backups.add(distanceBackup);
		backupMaxUsed.add(MAX_USED);
	}

	private void restoreDMatrix() {
		long[][] distanceBackup = backups.lastElement();
		for (int i = 0; i < backupMaxUsed.lastElement()+1; i++) {
			for (int j = 0; j < backupMaxUsed.lastElement()+1; j++) {
				distance[i][j] = distanceBackup[i][j];
			}			
		}
		backupConstraints.remove(backupConstraints.size()-1);
		backups.remove(backups.size()-1);
		backupMaxUsed.remove(backupMaxUsed.size()-1);
	}
	
	private void saveDMatrixInternal() {
		distanceBackupInternal = new long[MAX_USED+1][MAX_USED+1];
		for (int i = 0; i < MAX_USED+1; i++) {
			for (int j = 0; j < MAX_USED+1; j++) {
				distanceBackupInternal[i][j] = distance[i][j];
			}			
		}
	}

	private void restoreDMatrixInternal() {
		for (int i = 0; i < MAX_USED+1; i++) {
			for (int j = 0; j < MAX_USED+1; j++) {
				distance[i][j] = distanceBackupInternal[i][j];
			}			
		}
		distanceBackupInternal = null;
	}


	//Batch Time point erase
	private void tpDelete(int[] IDtimePoint) {
		logger.finest("Deleting " + IDtimePoint.length + " TP");
		for (int i = 0; i < IDtimePoint.length; i++) {
			tPoints[IDtimePoint[i]].setUsed(false);

			if (IDtimePoint[i] == MAX_USED) MAX_USED--;

			SimpleDistanceConstraint conO = new SimpleDistanceConstraint();
			SimpleDistanceConstraint conH = new SimpleDistanceConstraint();
			conO.setFrom(this.getVariable(0));
			conO.setTo(this.getVariable(IDtimePoint[i]));
			conH.setFrom(this.getVariable(IDtimePoint[i]));
			conH.setTo(this.getVariable(1));

			conO.setMinimum(0);
			conO.setMaximum(H-O);
			conH.setMinimum(0);
			conH.setMaximum(H-O);

			conO.addInterval(new Bounds(0,H-O));
			conH.addInterval(new Bounds(0,H-O));

			//[lb,ub] = [-di0,d0i]
			tPoints[IDtimePoint[i]].setLowerBound(O);
			tPoints[IDtimePoint[i]].setUpperBound(H);

			tPoints[0].setOut(IDtimePoint[i],conO);
			tPoints[IDtimePoint[i]].setOut(1,conH);
		}

		fromScratchDistanceMatrixComputation();
	}

	//Create an interval for a constraint
	private boolean cCreateFromScratch(Bounds i, int from, int to) {
		//Conversion
		long max = i.max;
		long min = i.min;
		if (i.max == APSPSolver.INF) max = H-O;
		if (i.min == -APSPSolver.INF) min = -1 * (H - O);
		i = new Bounds(min,max);

		// Checks
		if (i.min > i.max) return false; 
		if (from == to) return false;
		if (tPoints[from] == null) return false;

		//Already existing edge
		SimpleDistanceConstraint con = tPoints[from].getOut(to);
		if (con != null) {
			//check intersection between active con and new con
			if ( (con.getMinimum() > i.max) || (con.getMaximum() < i.min) ) return false;
			//check con does not contain active con
			if ( (con.getMinimum() > i.min) && (con.getMaximum() < i.max) ) {
				//OK it is. I save con without doing anything else
				if (!con.addInterval(i)) return false;
				return true;
			}
			//Update active con
			long oldd = con.getMinimum();
			long oldD = con.getMaximum();
			if (con.getMinimum() < i.min) con.setMinimum(i.min);
			if (con.getMaximum() > i.max) con.setMaximum(i.max);

			if (!con.addInterval(i)) return false;
			
			if (backupDMatrixSimple) saveDMatrixInternal();
			if (!fromScratchDistanceMatrixComputation()) {
				//Inconsistency. Rollback
				con.removeInterval(i);
				if (backupDMatrixSimple) restoreDMatrixInternal();
				return false;
			}

			//[lb,ub] = [-di0,d0i]
			for (int j = 0; j < MAX_USED+1; j++)
				if (tPoints[j].isUsed()) {
					tPoints[j].setLowerBound(sum(-distance[j][0],O));
					tPoints[j].setUpperBound(sum(distance[0][j],O));
				}
		}				
		else {
			con = new SimpleDistanceConstraint();
			con.setFrom(this.getVariable(from));
			con.setTo(this.getVariable(to));
			con.setMinimum(i.min);
			con.setMaximum(i.max);
			con.addInterval(new Bounds(i.min,i.max));
			//Add edge to tp
			tPoints[from].setOut(to,con);

			if (backupDMatrixSimple) saveDMatrixInternal();
			if (!fromScratchDistanceMatrixComputation()) {
				tPoints[from].setOut(to,null);
				if (backupDMatrixSimple) restoreDMatrixInternal();
				return false;
			}

			//[lb,ub] = [-di0,d0i]
			for (int j = 0; j < MAX_USED+1; j++) {
				if (tPoints[j].isUsed() == true) {
					tPoints[j].setLowerBound (sum(-distance[j][0],O));
					tPoints[j].setUpperBound (sum(distance[0][j],O));
				}
			}
		}
		return true;	
	}

	
	//Create an interval for a constraint
	private boolean cCreate(Bounds i, int from, int to, boolean noPropagation) {
		if (!noPropagation && doFromScratchInsteadOfIncremental) return cCreateFromScratch(i, from, to);
		//Conversion
		long max = i.max;
		long min = i.min;
		if (i.max == APSPSolver.INF) max = H-O;
		if (i.min == -APSPSolver.INF) min = -1 * (H - O);
		i = new Bounds(min,max);

		// Checks
		if (i.min > i.max) return false; 
		if (from == to) return false;
		if (tPoints[from] == null) return false;

		if (noPropagation) {
			distance[from][to] = max;
			distance[to][from] = -min;
		}
		
		//Already existing edge
		SimpleDistanceConstraint con = tPoints[from].getOut(to);
		if (con != null) {
			//check intersection between active con and new con
			if ( (con.getMinimum() > i.max) || (con.getMaximum() < i.min) ) return false;
			//check con does not contain active con
			if ( (con.getMinimum() > i.min) && (con.getMaximum() < i.max) ) {
				//OK it is. I save con without doing anything else
				if (!con.addInterval(i)) return false;
				return true;
			}
			//Update active con
			long oldd = con.getMinimum();
			long oldD = con.getMaximum();
			if (con.getMinimum() < i.min) con.setMinimum(i.min);
			if (con.getMaximum() > i.max) con.setMaximum(i.max);

//			saveDMatrix();
			if (!noPropagation && !incrementalDistanceMatrixComputation(from,to,i)) {
				//Inconsistency. Rollback
				con.setMinimum(oldd);
				con.setMaximum(oldD);
//				restoreDMatrix();
				return false;
			}

			//Ok update
			if (!con.addInterval(i)) return false;

			//[lb,ub] = [-di0,d0i]
			for (int j = 0; j < MAX_USED+1; j++)
				if (tPoints[j].isUsed()) {
					tPoints[j].setLowerBound(sum(-distance[j][0],O));
					tPoints[j].setUpperBound(sum(distance[0][j],O));
				}
		}				
		else {
//			saveDMatrix();
			if (!noPropagation && !incrementalDistanceMatrixComputation(from,to,i)) {
//				restoreDMatrix();
				return false;
			}
			//Ok no inconsistency
			con = new SimpleDistanceConstraint();
			con.setFrom(this.getVariable(from));
			con.setTo(this.getVariable(to));
			con.setMinimum(i.min);
			con.setMaximum(i.max);
			con.addInterval(new Bounds(i.min,i.max));

			//Add edge to tp
			tPoints[from].setOut(to,con);

			//[lb,ub] = [-di0,d0i]
			for (int j = 0; j < MAX_USED+1; j++) {
				if (tPoints[j].isUsed() == true) {
					tPoints[j].setLowerBound (sum(-distance[j][0],O));
					tPoints[j].setUpperBound (sum(distance[0][j],O));
				}
			}
		}
		return true;	
	}

    //Batch create intervals (for many constraints)
	private boolean cCreate(Bounds[] in, int[] from, int[] to) {
		long[] old_d = new long[in.length];
		long[] old_D = new long[in.length];
		boolean[] added = new boolean[in.length];
		for (int i = 0; i < added.length; i++) added[i] = false;
		boolean rollback = false;
		int rollBackPoint = -1;

		for (int i = 0; i < in.length; i++) {
			//Conversion
			long min = in[i].min;
			long max = in[i].max;
			if (in[i].max == APSPSolver.INF) max = H-O;
			if (in[i].min == -APSPSolver.INF) min = -1 * (H - O);
			in[i] = new Bounds(min,max);
			//Checks
			if (in[i].min > in[i].max) { rollback = true; rollBackPoint = i; break; /*return false;*/ }
			if (from[i] == to[i]) { rollback = true; rollBackPoint = i; break; /*return false;*/ }

			SimpleDistanceConstraint con = tPoints[from[i]].getOut(to[i]);
			if (con != null) {
				//Already existing edge
				//check intersection between active con and new con
				//added[i] = false;
				if ( (con.getMinimum() > in[i].max) || (con.getMaximum() < in[i].min) ) { rollback = true; rollBackPoint = i; break; /*return false;*/ }
				//Update active con
				old_d[i] = con.getMinimum();
				old_D[i] = con.getMaximum();
				if (con.getMinimum() < in[i].min) con.setMinimum(in[i].min);
				if (con.getMaximum() > in[i].max) con.setMaximum(in[i].max);
			} 
			else {          
				//Non-existing arc
				//Adding tentative constraint
				added[i] = true;
				con = new SimpleDistanceConstraint();
				con.setFrom(this.getVariable(from[i]));
				con.setTo(this.getVariable(to[i]));
				con.setMinimum(in[i].min);
				con.setMaximum(in[i].max);
				con.addInterval(new Bounds(in[i].min,in[i].max));
				tPoints[from[i]].setOut(to[i], con);
			}
		}

		if (rollback) {
			for (int i = rollBackPoint-1; i >= 0; i--) {
				SimpleDistanceConstraint con = tPoints[from[i]].getOut(to[i]);
				if (!added[i]) {
					//Rollback in case of already existing edge
					con.setMinimum(old_d[i]);
					con.setMaximum(old_D[i]);
				}
				else {
					//Rollback in case of new edge
					con.removeInterval(in[i]);
					tPoints[from[i]].setOut(to[i], null);
				}
			}
			return false;
		}

		if (backupDMatrixSimple) saveDMatrixInternal();
		if (!this.fromScratchDistanceMatrixComputation()) {
			if (backupDMatrixSimple) restoreDMatrixInternal();
			return false;
		}
		
		//Ok update
		for (int i = in.length-1; i >= 0; i--) {
			if (!added[i]) {
				SimpleDistanceConstraint con = tPoints[from[i]].getOut(to[i]);
				con.addInterval(in[i]);
			}
		}

		//[lb,ub] = [-di0,d0i]
		for (int j = 0; j < MAX_USED+1; j++)
			if (tPoints[j].isUsed() == true) {
				tPoints[j].setLowerBound(sum(-distance[j][0],O));
				tPoints[j].setUpperBound(sum(distance[0][j],O));
			}
		
		return true;    
	}
	
//	//Delete a constraint...
//	//throw error in case of parameter inconsistency
//	private boolean cDelete(Bounds i, int from, int to) throws ConstraintNotFound, MalformedSimpleDistanceConstraint {
//		//Conversion
//		long min = i.min;
//		long max = i.max;
//		if (i.max == Long.MAX_VALUE - 1) max = H-O;
//		if (i.min == Long.MIN_VALUE + 1) min = -1 * (H - O);
//		i = new Bounds(min,max);
//
//		SimpleDistanceConstraint con = tPoints[from].getOut(to);
//
//		if (con == null) throw new ConstraintNotFound(String.format("Interval %s, from %d, to %d", i.toString(), from, to));
//
//		if (con.getCounter() == 1) { 
//			if (con.removeInterval(i)) {
//				tPoints[from].setOut(to,null);
//			}
//			else throw new MalformedSimpleDistanceConstraint(con, 3);
//		}
//		else if (!con.removeInterval(i)) throw new MalformedSimpleDistanceConstraint(con, 4);
//
//		fromScratchDistanceMatrixComputation();
//
//		for (int j = 0; j < MAX_USED+1; j++)
//			if (tPoints[j].isUsed()) {
//				tPoints[j].setLowerBound(sum(-distance[j][0],O));
//				tPoints[j].setUpperBound(sum(distance[0][j],O));
//			}
//
//		return true;
//	}

	//Delete many constraints...
	//throw error in case of parameter inconsistency
	private boolean cDelete(Bounds[] in, int[] from, int[] to, boolean canRestore) throws ConstraintNotFound, MalformedSimpleDistanceConstraint {
		for (int i = 0; i < in.length; i++) {
			//Conversion
			long min = in[i].min;
			long max = in[i].max;
			if (in[i].max == Long.MAX_VALUE - 1) max = H-O;
			if (in[i].min == Long.MIN_VALUE + 1) min = -1 * (H - O);
			in[i] = new Bounds(min,max);

			SimpleDistanceConstraint con = tPoints[from[i]].getOut(to[i]);

			if (con == null) {
				throw new ConstraintNotFound(String.format("Interval %s, from %d, to %d", in[i].toString(), from[i], to[i]));
			}

			if (con.getCounter() == 1) { 
				if (con.removeInterval(in[i])) tPoints[from[i]].setOut(to[i],null);
				else throw new MalformedSimpleDistanceConstraint(con, 1);
			}
			else if (!con.removeInterval(in[i])) throw new MalformedSimpleDistanceConstraint(con, 2);
		}

		if (!canRestore) fromScratchDistanceMatrixComputation();
		else {
			logger.finest("QuickRestoring distance matrix, no propagation");
			if (backupDMatrixSimple) restoreDMatrix();
		}

		for (int j = 0; j < MAX_USED+1; j++)
			if (tPoints[j].isUsed() == true) {
				tPoints[j].setLowerBound(sum(-distance[j][0],O));
				tPoints[j].setUpperBound(sum(distance[0][j],O));
			}

		return true;
	}


	//Access methods

	/**
	 * Get a timepoint given its ID.
	 * @param Id The ID of the timepoint.
	 * @return The timepoint referenced by passed ID. 
	 */
	public TimePoint getTimePoint(int Id) {
		if (Id >= MAX_TPS) return null;
		if (tPoints[Id] == null) return null;
		if (!tPoints[Id].isUsed()) return null;
		return tPoints[Id];
	}


	//Printing functions

	/**
	 * Generates a String representation of the Simple Temporal Network (STN).
	 * @return A String describing the STN.
	 */
	@Override
	public String toString() {
		StringBuilder strb = new StringBuilder();
		strb.append("Temporal Network (" + MAX_TPS +" time points): \n");
		for (int i = 0; i < MAX_TPS; i++) {
			if (tPoints[i].isUsed()) {
				strb.append(tPoints[i] + "\n");
			}
		}
		return strb.toString();
	}

	//"from scratch" re-computation
	private boolean fromScratchDistanceMatrixComputation() {
		logger.fine("Propagating (cube) with (#TPs,#cons) = (" + this.MAX_USED + "," + this.theNetwork.getConstraints().length + ") (call num.: " + (++cubePropCount) + ")");
		//*
		//This code is not tested thoroughly but seems to work
		for (int i = 0; i < MAX_USED+1; i++) {
			for (int j = i; j < MAX_USED+1; j++) {
				if (i != j) {
					long dij = H;//APSPSolver.INF;
					long dji = H;//APSPSolver.INF;

					if(tPoints[i].getOut(j) != null) {
						dij = Math.min(dij, +tPoints[i].getOut(j).getMaximum());
						dji = Math.min(dji, -tPoints[i].getOut(j).getMinimum());
					}
					if(tPoints[j].getOut(i) != null) {
						dij = Math.min(dij, -tPoints[j].getOut(i).getMinimum());
						dji = Math.min(dji, +tPoints[j].getOut(i).getMaximum());
					}

					if(-dji > +dij) {
						return false;
					}

					distance[i][j] = dij;
					distance[j][i] = dji;

				}
				else distance[i][j] = 0;
			}
		}

		for (int k = 0; k < MAX_USED+1; k++) {
			if (tPoints[k].isUsed() == true) {
				for (int i = 0; i < MAX_USED+1; i++) {
					if (tPoints[i].isUsed() == true) {
						for (int j = 0; j < MAX_USED+1; j++) { 
							if (tPoints[j].isUsed() == true) {
								long temp = sum(distance[i][k],distance[k][j]);
								if (distance[i][j] > temp)
									distance[i][j] = temp;
							}
							if (i == j && distance[i][j] < 0) return false;
						}
					}
				}
			}
		}

		return true;
	} 

	//Gd graph propagation function
	private boolean incrementalDistanceMatrixComputation(int from,int to,Bounds i) {
		logger.fine("Propagating (quad) with (#TPs,#cons) = (" + this.MAX_USED + "," + this.theNetwork.getConstraints().length + ") (call num.: " + (++quadPropCount) + ")");

		if (distance[to][from] != APSPSolver.INF && sum(i.max,distance[to][from]) < 0) return false;
		if (distance[from][to] != APSPSolver.INF && sum(-i.min,distance[from][to]) < 0) return false;
		
//		System.out.println("a)" + sum(i.max,distance[to][from]));
//		System.out.println("b)" + sum(-i.min,distance[from][to]));
		
		long sum1;
		long sum2;
		long sum3;
		long sum4;
		long temp;
		
		for (int u = 0; u < MAX_USED+1; u++) {
			if (tPoints[u].isUsed()) {
				for (int v = 0; v < MAX_USED+1;v++) {
					if (tPoints[v].isUsed()) {
						//min{distance[u][v];(distance[u][from]+i.max+distance[to][v]);(distance[u][to]-i.minl+distance[from][v])}
						sum1 = sum(distance[u][to],-i.min);
						sum2 = sum(sum1,distance[from][v]);
						sum3 = sum(distance[u][from],i.max);
						sum4 = sum(sum3,distance[to][v]);
						temp = Math.min(sum2,sum4);
												
						if (distance[u][v] > temp) {
							//long oldD = distance[u][v];
							distance[u][v] = temp;
							if (u == v && distance[u][v] != 0) {
								//logger.info("==================> Updated distance[" + u + "][" + v + "] from " + oldD + " to " + temp);
								//throw new Error("Found negative cycle in incremental propagation while adding (from,to,i) (" + from + "," + to + "," + i + ")");
								return false;
							}
						}
					}
				}
			}
		}
		return true;
	}


	//Interface to framework classes 

	//Create many new variables (batch) - i.e., many timepoints.
	@Override
	protected Variable[] createVariablesSub(int num) {
		int[] tp = tpCreate(num);
		Variable[] ret = new Variable[num];
		for (int i = 0; i < tp.length; i++) {
			ret[i] = tPoints[tp[i]];			
		}
		return ret;
	}


	//Remove many variables (timepoints).
	@Override
	protected void removeVariablesSub(Variable[] ti) {
		int[] Ids = new int[ti.length];
		for (int i = 0; i < ti.length;i ++) 
		{
			if (ti[i] instanceof TimePoint) {
				Ids[i] = ((TimePoint)ti[i]).getID();
			}
		}
		//super.removeVariable(ti);
		tpDelete(Ids);
	}
	
	public void setAddingIndependentConstraints() {
		addingIndependentConstraints = true;
	}
	
	@Override
	protected boolean addConstraintsSub(Constraint[] con) {
		if (con == null || con.length == 0) return true;
		if (backupDMatrixSimple && backupConstraints.size() > 50) resetDMatrixBackups();
		Bounds[] tot = new Bounds[con.length];
		int[] from = new int[con.length];
		int[] to = new int[con.length];

		for (int i = 0; i < con.length; i++) {
			if (con[i] instanceof SimpleDistanceConstraint) {
				SimpleDistanceConstraint c = (SimpleDistanceConstraint)con[i];
				tot[i] = new Bounds(c.getMinimum(),c.getMaximum());                             
				from[i] = ((TimePoint)c.getFrom()).getID();
				to[i] = ((TimePoint)c.getTo()).getID();
			}
		}

		logger.finest("Trying to add constraints " + Arrays.toString(con) + "...");
		Vector<Constraint> added = new Vector<Constraint>();

		if (backupDMatrixSimple) saveDMatrix(con);
		
		if (addingIndependentConstraints) {
			addingIndependentConstraints = false;
			//Do special adding
			for (int i = 0; i < con.length; i++) {
				//add cons, one shot
				if (!cCreate(tot[i],from[i],to[i],true)) {
					logger.info("Critical error in new constraint propagation!");
					return false;
				}
			}
			return true;
		}
		
		if (con.length > MAX_USED) {
			logger.finest("From scratch prop is more convenient (MAX_USED = " + MAX_USED + " < " + con.length + " = #constraintsToAdd)...");
			return cCreate(tot, from, to);
		}
		
		logger.finest("Incremental prop is more convenient (MAX_USED = " + MAX_USED + " >= " + con.length + " = #constraintsToAdd)...");
		ConstraintNetwork cn = new ConstraintNetwork(null);
		for (int i = 0; i < con.length; i++) {
			//System.out.println("TOT: " + tot[i] + " FROM: " + printLong(from[i]) + " TO: " + printLong(to[i]));
			if (cCreate(tot[i],from[i],to[i],false)) {
				added.add(con[i]);
			}
			else {
				logger.finest("Failed to add " + con[i]);
				Bounds[] toDeleteBounds = new Bounds[added.size()];
				int[] toDeleteFrom = new int[added.size()];
				int[] toDeleteTo = new int[added.size()];
				for (int j = 0; j < added.size(); j++) {
					toDeleteBounds[j] = new Bounds(((SimpleDistanceConstraint)added.get(j)).getMinimum(),((SimpleDistanceConstraint)added.get(j)).getMaximum());
					toDeleteFrom[j] = ((TimePoint)((SimpleDistanceConstraint)added.get(j)).getFrom()).getID();
					toDeleteTo[j] = ((TimePoint)((SimpleDistanceConstraint)added.get(j)).getTo()).getID();
				}
				cDelete(toDeleteBounds, toDeleteFrom, toDeleteTo, true);
				return false;
			}
		}
		
		return true;
	}

	private void resetDMatrixBackups() {
		backups = new Vector<long[][]>();
		backupConstraints = new Vector<Constraint[]>();
		backupMaxUsed = new Vector<Integer>();
	}
	
	private boolean canRestoreDMatrix(Constraint[] con) {
		if (!backupDMatrixSimple) return false;
		if (backupConstraints.isEmpty()) return false;
		for (Constraint c : backupConstraints.lastElement()) {
			boolean found = false;
			for (Constraint c1 : con) {
				if (c1.equals(c)) {
					found = true;
					break;
				}
			}
			if (!found) return false;
		}
		return true;
	}

	//Remove a constraint (SimpleDistanceConstraint)
	@Override
	protected void removeConstraintsSub(Constraint[] con) {
		logger.finest("Trying to remove constraints " + Arrays.toString(con) + "...");
		if (con != null && con.length != 0) {
			Bounds[] tot = new Bounds[con.length];
			int[] from = new int[con.length];
			int[] to = new int[con.length];

			for (int i = 0; i < con.length; i++) {
				if (con[i] instanceof SimpleDistanceConstraint) {
					SimpleDistanceConstraint c = (SimpleDistanceConstraint)con[i];
					tot[i] = new Bounds(c.getMinimum(),c.getMaximum());
					from[i] = ((TimePoint)c.getFrom()).getID();
					to[i] = ((TimePoint)c.getTo()).getID();				
				}
			}
			if (canRestoreDMatrix(con)) cDelete(tot,from,to,true);
			else {
				if (backupDMatrixSimple) resetDMatrixBackups();
				cDelete(tot,from,to,false);
			}
		}
	}

	/**
	 * Perform complete propagation.  Computational cost is O(n^3) where n is the number of
	 * timepoints in the Simple Temporal Network.
	 * @return {@code True} if propagation was successful, {@code False} otherwise.
	 */
	@Override
	public boolean propagate(){
		return fromScratchDistanceMatrixComputation();
	}

	/**
	 * Get the time origin of this {@link APSPSolver}. 
	 * @return The time origin of this {@link APSPSolver}.
	 */
	public long getO() {
		return O;
	}

	/**
	 * Get the horizon of this {@link APSPSolver}. 
	 * @return The horizon of this {@link APSPSolver}.
	 */
	public long getH() {
		return H;
	}

	/**
	 * Set a new horizon for this network.
	 * @param val The new horizon.
	 * @return {@code True} If the operation succeeded, {@code False} otherwise.
	 */
	public boolean changeHorizon(long val)
	{
		this.removeConstraint(horizonConstraint);
		SimpleDistanceConstraint sdc = new SimpleDistanceConstraint();
		sdc.setFrom(this.getVariable(0));
		sdc.setTo(this.getVariable(1));
		sdc.setMinimum(val);
		sdc.setMaximum(val);
		if (this.addConstraint(sdc)) {
			this.H = val;
			horizonConstraint = sdc;
			return true;
		}
		return false;
	}

	/**
	 * Get the source timepoint of the Simple Temporal Network (anchored to origin of time).
	 * @return the source timepoint of the Simple Temporal Network.
	 */
	public TimePoint getSource() {
		return tPoints[0];
	}

	/**
	 * Get the sink timepoint of the Simple Temporal Network (anchored to horizon).
	 * @return the sink timepoint of the Simple Temporal Network.
	 */
	public TimePoint getSink() {
		return tPoints[1];
	}

	/**
	 * Class method for pretty-printing longs (substitutes +/-INF appropriately). 
	 * @param l The long value to pretty-print.
	 * @return a String representation of the given long value. 
	 */
	public static String printLong(long l) {
		if (l >= 0) return "" + ((l == APSPSolver.INF ? "INF" : l));
		return "" + (-l == APSPSolver.INF ? "-INF" : l);
	}

	/**
	 * Get active constraint between two {@link TimePoint}s.
	 * @param tpFrom The source {@link TimePoint}.
	 * @param tpTo The destination {@link TimePoint}.
	 * @return The active {@link SimpleDistanceConstraint} between the
	 * two {@link TimePoint}s (<code>null</code> if none exists).
	 */
	public SimpleDistanceConstraint getConstraint(TimePoint tpFrom, TimePoint tpTo) {
		if (this.distance[tpFrom.getID()][tpTo.getID()] != INF)
			return tPoints[tpFrom.getID()].getOut(tpTo.getID());
		return null;
	}

	/**
	 * Gets the effective bounds between a pair of {@link TimePoint}s.
	 * (After propagation, considering all constraints in the network)
	 */
	public Bounds getDistanceBounds(TimePoint tpFrom, TimePoint tpTo) {
		final long max = distance[tpFrom.getID()][tpTo.getID()];
		final long min = -distance[tpTo.getID()][tpFrom.getID()];
		return new Bounds(min, max);
	}

	/**
	 * @return The maximum number of timepoints that can be added to this STP network
	 * (excluding the Origin (O) and Horizon (H) timepoint).
	 * @see APSPSolver#APSPSolver(long, long, int)
	 */
	public int getMaxTps() {
		//Subtract 2 (O and H) 
		return this.MAX_TPS - 2;
	}

	/**
	 * Draw a graph representing this {@link APSPSolver}'s {@link ConstraintNetwork}.  This method depends on the Prefuse library.
	 */
	public void draw() {
		//=================================================================
		//Now plot the STP with the Utility.PlotSTPTemporalModule class
		//=================================================================
		PlotSTPTemporalModule view;
		Toolkit tk = Toolkit.getDefaultToolkit();
		int xSize = ((int) tk.getScreenSize().getWidth());
		int ySize = ((int) tk.getScreenSize().getHeight());
		//int ySize = 500;

		//GIVE tm TO VISIUALIZER		
		view = new PlotSTPTemporalModule(this, xSize, ySize);
		JFrame myFrame = new JFrame("Simple Temporal Network");

		Container pane = myFrame.getContentPane();
		pane.setLayout(new BorderLayout());
		pane.add(view, BorderLayout.NORTH);
		myFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		myFrame.setSize(xSize,ySize);

		myFrame.pack();
		myFrame.setVisible(true);

		view.touchLBUBNodes();
		//=================================================================
		//End STP plotting
		//=================================================================
	}
	/**
	 * This method computes the root mean square rigidity of a consistent STN (the inverse concept of flexibility of a STN).
	 * If the STN is completely rigid, then its rigidity is 1. If the STN has no constraints, its rigidity is 0.
	 * This measure in proposed in [Luke Hunsberger, 2002]. 
	 * @return Root mean square rigidity of a consistent STN.
	 */
	public double getRMSRigidity(){

		rigidity = new double[this.getVariables().length];
		for (int i = 0; i < this.getVariables().length; i++) {
			if ( ((TimePoint)this.getVariables()[i]).isUsed() ) {
			rigidity[i] = (
					((double)1 / 
							((double)(1 + ((TimePoint)this.getVariables()[i]).getUpperBound() - ((TimePoint)this.getVariables()[i]).getLowerBound()))
							));
			//System.out.println(i + " " + j + " -> " + distance[this.getVariables()[i].getID()][this.getVariables()[j].getID()]);
			//System.out.println(i + " " + j + " -> " + rigidity[i][j]);
			}
		}
		double sigma = 0;
		for (int i = 0; i < this.getVariables().length; i++) {
			if ( ((TimePoint)this.getVariables()[i]).isUsed() ) {
				sigma += Math.pow(rigidity[i], 2.0);
			}
		}			

		return Math.sqrt(((double)sigma) * ((double)2/(this.getVariables().length * (this.getVariables().length + 1))));
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		logger = MetaCSPLogging.getLogger(this.getClass());
	}

	public int bookmark() {
		logger.fine("Bookmark #"+this.distanceRollback.size()+" MAX_USED="+this.MAX_USED);
		
		long[][] distanceSnapshot = new long[this.distance.length][this.distance[0].length];
		TimePoint[] tPointSnapshot = new TimePoint[this.tPoints.length];

		for ( int i = 0 ; i < tPoints.length ; i++ ) {
			TimePoint clone = tPoints[i].clone();
			tPointSnapshot[i] = clone;
		}		


//		for ( int i = 0 ; i < this.MAX_TPS ; i++ ) {
//			for ( int j = 0 ; j < this.MAX_TPS ; j++ ) {
//				distanceSnapshot[i][j] = distance[i][j];
//			}
//		}
		
		for ( int i = 0 ; i < this.MAX_USED+1 ; i++ ) {
			for ( int j = 0 ; j < this.MAX_USED+1 ; j++ ) {
				distanceSnapshot[i][j] = distance[i][j];
			}
		}

		distanceRollback.add(distanceSnapshot);
		tPointsRollback.add(tPointSnapshot);
		maxUsedRollback.add( new Integer(this.MAX_USED) );

		return distanceRollback.size()-1;
	}

	public void removeBookmark( int i ) {
		this.distanceRollback.remove(i);
		this.tPointsRollback.remove(i);
		this.maxUsedRollback.remove(i);
	}

	public void revert( int i ) {		
		this.distance = this.distanceRollback.get(i);	
		this.tPoints = this.tPointsRollback.get(i);
		this.MAX_USED = this.maxUsedRollback.get(i).intValue();

		for ( int j = this.distanceRollback.size()-1 ; j >= i ; j-- ) {
			this.distanceRollback.remove(j);
			this.tPointsRollback.remove(j);
			this.maxUsedRollback.remove(j);
		}
		logger.fine("Reverting to #"+this.distanceRollback.size()+" MAX_USED="+this.MAX_USED);
	}

	public int numBookmarks() {
		return this.distanceRollback.size();
	}

	public TimePoint getEqualTimePoint( TimePoint queryTp ) {
		for ( TimePoint tp : this.tPoints ) {
			if ( tp.equals(queryTp) ) {
				return tp;
			}
		}
		return null;
	}

	public String printDist() {
		String s = "";
		for ( int i = 0 ; i < this.MAX_USED+1; i++ ) {
			for ( int j = 0 ; j < this.MAX_USED+1; j ++ ) {
				s +=  printLong(distance[i][j]) + " ";
			}
			s += "\n";
		}
		return s;
	}

	private static long sum(long a, long b) {
		if (a == APSPSolver.INF || b == APSPSolver.INF) return APSPSolver.INF;
		return a+b;
	}

	public String printDistHist() {
		String s = "";
		int ci = 0;
		for ( long[][] distance : this.distanceRollback ) {
			s += "=============================\n";
			s += "= " + (ci++) + "\n";
			s += "=============================\n";
			for ( int i = 0 ; i < this.MAX_USED+1 ; i++ ) {
				for ( int j = 0 ; j < this.MAX_USED+1 ; j ++ ) {
					s +=  printLong(distance[i][j]) + " ";
				}
				s += "\n";
			}
		}
		return s;
	}

	@Override
	public void registerValueChoiceFunctions() {
	    ValueChoiceFunction startFunction = new ValueChoiceFunction() {
			@Override
			public Object getValue(Domain dom) {
				return ((Interval)dom).getBounds().min;
			}
	    };
	    ValueChoiceFunction endFunction = new ValueChoiceFunction() {
			@Override
			public Object getValue(Domain dom) {
				return ((Interval)dom).getBounds().max;
			}
	    };
		Domain.registerValueChoiceFunction(Interval.class, startFunction, "ET");
		Domain.registerValueChoiceFunction(Interval.class, endFunction, "LT");
	}
}

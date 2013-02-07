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
package multi.activity;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import utility.UI.PlotBoxTLSmall;
import framework.Variable;

public abstract class Timeline {
	
	private ActivityNetwork an;

	private Long[] pulses = null;
	private Long[] durations = null;
	protected String component;
	
	public Timeline(ActivityNetworkSolver ans, String component) {
		this.an = (ActivityNetwork)ans.getConstraintNetwork();
		this.component = component;
		this.pulses = null;
		this.durations = null;
		this.computePulses();	
		this.computeDurations();
	}
	
	public String getComponent() { return component; }
	
	private void computeDurations() {
		if (pulses.length == 0) durations = new Long[0];
		else {
			durations = new Long[pulses.length-1];
			for (int i = 0; i < pulses.length-1; i++) {
				durations[i] =  pulses[i+1]-pulses[i];
			}
		}
	}
	
	private void computePulses() {
		Variable[] stVars = an.getVariables(component);
		Vector<Long> pulsesTemp = new Vector<Long>();
		if (stVars.length != 0 ) pulsesTemp.add(((ActivityNetworkSolver)stVars[0].getConstraintSolver()).getOrigin());
		else pulsesTemp.add(new Long(0));
		for (int i = 0; i < stVars.length; i++) {
			Activity act = (Activity)stVars[i];
			long start = act.getTemporalVariable().getEST();
			if (!pulsesTemp.contains(start)) {
				pulsesTemp.add(start);
			}
			long end = act.getTemporalVariable().getEET();
			if (!pulsesTemp.contains(end)) {
				pulsesTemp.add(end);
			}
		}		
		pulses = pulsesTemp.toArray(new Long[pulsesTemp.size()]);
		Arrays.sort(pulses);
	}
	
	public abstract Object[] getValues();
		
	public Long[] getPulses() {
		return pulses;
	}
	
	public Long[] getDurations() {
		return durations;
	}
	
	public void draw() {
		Toolkit tk = Toolkit.getDefaultToolkit();
		int xSize = ((int) tk.getScreenSize().getWidth());
		//int ySize = ((int) tk.getScreenSize().getHeight());
		int ySize = 300;
			
		JFrame profileFrame = new JFrame(this.getClass().getSimpleName());
		JPanel jp = new JPanel();
		jp.setLayout(new GridLayout(1,1));
		PlotBoxTLSmall myPlotBox =  new PlotBoxTLSmall(this, component, true, false, -1, -1);
		jp.add(myPlotBox);
		xSize = myPlotBox.getXSize();
		
		JScrollPane sp = new JScrollPane(jp);
		sp.setPreferredSize( new Dimension(xSize,ySize) );
		profileFrame.getContentPane().add(sp,BorderLayout.CENTER);
		profileFrame.setSize(xSize, ySize);	
		profileFrame.setResizable(false);

		profileFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		profileFrame.pack();
		profileFrame.setVisible(true);
	}
	
	public ActivityNetwork getAn() {
		return an;
	}
	
	public abstract boolean isUndetermined(Object o);
	
	public abstract boolean isCritical(Object o);
	
	public abstract boolean isInconsistent(Object o);

	public String toString() {
		String ret = "== " + this.component + " ==\nPulses: " + Arrays.toString(pulses);
		ret += "\nValues: "  + Arrays.toString(this.getValues());
		ret += "\n(Durations: " + Arrays.toString(durations) + ")";
		return ret;
	}

}

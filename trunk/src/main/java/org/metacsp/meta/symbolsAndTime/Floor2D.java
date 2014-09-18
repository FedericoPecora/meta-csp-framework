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
package org.metacsp.meta.symbolsAndTime;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.ValueOrderingH;
import org.metacsp.framework.Variable;
import org.metacsp.framework.VariableOrderingH;
import org.metacsp.framework.meta.MetaVariable;
import org.metacsp.multi.activity.Activity;

public class Floor2D extends Schedulable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5013488497285863126L;
	private double distanceThreshold;
	
	public Floor2D(VariableOrderingH varOH, ValueOrderingH valOH, double distanceThreshold) {
		super(varOH, valOH);
		this.distanceThreshold = distanceThreshold;
	}

	@Override
	public boolean isConflicting(Activity[] peak) {
		if (peak.length == 1) return false;
//		double[] coords1 = parseCoordinates(((SymbolicDomain)peak[0].getSymbolicVariable().getDomain()).getSymbols()[0]);
//		double[] coords2 = parseCoordinates(((SymbolicDomain)peak[1].getSymbolicVariable().getDomain()).getSymbols()[0]);
		double[] coords1 = parseCoordinates(peak[0].getSymbolicVariable().getSymbols()[0]);
		double[] coords2 = parseCoordinates(peak[1].getSymbolicVariable().getSymbols()[0]);

		return (eucledianDistance(coords1, coords2) <= distanceThreshold);
	}

	@Override
	public void draw(final ConstraintNetwork network) {
		double maxX = -Double.MAX_VALUE;
		double maxY = -Double.MAX_VALUE;
		final double scale = 40.0;
		for (Variable v : network.getVariables()) {
			Activity a = (Activity)v;
//			SymbolicDomain dom = (SymbolicDomain)a.getSymbolicVariable().getDomain();
//			double[] coords = parseCoordinates(dom.getSymbols()[0]);
			double[] coords = parseCoordinates(a.getSymbolicVariable().getSymbols()[0]);

			if (coords[0] > maxX) maxX = coords[0];
			if (coords[1] > maxY) maxY = coords[1];
		}
		final int xSize = (int)(maxX*scale)+100;
		final int ySize = (int)(maxY*scale);
			
		JFrame dfFrame = new JFrame(this.getClass().getSimpleName());
		//JPanel jp = new JPanel();
		//jp.setLayout(new GridLayout(1,1));
		
		JPanel map = new JPanel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 6777310527624691141L;

			public void paintComponent (Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D)g;
				g2.setStroke(new BasicStroke(4.0f));
				
				double dotSize = 20.0;
				
				Variable[] vars = network.getVariables();
				for (int i = 0; i < vars.length; i++) {
					g2.setColor(Color.black);
					Activity a = (Activity)vars[i];
//					SymbolicDomain dom = (SymbolicDomain)a.getSymbolicVariable().getDomain();
//					double[] coords = parseCoordinates(dom.getSymbols()[0]);
					double[] coords = parseCoordinates(a.getSymbolicVariable().getSymbols()[0]);
					double x = scale*coords[0];
					double y = ((double)ySize-scale*coords[1]);
					//Ellipse2D.Double circle = new Ellipse2D.Double(((double)xSize-scale*coords[0]), ((double)ySize-scale*coords[1]), dotSize, dotSize);
					Ellipse2D.Double circle = new Ellipse2D.Double(x, y, dotSize, dotSize);
					g2.fill(circle);
					g2.drawString(a.getTemporalVariable().getDomain().toString(), (int)(x+dotSize), (int)(y+dotSize));
					//System.out.println("Drawing " + circle);
					
					///////////////////////
					for (int j = i+1; j < vars.length; j++) {
						Activity a1 = (Activity)vars[j];
//						SymbolicDomain dom1 = (SymbolicDomain)a1.getSymbolicVariable().getDomain();
//						double[] coords1 = parseCoordinates(dom1.getSymbols()[0]);
						double[] coords1 = parseCoordinates(a1.getSymbolicVariable().getSymbols()[0]);
						double distance = eucledianDistance(coords, coords1);
						if (distance < distanceThreshold && temporalOverlap(a, a1)) {
							g2.setPaint(Color.red);
							double x1 = scale*coords1[0];
							double y1 = ((double)ySize-scale*coords1[1]);
							Line2D.Double line = new Line2D.Double(x+dotSize/2,y+dotSize/2,x1+dotSize/2,y1+dotSize/2);
							g2.draw(line);
						}
						
					}
					//////////////////////
				}
			}
		};
		//jp.add(map);
		
		//JScrollPane sp = new JScrollPane(jp);
		//sp.setPreferredSize( new Dimension(xSize,ySize) );
		//dfFrame.getContentPane().add(sp,BorderLayout.CENTER);
		
		map.setPreferredSize( new Dimension(xSize,ySize) );
		
		dfFrame.getContentPane().add(map);
		dfFrame.setSize(xSize, ySize);	
		dfFrame.setResizable(false);

		dfFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    	
		dfFrame.pack();
		dfFrame.setVisible(true);
	}
	
	private double[] parseCoordinates(String coords) {
		double[] ret = new double[2];
		String xString = coords.substring(1, coords.indexOf("y"));
		String yString = coords.substring(coords.indexOf("y")+1, coords.length());
		ret[0] = Double.parseDouble(xString);
		ret[1] = Double.parseDouble(yString);
		return ret;
	}
	
	private double eucledianDistance(double[] point1, double[] point2) {
		return Math.sqrt(Math.pow(point2[0]-point1[0],2) + Math.pow(point2[1]-point1[1],2));
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getEdgeLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEquivalent(Constraint c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ConstraintSolver getGroundSolver() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ConstraintNetwork[] getMetaValues(MetaVariable metaVariable,
			int initial_time) {
		// TODO Auto-generated method stub
		return null;
	}


}

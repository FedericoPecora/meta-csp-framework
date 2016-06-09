package org.metacsp.utility.UI;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.multi.spatial.DE9IM.GeometricShapeDomain;
import org.metacsp.multi.spatial.DE9IM.PointDomain;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import org.metacsp.multi.spatioTemporal.paths.PoseSteering;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelope;

import sun.util.resources.CalendarData;

import com.vividsolutions.jts.geom.Geometry;

public class TrajectoryEnvelopeAnimator {
	
	private JTSDrawingPanel panel = null;
	private ArrayList<TrajectoryEnvelope> tes = new ArrayList<TrajectoryEnvelope>();
	private HashMap<String,Pose> markers = new HashMap<String, Pose>();
	private ArrayList<Geometry> extraGeoms = new ArrayList<Geometry>();
	private long origin = 0;
	private long horizon = 1000;
	private JSlider slider = null;
	private int value = 0;
	private long timeL = 0;
	private JButton resetViz = null;
	private JButton updateTime = null;
	private JTextField currentTimeField = null;
	private JTextField currentTTCField = null;
	private boolean recomputeTime = true;
	private boolean addMakespanVisualizer = true;
	private MakespanVisualizer mv = null;
	private static final int panelWidth = 700;
	private static final int panelHeight = 500;
	
	public TrajectoryEnvelopeAnimator(String title) {
		panel = new JTSDrawingPanel();
		final JFrame frame = new JFrame(title); 
		Container cp = frame.getContentPane();
		cp.setLayout(new BorderLayout());
		
		resetViz = new JButton("Fit screen");
		resetViz.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				panel.resetVisualization();
				frame.requestFocus();
			}
		});
		frame.getContentPane().add(resetViz, BorderLayout.NORTH);
		
		cp.add(panel, BorderLayout.CENTER);
		final JPanel pBottom = new JPanel();
		pBottom.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 2));
		slider = new JSlider(0, 100, 0);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				value = ((JSlider)e.getSource()).getValue();
				if (recomputeTime) {
					double time = (getHorizon()-getOrigin())*(((double)value)/100)+getOrigin();
					timeL = (long)time;
					updateTime();
				}
				recomputeTime = true;
			}
		});
		pBottom.add(slider);
		pBottom.add(new JLabel("Time:"));
		currentTimeField = new JTextField(10);
		currentTimeField.setEditable(true);
		currentTimeField.setHorizontalAlignment(SwingConstants.RIGHT);
		pBottom.add(currentTimeField);
		updateTime = new JButton("Update");
		updateTime.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					long newtime = Long.parseLong(currentTimeField.getText());
					if (newtime < origin) newtime = origin;
					if (newtime > horizon) newtime = horizon;
					timeL = newtime;
					updateTime();
					recomputeTime = false;
					updateValue();
				}
				catch(NumberFormatException e1) {
					currentTimeField.setText(""+timeL);
					currentTTCField.setText(formatTTC());
				}
				frame.requestFocus();
			}
		});
		pBottom.add(updateTime);

		pBottom.add(new JLabel("TTC:"));
		currentTTCField = new JTextField(8);
		currentTTCField.setEditable(false);
		currentTTCField.setHorizontalAlignment(SwingConstants.RIGHT);
		pBottom.add(currentTTCField);

		cp.add(pBottom, BorderLayout.SOUTH);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		frame.add(panel);
		frame.setSize(panelWidth, panelHeight); 
		frame.setVisible(true); 
		frame.setFocusable(true);
		frame.setFocusableWindowState(true);
		frame.requestFocus();

		//slider.setPreferredSize(new Dimension(p.getSize().width, slider.getPreferredSize().height));
		
		MouseListener ml = new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) { }
			@Override
			public void mousePressed(MouseEvent e) { frame.requestFocus(); }
			@Override
			public void mouseExited(MouseEvent e) { }
			@Override
			public void mouseEntered(MouseEvent e) { }
			@Override
			public void mouseClicked(MouseEvent e) { }
		};
		
		KeyListener kl = new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				//double change = ((getHorizon()-getOrigin())*0.01);
				double change = 1000;
				if (e.getKeyChar() == '+') {
					timeL = Math.min(horizon, timeL+(long)change);
					updateTime();
					updateValue();
				}
				else if (e.getKeyChar() == '-') {
					timeL = Math.max(origin,timeL-(long)change);
					updateTime();
					updateValue();
				}
			}
			@Override
			public void keyReleased(KeyEvent e) { }
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_END) {
					timeL = getHorizon();
					updateTime();
					updateValue();					
				}
				else if (e.getKeyCode() == KeyEvent.VK_HOME) {
					timeL = getOrigin();
					updateTime();
					updateValue();					
				}
			}
		};
		
		frame.addMouseListener(ml);
		slider.addMouseListener(ml);
		frame.addKeyListener(kl);
		
		if (addMakespanVisualizer) {
			mv = new MakespanVisualizer() {
				private static final long serialVersionUID = -911507104369026648L;
				@Override
				public long getTime() { return getCurrentTime(); }
			};
		}
	}
	
	private void updateValue() {
		double valueD = ((double)timeL-(double)getOrigin()) / ((double)getHorizon()-(double)getOrigin())*100.0;
		value = (int)valueD;
		slider.setValue(value);
	}
	
	public long getOrigin() {
		return origin;
	}
	
	public long getHorizon() {
		return horizon;
	}
	
	public void addTrajectoryEnvelope(TrajectoryEnvelope te) {
		this.tes.add(te);
		updateBounds();
		updateTime();
	}
	
	public void addTrajectoryEnvelopes(TrajectoryEnvelope ... te) {
		for (TrajectoryEnvelope t : te) this.tes.add(t);
		updateBounds();
		updateTime();
	}
	
	public void addTrajectoryEnvelopes(ConstraintNetwork con) {
		for (Variable v : con.getVariables()) {
			if (v instanceof TrajectoryEnvelope) {
				TrajectoryEnvelope te = (TrajectoryEnvelope)v;
				if (!te.hasSubEnvelopes()) {
					this.tes.add(te);
				}
			}
		}
		updateBounds();
		updateTime();
	}
		
	public void addMarkers(String[] ids, Pose[] poses) {
		for (int i = 0; i < poses.length; i++) {
			this.markers.put(ids[i], poses[i]);
		}
		updateBounds();
		updateTime();
	}
	
	public void addExtraGeometries(Geometry ... geoms) {
		for (int i = 0; i < geoms.length; i++) {
			this.extraGeoms.add(geoms[i]);
		}
		updateBounds();
		updateTime();
	}

	private void updateBounds() {
		ArrayList<Long> starts = new ArrayList<Long>();
		ArrayList<Long> ends = new ArrayList<Long>();
		for (int i = 0; i < tes.size(); i++) {
			if (!tes.get(i).getReferencePathVariable().getShapeType().equals(PointDomain.class)) {
				starts.add(tes.get(i).getTemporalVariable().getEST());
				ends.add(tes.get(i).getTemporalVariable().getEET());
			}
		}
		Collections.sort(starts);
		Collections.sort(ends);
		this.origin = starts.get(0);
		this.horizon = ends.get(ends.size()-1);
		if (mv != null) this.mv.setCompletionDate(this.horizon);
	}
	
	public void updateTime() {
		panel.flushGeometries();
		for (TrajectoryEnvelope te : tes) {
			panel.addGeometry("_"+te.getID(), ((GeometricShapeDomain)te.getEnvelopeVariable().getDomain()).getGeometry(),true);
			TrajectoryEnvelope gte = te.getClosestGroundEnvelope(timeL);
			if (gte != null) {
				PoseSteering ps = gte.getPoseSteering(timeL);
				if (gte.getReferencePathVariable().getShapeType().equals(PointDomain.class)) {
					panel.addGeometry("_"+gte.getID(), ((GeometricShapeDomain)gte.getEnvelopeVariable().getDomain()).getGeometry());
					panel.addGeometry("_ObstacleActive" + te.getID(), te.makeFootprint(ps), true, true, false);
				}
				else {
					panel.addGeometry(gte.getID()+"", ((GeometricShapeDomain)gte.getEnvelopeVariable().getDomain()).getGeometry());
					panel.addGeometry("Robot " + te.getRobotID(), te.makeFootprint(ps), true, true, false);					
				}
			}
		}
		for (Entry<String, Pose> e : this.markers.entrySet()) {
			panel.addArrow(e.getKey(), e.getValue());
		}
		for (int i = 0; i < this.extraGeoms.size(); i++) {
			panel.addGeometry("_extraGeom"+i, this.extraGeoms.get(i), true, true);
		}
		currentTimeField.setText("" + timeL);
		currentTTCField.setText(formatTTC());
		panel.updatePanel();
	}
	
	private String formatTTC() {
		long currentTTC = horizon-timeL;
		int seconds = (int) (currentTTC / 1000) % 60 ;
		int minutes = (int) ((currentTTC / (1000*60)) % 60);
		int hours   = (int) ((currentTTC / (1000*60*60)) % 24);
		return String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds);	
	}
	
	public long getCurrentTime() {
		return timeL;
	}

}

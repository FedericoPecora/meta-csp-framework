package org.metacsp.utility.UI;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;
import org.metacsp.meta.spatioTemporal.paths.TrajectoryEnvelopeScheduler;
import org.metacsp.multi.allenInterval.AllenIntervalConstraint;
import org.metacsp.multi.spatial.DE9IM.GeometricShapeDomain;
import org.metacsp.multi.spatial.DE9IM.GeometricShapeVariable;
import org.metacsp.multi.spatial.DE9IM.PointDomain;
import org.metacsp.multi.spatial.DE9IM.PolygonalDomain;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import org.metacsp.multi.spatioTemporal.paths.PoseSteering;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelope;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelopeSolver;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.Bounds;
import org.metacsp.utility.logging.MetaCSPLogging;

import cern.colt.Arrays;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

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
	private JButton updateTime = null;
	private JTextField currentTimeField = null;
	private JTextField currentTTCField = null;
	private boolean recomputeTime = true;
	private boolean addMakespanVisualizer = false;
	private MakespanVisualizer mv = null;
	private static final int panelWidth = 700;
	private static final int panelHeight = 500;
	
	private TrajectoryEnvelopeScheduler metaSolver = null;
	
	private JMenuBar menuBar;
	private JMenu menuFile;
	private JMenuItem itemSave;
	private JMenuItem itemOpen;
	private JMenuItem itemAddDurations;
	private JMenuItem itemQuit;
	private JMenu menuSolve;
	private JMenuItem itemSolve;
	private JMenuItem itemRefine;
	private JMenuItem itemAddDelay;
    
	public void setTrajectoryEnvelopeScheduler (TrajectoryEnvelopeScheduler metaSolver) {
		this.metaSolver = metaSolver;
	}
	
	private ConstraintNetwork getConstraintNetwork() {
		if (tes == null || tes.isEmpty()) return null;
		return tes.get(0).getConstraintSolver().getConstraintNetwork();
	}
	
	private Coordinate[] parseGeofenceFile(File file, double zoom) {
		ArrayList<Coordinate> coords = new ArrayList<Coordinate>();
		try {
			Scanner in = new Scanner(new FileReader(file));
			while (in.hasNextLine()) {
				String line = in.nextLine().trim();
				if (!line.startsWith("#")) {
					if (line.length() != 0) {
						String[] oneline = line.split(",");
						if (oneline.length == 2) {
							coords.add(new Coordinate(zoom*Double.parseDouble(oneline[0]), zoom*Double.parseDouble(oneline[1])));
						}
					}
				}
			}
			if (!coords.isEmpty()) coords.add(coords.get(0));
			in.close();
		}
		catch (FileNotFoundException e) { e.printStackTrace(); }
		return coords.toArray(new Coordinate[coords.size()]);
	}
	
	private void addDurations(long duration, TrajectoryEnvelope ... envelopes) {
		if (envelopes.length > 0) {
			ConstraintSolver solver = envelopes[0].getConstraintSolver();
			ArrayList<AllenIntervalConstraint> toAdd = new ArrayList<AllenIntervalConstraint>();
			for (TrajectoryEnvelope te : envelopes) {
				AllenIntervalConstraint dur = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Duration, new Bounds(duration, APSPSolver.INF));
				dur.setFrom(te);
				dur.setTo(te);
				toAdd.add(dur);
			}
			System.out.println(toAdd);
			solver.addConstraints(toAdd.toArray(new AllenIntervalConstraint[toAdd.size()]));
		}
	}
	
	private TrajectoryEnvelope[] getAllTrajecotryEnvleopesLargerThan(double area) {
		ArrayList<TrajectoryEnvelope> ret = new ArrayList<TrajectoryEnvelope>();
		for (TrajectoryEnvelope te : tes) {
			if (te.getPathLength() == 1) {
				GeometricShapeVariable shape = te.getEnvelopeVariable();
				PolygonalDomain poly = (PolygonalDomain)shape.getDomain();
				if (poly.getGeometry().getArea() > area) {
					ret.add(te);
				}
			}
		}
		return ret.toArray(new TrajectoryEnvelope[ret.size()]);
	}
	
	private String getSemrobDir() {
		File theDir = new File(System.getProperty("user.home") + File.separator + ".semrob");
		// if the directory does not exist, create it
		if (!theDir.exists()) {
		    boolean result = false;
		    try {
		        theDir.mkdir();
		        result = true;
		    }
		    catch(SecurityException se){ se.printStackTrace(); }        
		    if(!result) { return System.getProperty("user.home"); }
		}
		return theDir.getAbsolutePath();
	}
	
	public TrajectoryEnvelopeAnimator(String title) {
		panel = new JTSDrawingPanel();
		final JFrame frame = new JFrame(title); 
		final Container cp = frame.getContentPane();
		cp.setLayout(new BorderLayout());

		//Menu bar
		menuBar = new JMenuBar();
		menuFile = new JMenu("File");
		itemOpen = new JMenuItem("Open...");
		itemSave = new JMenuItem("Save As...");
		itemAddDurations = new JMenuItem("Add durations");
		itemQuit = new JMenuItem("Quit");
        menuFile.add(itemOpen);
		menuFile.add(itemSave);
		menuFile.add(itemAddDurations);
		menuFile.add(itemQuit);
		menuSolve = new JMenu("Solve");
		itemSolve = new JMenuItem("Solve");
		itemRefine = new JMenuItem("Refine envelopes");
		itemAddDelay = new JMenuItem("Add delay...");
        menuFile.add(itemOpen);
		menuFile.add(itemSave);
		menuFile.add(itemAddDurations);
		menuFile.add(itemQuit);
		menuSolve.add(itemRefine);
		menuSolve.add(itemSolve);
		itemSolve.setEnabled(false);
		menuSolve.add(itemAddDelay);
        
		itemSave.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser(getSemrobDir());
                chooser.showOpenDialog(null);
                File file = chooser.getSelectedFile();
                if (file != null) {
                	ConstraintNetwork.saveConstraintNetwork(getConstraintNetwork(), file);
	                frame.setTitle(file.getName());
                }
            }
        });

        itemOpen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	
                JFileChooser chooser = new JFileChooser(getSemrobDir());
                chooser.showOpenDialog(null);
                File file = chooser.getSelectedFile();
                if (file != null) {
                	if (file.getName().endsWith(".cn")) {
	                	ConstraintNetwork con = ConstraintNetwork.loadConstraintNetwork(file);
		            	tes = new ArrayList<TrajectoryEnvelope>();
		            	markers = new HashMap<String, Pose>();
		            	extraGeoms = new ArrayList<Geometry>();
		            	origin = 0;
		            	horizon = 1000;
		            	timeL = 0;
		                panel.flushGeometries();
		                panel.reinitVisualization();
		                addTrajectoryEnvelopes(con);
		                frame.setTitle(file.getName());
		            	updateValue();
                	}
                	else if (file.getName().endsWith(".gf")) {
                		Coordinate[] gfence = parseGeofenceFile(file,2.0);
                		GeometryFactory gf = new GeometryFactory();
                		Geometry geofence = gf.createLineString(gfence);
                		addExtraGeometries(geofence);
                	}
                }
            }
        });
        
        itemAddDurations.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addDurations(1200000, getAllTrajecotryEnvleopesLargerThan(5.0));
				updateBounds();
			}
        });
        
        itemQuit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        itemRefine.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		if (metaSolver != null) {
        			ConstraintNetwork refined = metaSolver.refineTrajectoryEnvelopes();
    				tes.clear();
    				addTrajectoryEnvelopes(metaSolver.getConstraintSolvers()[0].getConstraintNetwork());
//    				JTSDrawingPanel.drawConstraintNetwork("Geometries after refinement",refined);
    				itemSolve.setEnabled(true);
        		}
            }
        });
        
        itemSolve.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		if (metaSolver != null) {
        			boolean solved = metaSolver.backtrack();
        			System.out.println("Solved? " + solved);
        			updateTime();
    				updateBounds();
        		}
            }
        });

        itemAddDelay.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		if (metaSolver != null) {
        			TrajectoryEnvelopeSolver solver = (TrajectoryEnvelopeSolver)metaSolver.getConstraintSolvers()[0];
        			TrajectoryEnvelope[] tes = solver.getTrajectoryEnvelopes(0);
        			TrajectoryEnvelope superEnvelope = null;
        			for (TrajectoryEnvelope te : tes) {
        				if (!te.hasSuperEnvelope()) {
        					superEnvelope = te;
        					break;
        				}
        			}
        			TrajectoryEnvelope gte = superEnvelope.getClosestGroundEnvelope(timeL);
        			long delay = 1000;
        			long newRelease = delay + gte.getTemporalVariable().getEST();
        			AllenIntervalConstraint release = new AllenIntervalConstraint(AllenIntervalConstraint.Type.Release, new Bounds(newRelease,APSPSolver.INF));
        			release.setFrom(gte);
        			release.setTo(gte);
        			boolean added = solver.addConstraint(release);
        			System.out.println("Delayed " + gte + " by " + delay + " ms");
        			updateTime();
    				updateBounds();
        		}
            }
        });

        menuBar.add(menuFile);
        menuBar.add(menuSolve);
		frame.setJMenuBar(menuBar);
		
		cp.add(panel, BorderLayout.NORTH);
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
				if (!te.hasSubEnvelopes() || !te.hasSuperEnvelope()) {
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
//		updateBounds();
		updateTime();
	}
	
	public void addExtraGeometries(Geometry ... geoms) {
		for (int i = 0; i < geoms.length; i++) {
			this.extraGeoms.add(geoms[i]);
		}
//		updateBounds();
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
		//int hours   = (int) ((currentTTC / (1000*60*60)) % 24);
		int hours   = (int) ((currentTTC / (1000*60*60)));
		return String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds);	
	}
	
	public long getCurrentTime() {
		return timeL;
	}

}

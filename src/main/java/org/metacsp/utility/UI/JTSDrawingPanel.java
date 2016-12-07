package org.metacsp.utility.UI;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.multi.spatial.DE9IM.GeometricShapeDomain;
import org.metacsp.multi.spatial.DE9IM.GeometricShapeVariable;
import org.metacsp.multi.spatioTemporal.paths.Pose;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelope;
import org.metacsp.utility.logging.MetaCSPLogging;

import com.vividsolutions.jts.awt.ShapeWriter;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.util.AffineTransformation;
import com.vividsolutions.jts.util.GeometricShapeFactory;

public class JTSDrawingPanel extends JPanel {

	private static final String[] COLOR_CHART = new String[]{
		"#000000", "#FFFF00", "#1CE6FF", "#FF34FF", "#FF4A46", "#008941", "#006FA6", "#A30059",
		"#FFDBE5", "#7A4900", "#0000A6", "#63FFAC", "#B79762", "#004D43", "#8FB0FF", "#997D87",
		"#5A0007", "#809693", "#FEFFE6", "#1B4400", "#4FC601", "#3B5DFF", "#4A3B53", "#FF2F80",
		"#61615A", "#BA0900", "#6B7900", "#00C2A0", "#FFAA92", "#FF90C9", "#B903AA", "#D16100",
		"#DDEFFF", "#000035", "#7B4F4B", "#A1C299", "#300018", "#0AA6D8", "#013349", "#00846F",
		"#372101", "#FFB500", "#C2FFED", "#A079BF", "#CC0744", "#C0B9B2", "#C2FF99", "#001E09",
		"#00489C", "#6F0062", "#0CBD66", "#EEC3FF", "#456D75", "#B77B68", "#7A87A1", "#788D66",
		"#885578", "#FAD09F", "#FF8A9A", "#D157A0", "#BEC459", "#456648", "#0086ED", "#886F4C",

		"#34362D", "#B4A8BD", "#00A6AA", "#452C2C", "#636375", "#A3C8C9", "#FF913F", "#938A81",
		"#575329", "#00FECF", "#B05B6F", "#8CD0FF", "#3B9700", "#04F757", "#C8A1A1", "#1E6E00",
		"#7900D7", "#A77500", "#6367A9", "#A05837", "#6B002C", "#772600", "#D790FF", "#9B9700",
		"#549E79", "#FFF69F", "#201625", "#72418F", "#BC23FF", "#99ADC0", "#3A2465", "#922329",
		"#5B4534", "#FDE8DC", "#404E55", "#0089A3", "#CB7E98", "#A4E804", "#324E72", "#6A3A4C",
		"#83AB58", "#001C1E", "#D1F7CE", "#004B28", "#C8D0F6", "#A3A489", "#806C66", "#222800",
		"#BF5650", "#E83000", "#66796D", "#DA007C", "#FF1A59", "#8ADBB4", "#1E0200", "#5B4E51",
		"#C895C5", "#320033", "#FF6832", "#66E1D3", "#CFCDAC", "#D0AC94", "#7ED379", "#012C58"
	};

	private static final long serialVersionUID = -2533567139276709334L;
	private static final int MARGIN = 5; 
	private HashMap<String,Geometry> geometries = new HashMap<String,Geometry>(); 
	private HashMap<String,Boolean> emptyGeoms = new HashMap<String,Boolean>(); 
	private HashMap<String,Boolean> thickGeoms = new HashMap<String,Boolean>(); 
	private HashMap<String,Boolean> transpGeoms = new HashMap<String,Boolean>(); 
	private HashMap<String,Paint> polyColors = new HashMap<String,Paint>(); 
	private AffineTransform geomToScreen = null;
	private double scale = 1.0;
	private double userScale = 1.0;
	private AffineTransform panTrans = AffineTransform.getTranslateInstance(0.0, 0.0);
	private Logger metacsplogger = MetaCSPLogging.getLogger(this.getClass());
	private boolean rotateMode = false;
	
	public JTSDrawingPanel() {
		this.setDoubleBuffered(true);
		
		this.addMouseListener(new MouseAdapter() {
		    @Override
		    public void mouseClicked(MouseEvent e) {
		    	if (SwingUtilities.isLeftMouseButton(e)) {
		    		if (e.getClickCount() == 2) {
		    			resetVisualization();
		    		}
		    	}
		    	if (SwingUtilities.isMiddleMouseButton(e)) {
		    		try {
						AffineTransform geomToScreenInv = geomToScreen.createInverse();
						Point2D.Double clickedPoint = new Point2D.Double((double)e.getX(),(double)e.getY());
						Point2D.Double tClickedPoint = new Point2D.Double();
						geomToScreenInv.transform(clickedPoint, tClickedPoint);
						metacsplogger.info("Clicked point (x,y) = (" + tClickedPoint.getX() + "," + tClickedPoint.getY() + ")");
					} catch (NoninvertibleTransformException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
		    	}
		    }
		});
		
		this.addMouseMotionListener(new MouseAdapter() {
			int previousX;
			int previousY;
			
			@Override
			public void mousePressed(MouseEvent e) {
				previousX = e.getX();
				previousY = e.getY();
			}
			
		    @Override
		    public void mouseDragged(MouseEvent e) {
		        int x = e.getX();
		        int y = e.getY();
		    	if (SwingUtilities.isRightMouseButton(e)) {
		    		userScale += Math.signum(y-previousY)*0.05;
		    		if (userScale < 0.1) userScale = 0.1;		    		
		    	}
		    	else if (SwingUtilities.isLeftMouseButton(e)) {
		    		panTrans = AffineTransform.getTranslateInstance(panTrans.getTranslateX()+Math.signum(x-previousX)*10.0, panTrans.getTranslateY()+Math.signum(y-previousY)*10.0);
		    	}
	    		previousX = x;
	    		previousY = y;
	        	updatePanel();
		    }
		    
		});
	}
	
	private Geometry createArrow(Pose pose) {
		GeometryFactory gf = new GeometryFactory();
		Coordinate[] coords = new Coordinate[8];
		coords[0] = new Coordinate(0.0,-0.3);
		coords[1] = new Coordinate(2.0,-0.3);
		coords[2] = new Coordinate(2.0,-0.8);
		coords[3] = new Coordinate(3.0,0.0);
		coords[4] = new Coordinate(2.0,0.8);
		coords[5] = new Coordinate(2.0,0.3);
		coords[6] = new Coordinate(0.0,0.3);
		coords[7] = new Coordinate(0.0,-0.3);
		Polygon arrow = gf.createPolygon(coords);
		AffineTransformation at = new AffineTransformation();
		at.rotate(pose.getTheta());
		at.translate(pose.getX(), pose.getY());
		Geometry ret = at.transform(arrow);
		return ret;
	}
	
	public synchronized void addArrow(String arrowId, Pose pose) {
		geometries.put(arrowId, createArrow(pose));
		emptyGeoms.put(arrowId, false);
		transpGeoms.put(arrowId, false);
		thickGeoms.put(arrowId, false);
		polyColors.put(arrowId, Color.black);
	}
	
	public synchronized void resetVisualization() {
		userScale = 1.0;
		panTrans = AffineTransform.getTranslateInstance(0.0, 0.0);
		updatePanel();
	}
	
	public synchronized void reinitVisualization() {
		userScale = 1.0;
		scale = 1.0;
		panTrans = AffineTransform.getTranslateInstance(0.0, 0.0);
		rotateMode = false;
		geomToScreen = null;
	}
	
	public synchronized void addGeometry(String id, Geometry geom) { 
		geometries.put(id,geom);
		emptyGeoms.put(id,false);
		thickGeoms.put(id,false);
		transpGeoms.put(id,true);
		Paint polyPaint = Color.decode(COLOR_CHART[(Math.abs(id.hashCode()))%COLOR_CHART.length]);
		polyColors.put(id,polyPaint);
	}

	public synchronized void flushGeometries() {
		geometries.clear();
		emptyGeoms.clear();
		thickGeoms.clear();
		transpGeoms.clear();
		polyColors.clear();
	}

	public synchronized void addGeometry(String id, Geometry geom, boolean empty) { 
		geometries.put(id,geom);
		emptyGeoms.put(id,empty);
		thickGeoms.put(id,false);
		transpGeoms.put(id,true);
		Paint polyPaint = Color.decode(COLOR_CHART[(Math.abs(id.hashCode()))%COLOR_CHART.length]);
		polyColors.put(id,polyPaint);
	}

	public synchronized void addGeometry(String id, Geometry geom, boolean empty, boolean thick) { 
		geometries.put(id,geom);
		emptyGeoms.put(id,empty);
		thickGeoms.put(id,thick);
		transpGeoms.put(id,true);
		Paint polyPaint = Color.decode(COLOR_CHART[(Math.abs(id.hashCode()))%COLOR_CHART.length]);
		polyColors.put(id,polyPaint);
	}

	public synchronized void addGeometry(String id, Geometry geom, boolean empty, boolean thick, boolean transp) { 
		geometries.put(id,geom);
		emptyGeoms.put(id,empty);
		thickGeoms.put(id,thick);
		transpGeoms.put(id,transp);
		Paint polyPaint = Color.decode(COLOR_CHART[(Math.abs(id.hashCode()))%COLOR_CHART.length]);
		polyColors.put(id,polyPaint);
	}
	
	public Coordinate getCoordinatesInRealWorld(Point clicked) {
		AffineTransform invTrans = new AffineTransform(geomToScreen);
		try { invTrans.invert(); }
		catch (NoninvertibleTransformException e) { e.printStackTrace(); }
		Point2D.Double transformed = new Point2D.Double(0, 0);
		invTrans.transform(new Point2D.Double(clicked.getX(), clicked.getY()), transformed);
		return new Coordinate(transformed.getX(), transformed.getY());
	}

	public synchronized void removeGeometry(String id) { 
		geometries.remove(id); 
		emptyGeoms.remove(id);
		thickGeoms.remove(id);
		transpGeoms.remove(id);
		polyColors.remove(id);
	} 

	public void updatePanel() {
		this.repaint();
	}

	private AlphaComposite makeComposite(float alpha) {
		int type = AlphaComposite.SRC_OVER;
		return(AlphaComposite.getInstance(type, alpha));
	}

	private void drawText(Graphics2D g2d, String text, double x, double y, Paint polyPaint, boolean empty) {
		g2d.setComposite(makeComposite(1.0f));
		g2d.setPaint(polyPaint); 
		AffineTransform newTrans = new AffineTransform(geomToScreen);
		if (rotateMode) newTrans.rotate(-Math.PI/2, x, y);
		newTrans.translate(x, y);
		Font f = new Font("TimesRoman", Font.PLAIN, 3);
		TextLayout tl = new TextLayout(text, f, g2d.getFontRenderContext());
		Shape shape = tl.getOutline(null);
		Shape newShape = newTrans.createTransformedShape(shape);
		if (!empty) g2d.fill(newShape);
		else g2d.draw(newShape);
	}
	
	@Override 
	protected synchronized void paintComponent(Graphics g) { 
		super.paintComponent(g); 
		
		if (!geometries.isEmpty()) { 
			setTransform(); 

			Graphics2D g2d = (Graphics2D) g; 
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
			g2d.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			
			Paint defaultPaint = Color.BLACK; 
			Paint startCircPaint = Color.GREEN; 
			Paint endCircPaint = Color.RED; 

			for (Entry<String,Geometry> e : geometries.entrySet()) { 
				Geometry geom = e.getValue();
				boolean empty = emptyGeoms.get(e.getKey());
				boolean thick = thickGeoms.get(e.getKey());
				boolean transp = transpGeoms.get(e.getKey());
				ShapeWriter writer = new ShapeWriter();
				Shape shape = writer.toShape(geom);
				Shape newShape = geomToScreen.createTransformedShape(shape);
				if (geom instanceof Polygon) {
					Paint polyPaint = polyColors.get(e.getKey());
					if (transp) g2d.setComposite(makeComposite(0.5f));
					g2d.setPaint(polyPaint); 

					if (thick) g2d.setStroke(new BasicStroke(5.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

					if (empty) g2d.draw(newShape);
					else g2d.fill(newShape);

					g2d.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

					if (!e.getKey().startsWith("_")) {
						//Draw label
						String text = ""+e.getKey();
						drawText(g2d, text, geom.getCentroid().getX(), geom.getCentroid().getY(), polyPaint, empty);
					}

				} else {
					g2d.setPaint(defaultPaint); 
					g2d.draw(newShape);

					//draw start/end circles
					GeometricShapeFactory gsf = new GeometricShapeFactory();
					gsf.setSize(3);
					gsf.setCentre(geom.getCoordinates()[0]);
					Polygon startCirc = gsf.createCircle();
					gsf.setCentre(geom.getCoordinates()[geom.getCoordinates().length-1]);
					Polygon endCirc = gsf.createCircle();
					Shape startCircShape = writer.toShape(startCirc);
					Shape endCircShape = writer.toShape(endCirc);
					Shape newStartCircShape = geomToScreen.createTransformedShape(startCircShape);
					Shape newEndCircShape = geomToScreen.createTransformedShape(endCircShape);
					g2d.setStroke(new BasicStroke(2));
					g2d.setPaint(startCircPaint); 
					g2d.draw(newStartCircShape);
					g2d.setPaint(endCircPaint); 
					g2d.draw(newEndCircShape);
				} 
			}
			g2d.setComposite(makeComposite(1.0f));
		}
	} 

	private void setTransform() { 
		Envelope env = getGeometryBounds(); 
		Rectangle visRect = getVisibleRect();
		Rectangle drawingRect = new Rectangle(visRect.x + MARGIN, visRect.y + MARGIN, visRect.width - 2*MARGIN, visRect.height - 2*MARGIN); 
		if (env.getWidth() < env.getHeight()) rotateMode = true;
		
		scale = Math.min(drawingRect.getWidth() / env.getWidth(), drawingRect.getHeight() / env.getHeight()) * userScale; 
		double xoff = MARGIN - scale * env.getMinX();
		if (rotateMode) xoff += scale*(env.getMaxY()-env.getMinY())/2.0;
		double yoff = MARGIN - env.getMinY() * scale; 
		geomToScreen = new AffineTransform(scale, 0, 0, -scale, xoff, yoff);
		geomToScreen.concatenate(AffineTransform.getScaleInstance(1, -1));
		geomToScreen.concatenate(panTrans);
		if (rotateMode) geomToScreen.rotate(Math.PI/2.0, env.getMinX()+(env.getMaxX()-env.getMinX())/2, env.getMinY()+(env.getMaxY()-env.getMinY())/2);
		//geomToScreen.concatenate(rotateTrans);
	} 

	private Envelope getGeometryBounds() { 
		Envelope env = new Envelope(); 
		for (Entry<String,Geometry> e : geometries.entrySet()) { 
			Geometry geom = e.getValue();
			Envelope geomEnv = geom.getEnvelopeInternal(); 
			env.expandToInclude(geomEnv); 
		} 

		return env; 
	} 

	public static void drawVariables(String title, boolean[] empty, GeometricShapeVariable[] vars) {
		JTSDrawingPanel panel = new JTSDrawingPanel(); 
		JFrame frame = new JFrame(title); 
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		frame.add(panel); 
		frame.setSize(500, 500);
		for (int i = 0; i < vars.length; i++) {
			panel.emptyGeoms.put(vars[i].getID()+"", empty[i]);
			panel.addGeometry(vars[i].getID()+"",((GeometricShapeDomain)vars[i].getDomain()).getGeometry());
		}
		frame.setVisible(true);     	
	}

	public static void drawVariables(String title, GeometricShapeVariable ... vars) {
		JTSDrawingPanel panel = new JTSDrawingPanel(); 
		JFrame frame = new JFrame(title); 
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		frame.add(panel); 
		frame.setSize(500, 500); 
		for (int i = 0; i < vars.length; i++) {
			panel.emptyGeoms.put(vars[i].getID()+"", false);
			panel.addGeometry(vars[i].getID()+"",((GeometricShapeDomain)vars[i].getDomain()).getGeometry());
		}
		frame.setVisible(true);     	
	}

	public static void drawConstraintNetwork(String title, ConstraintNetwork cn) {
		ArrayList<GeometricShapeVariable> tes = new ArrayList<GeometricShapeVariable>();
		for (Variable v : cn.getVariables()) {
			if (v instanceof TrajectoryEnvelope) {
				TrajectoryEnvelope te = (TrajectoryEnvelope)v;
				if (!te.hasSubEnvelopes()) {
					tes.add(((TrajectoryEnvelope)v).getEnvelopeVariable());
					tes.add(((TrajectoryEnvelope)v).getReferencePathVariable());
				}
			}
		}
		drawVariables(title, tes.toArray(new GeometricShapeVariable[tes.size()]));
	}


} 
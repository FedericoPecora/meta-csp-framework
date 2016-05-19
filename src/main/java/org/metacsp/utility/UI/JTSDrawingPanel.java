package org.metacsp.utility.UI;

import com.vividsolutions.jts.awt.ShapeWriter;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope; 
import com.vividsolutions.jts.geom.Geometry; 
import com.vividsolutions.jts.geom.LineString; 
import com.vividsolutions.jts.geom.Polygon; 
import com.vividsolutions.jts.io.WKTReader; 
import com.vividsolutions.jts.util.GeometricShapeFactory;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color; 
import java.awt.Font;
import java.awt.GradientPaint; 
import java.awt.Graphics; 
import java.awt.Graphics2D; 
import java.awt.Paint; 
import java.awt.Rectangle; 
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform; 
import java.util.ArrayList; 
import java.util.HashMap;
import java.util.List; 
import java.util.Map.Entry;

import javax.swing.JFrame; 
import javax.swing.JPanel; 

import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.Variable;
import org.metacsp.multi.spatial.DE9IM.GeometricShapeDomain;
import org.metacsp.multi.spatial.DE9IM.GeometricShapeVariable;
import org.metacsp.multi.spatioTemporal.paths.TrajectoryEnvelope;

public class JTSDrawingPanel extends JPanel { 
    private static final int MARGIN = 5; 
    private HashMap<Integer,Geometry> geometries = new HashMap<Integer,Geometry>(); 
    private HashMap<Integer,Boolean> emptyGeoms = new HashMap<Integer,Boolean>(); 
    private AffineTransform geomToScreen; 

    public void addGeometry(int id, Geometry geom) { 
        geometries.put(id,geom); 
    } 

    private AlphaComposite makeComposite(float alpha) {
    	  int type = AlphaComposite.SRC_OVER;
    	  return(AlphaComposite.getInstance(type, alpha));
    	 }
    
    private void drawText(Graphics2D g2d, String text, int x, int y, Paint polyPaint, boolean empty) {
//      g2d.setPaint(defaultPaint);	
    	g2d.setComposite(makeComposite(1.0f));
    	g2d.setPaint(polyPaint); 
    	AffineTransform orig = g2d.getTransform();
//    	g2d.setTransform(geomToScreen);
    	AffineTransform newTrans = new AffineTransform(geomToScreen);
    	newTrans.translate(x,y);
    	newTrans.scale(1/geomToScreen.getScaleX(), 1/geomToScreen.getScaleY());
    	g2d.setTransform(newTrans);
    	Font f = new Font("TimesRoman", Font.PLAIN, 36);
    	if (!empty) {
//	    	g2d.setFont(f); 
//	    	g2d.drawString(text, x, y);
    		TextLayout tl = new TextLayout(text, f, g2d.getFontRenderContext());
    		Shape shape = tl.getOutline(null);
    		g2d.fill(shape);
    	}
    	else {
    		TextLayout tl = new TextLayout(text, f, g2d.getFontRenderContext());
    		Shape shape = tl.getOutline(null);
    		g2d.draw(shape);
    	}
    	g2d.setTransform(orig);
    }
    
    @Override 
    protected void paintComponent(Graphics g) { 
        super.paintComponent(g); 

        if (!geometries.isEmpty()) { 
            setTransform(); 

            Graphics2D g2d = (Graphics2D) g; 
//            Paint polyPaint = new GradientPaint(0, 0, Color.CYAN, 100, 100, Color.MAGENTA, true);
//            Paint polyPaint = Color.GRAY;
            Paint defaultPaint = Color.BLACK; 
            Paint startCircPaint = Color.GREEN; 
            Paint endCircPaint = Color.RED; 
            
            for (Entry<Integer,Geometry> e : geometries.entrySet()) { 
            	Geometry geom = e.getValue();
            	boolean empty = emptyGeoms.get(e.getKey());
            	ShapeWriter writer = new ShapeWriter();
            	Shape shape = writer.toShape(geom);
            	Shape newShape = geomToScreen.createTransformedShape(shape);
            	if (geom instanceof Polygon) {
                	Paint polyPaint = Color.getHSBColor((float) Math.random(), .6f, .6f);
                	g2d.setComposite(makeComposite(0.5f));
                    g2d.setPaint(polyPaint); 
                    if (empty) g2d.draw(newShape);
                    else g2d.fill(newShape);
                    
                    //Draw label
                    String text = ""+e.getKey();
                    drawText(g2d, text, (int)(geom.getCentroid().getX()), (int)(geom.getCentroid().getY()), polyPaint, empty);
                    
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
        } 
    } 

    private void setTransform() { 
        Envelope env = getGeometryBounds(); 
        Rectangle visRect = getVisibleRect(); 
        Rectangle drawingRect = new Rectangle(visRect.x + MARGIN, visRect.y + MARGIN, visRect.width - 2*MARGIN, visRect.height - 2*MARGIN); 

        double scale = Math.min(drawingRect.getWidth() / env.getWidth(), drawingRect.getHeight() / env.getHeight()); 
        double xoff = MARGIN - scale * env.getMinX();
//        double yoff = MARGIN + env.getMaxY() * scale; 
        double yoff = MARGIN - env.getMinY() * scale; 
        geomToScreen = new AffineTransform(scale, 0, 0, -scale, xoff, yoff);
        geomToScreen.concatenate(AffineTransform.getScaleInstance(1, -1));
    } 

    private Envelope getGeometryBounds() { 
        Envelope env = new Envelope(); 
        for (Entry<Integer,Geometry> e : geometries.entrySet()) { 
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
        	panel.emptyGeoms.put(vars[i].getID(), empty[i]);
        	panel.addGeometry(vars[i].getID(),((GeometricShapeDomain)vars[i].getDomain()).getGeometry());
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
        	panel.emptyGeoms.put(vars[i].getID(), false);
        	panel.addGeometry(vars[i].getID(),((GeometricShapeDomain)vars[i].getDomain()).getGeometry());
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
        
//    public static void main(String[] args) throws Exception { 
//        JTSDrawingPanel panel = new JTSDrawingPanel(); 
//        JFrame frame = new JFrame("Draw geometries"); 
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
//        frame.add(panel); 
//        frame.setSize(500, 500); 
//
//        WKTReader reader = new WKTReader(); 
//
//        LineString line = (LineString) reader.read( 
//                "LINESTRING(20 20, 20 25, 25 25, " + 
//                "25 15, 15 15, 15 30, 30 30, 30 10, " + 
//                "10 10, 10 35, 35 35, 35 5)"); 
//        panel.addGeometry(1,line); 
//
//        line = (LineString) reader.read("LINESTRING(-10 40, 5 50, 20 40, 35 50, 50 40)"); 
//        panel.addGeometry(2,line); 
//
//        Polygon poly = (Polygon) reader.read( 
//                "POLYGON((-10 -10, 0 0, 40 0, 50 -10, 40 -20, 0 -20, -10 -10), " + 
//                "(0 -10, 5 -5, 10 -10, 5 -15, 0 -10), " + 
//                "(30 -10, 35 -5, 40 -10, 35 -15, 30 -10))"); 
//
//        panel.addGeometry(4,poly); 
//
//        frame.setVisible(true); 
//
//    } 
} 

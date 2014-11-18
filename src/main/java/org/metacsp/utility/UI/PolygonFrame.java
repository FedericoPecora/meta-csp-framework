package org.metacsp.utility.UI;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Vector;

import javax.naming.ldap.HasControls;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintNetworkChangeEvent;
import org.metacsp.framework.ConstraintNetworkChangeListener;
import org.metacsp.framework.Variable;
import org.metacsp.spatial.geometry.GeometricConstraint;
import org.metacsp.spatial.geometry.Polygon;
import org.metacsp.spatial.geometry.Vec2;

public class PolygonFrame extends JFrame implements ConstraintNetworkChangeListener {
		
	private static final long serialVersionUID = 7979735587935134767L;
	private final Dimension dim = new Dimension(1024, 768);
	private float zoom;
	private float zoomInv;
	private float deltaX = 0.0f;
	private float deltaY = 0.0f;
	private float xSpan = 0.0f;
	private float ySpan = 0.0f;
	private int paddingPixels = 20;
	private int originX = 0;
	private int originY = 0;
	
	private float canvasStartX;
	private float canvasStartY;
	private float canvasEndX;
	private float canvasEndY;

	private HashMap<Polygon,Color> colors = new HashMap<Polygon,Color>();
	private HashMap<Polygon,int[]> centers = new HashMap<Polygon,int[]>();
	private HashMap<Polygon,java.awt.Polygon> drawablePolys = new HashMap<Polygon,java.awt.Polygon>();
	
	private Variable[] polys;
	private Constraint[] cons;
	
	private ConstraintNetwork cn;
	
//	private class Pair {
//    	public Variable from, to;
//    	public Constraint con; 
//    	public Pair(Variable from, Variable to, Constraint con) {
//    		this.from = from;
//    		this.to = to;
//    		this.con = con;
//    	}
//    	public boolean equals(Object o) {
//    		return ((this.from.equals(((Pair)o).from) && this.to.equals(((Pair)o).to)) || (this.from.equals(((Pair)o).to) && this.to.equals(((Pair)o).from)));
//    	}
//    	public int hashCode() {
//    		return this.from.hashCode() + this.toString().hashCode();
//    	}
//    }
	
	private int[] toScreen(float x, float y) {
		// |------ 1024 ------|
		//dim.x:spanX=input.x:output.x -> output.x = spanX*input.x/dim.x*
		int[] ret = new int[2];
//		ret[0] = Math.round(this.zoomInv*xSpan*(x-deltaX+paddingPixels)/dim.width);
//		ret[1] = Math.round(this.zoomInv*ySpan*(y-deltaY+paddingPixels)/dim.height);
		ret[0] = Math.round(this.zoomInv*(x-deltaX+paddingPixels));
		ret[1] = Math.round(this.zoomInv*(y-deltaY+paddingPixels));
		return ret;
	}

	private void initialize() {
		this.polys = cn.getVariables();
		this.cons = cn.getConstraints();

		float maxX = Integer.MIN_VALUE;
		float minX = Integer.MAX_VALUE;
		float maxY = Integer.MIN_VALUE;
		float minY = Integer.MAX_VALUE;
		
		for (Variable p : polys) {
			if (!((Polygon)p).hasDefaultDomain()) {
				Vector<Vec2> vertices = ((Polygon)p).getFullSpaceRepresentation();
				for (int i = 0; i < vertices.size(); i++) {
					if (vertices.get(i).x > maxX) maxX = vertices.get(i).x;
					if (vertices.get(i).y > maxY) maxY = vertices.get(i).y;
					if (vertices.get(i).x < minX) minX = vertices.get(i).x;
					if (vertices.get(i).y < minY) minY = vertices.get(i).y;
				}
			}
		}

		deltaX = minX;
		deltaY = minY;
		xSpan = Math.abs(maxX-minX);
		ySpan = Math.abs(maxY-minY);
		canvasStartX = minX;
		canvasStartY = minY;
		canvasEndX = maxX;
		canvasEndY = maxY;
		
		zoom = (float)xSpan/(float)dim.getWidth();
		zoomInv = 1/zoom;
	}
	
	private void updatePositions() {
		Random rand = new Random(1231234);    	
		for (Variable p : polys) {
			Vector<Vec2> vertices = ((Polygon)p).getFullSpaceRepresentation();
			int[] xCoords = new int[vertices.size()];
			int[] yCoords = new int[vertices.size()];
			for (int i = 0; i < vertices.size(); i++) {
				int[] screenCoords = toScreen(vertices.get(i).x, vertices.get(i).y); 
				xCoords[i] = screenCoords[0];
				yCoords[i] = screenCoords[1];
			}
	    	centers.put(((Polygon)p),toScreen(((Polygon)p).getPosition().x,((Polygon)p).getPosition().y));
			java.awt.Polygon drawablePoly = new java.awt.Polygon(xCoords, yCoords, xCoords.length);
			drawablePolys.put(((Polygon)p),drawablePoly);
			float r = rand.nextFloat();
			float g = rand.nextFloat();
			float b = rand.nextFloat();
			colors.put(((Polygon)p),new Color(r,g,b));
		}
	}
	
	private void updatePolygonFrame() {
		initialize();
		repaint();
	}
		
	public PolygonFrame(String title, ConstraintNetwork constraintNetwork) {
		this.setResizable(true);
		this.setTitle(title);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.cn = constraintNetwork;
		initialize();
		
		this.cn.addConstraintNetworkChangeListener(this);
			
		this.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent arg0) { }
			
			@Override
			public void mouseDragged(MouseEvent arg0) {
				//java.awt.Point pStart = arg0.getPoint();
				deltaX += originX-arg0.getX();
				deltaY += originY-arg0.getY();
				originX = arg0.getX();
				originY = arg0.getY();
				repaint();
			}
			
		});
		
		this.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent arg0) {
				int rot = arg0.getWheelRotation();
				zoom += (0.01*rot);
				zoomInv = 1/zoom;
				repaint();
			}
		});
		
		this.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent arg0) { }
			
			@Override
			public void mousePressed(MouseEvent arg0) {
				originX = arg0.getX();
				originY = arg0.getY();
			}
			
			@Override
			public void mouseExited(MouseEvent arg0) { }
			
			@Override
			public void mouseEntered(MouseEvent arg0) { }
			
			@Override
			public void mouseClicked(MouseEvent arg0) { }
		});
		
        JPanel p = new JPanel() {
			private static final long serialVersionUID = 8429745665836985179L;
			//private final int ARR_SIZE = 16;
			private final float eccentricityStep = 1.8f;
			
        	//Draw arrows
        	private void drawArrow(Graphics g1, int x1, int y1, int x2, int y2, String label, int num) {
                Graphics2D g = (Graphics2D) g1.create();
                g.setStroke(new BasicStroke(2));
                g.setColor(new Color(0.6f,0.6f,0.6f));
                double dx = x2 - x1, dy = y2 - y1;
                double angle = Math.atan2(dy, dx);
                int len = (int) Math.sqrt(dx*dx + dy*dy);
                AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
                at.concatenate(AffineTransform.getRotateInstance(angle));
                g.transform(at);
                
                g.setColor(new Color(0.6f,0.6f,0.6f));
                java.awt.Point startP = new java.awt.Point(0, 0);
                java.awt.Point endP = new java.awt.Point(len, 0);
                CurvedArrow ca = new CurvedArrow(startP,endP,num*eccentricityStep);
                ca.label = label;
                ca.draw(g);
                
//                g.setColor(new Color(0.6f,0.6f,0.6f));
//                // Draw horizontal arrow starting in (0, 0)
//                g.drawLine(0, 0, len, 0);
//                g.fillPolygon(new int[] {len, len-ARR_SIZE, len-ARR_SIZE, len}, new int[] {0, -ARR_SIZE/3, ARR_SIZE/3, 0}, 4);
//            	g.setFont(new Font("default", Font.BOLD, 16));
//            	int stringLen = (int)g.getFontMetrics().getStringBounds(label, g).getWidth();
//                g.drawString(label, len/2-stringLen/2, -6);
            }
        	
        	private void drawGrid(Graphics g1) {
                Graphics2D g = (Graphics2D) g1.create();
				g.setStroke(new BasicStroke(1.0f));
				g.setColor(new Color(0.7f,0.7f,0.7f));
				
            	DecimalFormat df = new DecimalFormat("#.#");
            	
				float positionX = canvasStartX;
//				System.out.println("StartX = " + canvasStartX);
				float interval = xSpan/10.0f;
				while(positionX <= canvasEndX+interval) {
					int[] p1 = toScreen(positionX,canvasStartY);
					int[] p2 = toScreen(positionX,canvasEndY+interval);
					g.drawLine(p1[0], p1[1], p2[0], p2[1]);
					g.setFont(new Font("default", Font.BOLD, 8));
					String label = df.format(positionX);
	            	int stringLen = (int)g.getFontMetrics().getStringBounds(label, g).getWidth();
	            	int stringHeight = (int)g.getFontMetrics().getStringBounds(label, g).getHeight();
					g.drawString(label, p1[0]-stringLen/2, p1[1]-stringHeight);
					positionX += interval;
				}
				float positionY = canvasStartY;
				while(positionY <= canvasEndY+interval) {
					int[] p1 = toScreen(canvasStartX,positionY);
					int[] p2 = toScreen(canvasEndX+interval,positionY);
					g.drawLine(p1[0], p1[1], p2[0], p2[1]);
					g.setFont(new Font("default", Font.BOLD, 8));
					String label = df.format(positionY);
	            	int stringLen = (int)g.getFontMetrics().getStringBounds(label, g).getWidth();
	            	int stringHeight = (int)g.getFontMetrics().getStringBounds(label, g).getHeight();
					g.drawString(label, p2[0]+stringLen/2, p2[1]+stringHeight/2);
					positionY += interval;
				}
			}
        	
        	private void drawPolygon(Graphics g, Polygon p) {
                g.setColor(colors.get(p));
                ((Graphics2D)g).setStroke(new BasicStroke(5));
            	g.drawPolygon(drawablePolys.get(p));
            	g.setFont(new Font("default", Font.BOLD, 16));
            	g.drawString("P"+p.getID(), centers.get(p)[0], centers.get(p)[1]);        		
        	}

			@Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                
                //Compute positions of all polys 
                updatePositions();
                
                //Draw grid on top
				drawGrid(g);

                //Draw polys
                for (Variable p : polys) drawPolygon(g, (Polygon)p);
                
            	//Draw arrows
                Random rand = new Random(125534);
                for (Constraint con : cons) {
                	Polygon from = (Polygon)((GeometricConstraint)con).getFrom();
                	Polygon to = (Polygon)((GeometricConstraint)con).getTo();
                	drawArrow(g, centers.get(from)[0], centers.get(from)[1], centers.get(to)[0], centers.get(to)[1], con.getEdgeLabel(), rand.nextInt(10));
                }
            }

            @Override
            public Dimension getPreferredSize() {
                return dim;
            }
        };
        
        this.add(p);
        this.pack();
        this.setVisible(true);
	}
	
	@Override
	public void stateChanged(ConstraintNetworkChangeEvent event) {
		updatePolygonFrame();
	}

}

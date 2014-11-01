package org.metacsp.utility.UI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Random;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.metacsp.spatial.geometry.Polygon;
import org.metacsp.spatial.geometry.Vec2;

public class PolygonFrame extends JFrame {
	
	private static final long serialVersionUID = 7979735587935134767L;
	private final Dimension dim = new Dimension(1024, 768);
	public static int ZOOM = 2;
	private float resolutionX = 1.0f;
	private float resolutionY = 1.0f;
	
	public PolygonFrame(String title, final Polygon ... polys) {
		this.setResizable(true);
		this.setTitle(title);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		final Vector<java.awt.Polygon> drawablePolys = new Vector<java.awt.Polygon>();
		
		float maxX = Integer.MIN_VALUE;
		float minX = Integer.MAX_VALUE;
		float maxY = Integer.MIN_VALUE;
		float minY = Integer.MAX_VALUE;
		
		for (Polygon p : polys) {
			Vector<Vec2> vertices = p.getFullSpaceRepresentation();
			for (int i = 0; i < vertices.size(); i++) {
				if (vertices.get(i).x > maxX) maxX = vertices.get(i).x;
				if (vertices.get(i).y > maxY) maxY = vertices.get(i).y;
				if (vertices.get(i).x < minX) minX = vertices.get(i).x;
				if (vertices.get(i).x < minY) minY = vertices.get(i).y;
			}
		}

		if (maxX < 0) {
			System.out.println("Inverting maxX and adding to minX");
			maxX = -maxX;
			minX += 2*maxX;
		}
		if (minX < 0) {
			System.out.println("Shfting X by " + minX);
			maxX += minX;
			minX = 0;
		}
		
		if (maxY < 0) {
			System.out.println("Inverting maxY and adding to minY");
			maxY = -maxY;
			minY += 2*maxY;
		}
		if (minY < 0) {
			System.out.println("Shfting Y by " + minX);
			maxY += minY;
			minY = 0;
		}


		float xSpan = Math.abs(maxX-minX);
		float ySpan = Math.abs(maxY-minY);
		// |------ 1024 ------|
		// maxX*resolutionX = xSpan; resolutionX = xSpan/maxX
		// maxY*resolutionY = ySpan; resolutionY = ySpan/maxY
		this.resolutionX = xSpan/maxX;
		this.resolutionY = ySpan/maxY;
		
		final Vector<Color> colors = new Vector<Color>();
		final Vector<Vec2> centers = new Vector<Vec2>();
		Random rand = new Random(1231234);
    	
		for (Polygon p : polys) {
			Vector<Vec2> vertices = p.getFullSpaceRepresentation();
			int[] xCoords = new int[vertices.size()];
			int[] yCoords = new int[vertices.size()];
			for (int i = 0; i < vertices.size(); i++) {
				xCoords[i] = (int)Math.round((vertices.get(i).x*ZOOM*resolutionX));
				yCoords[i] = (int)Math.round((vertices.get(i).y*ZOOM*resolutionY));
			}
	    	centers.add(new Vec2(p.getPosition().x*ZOOM*resolutionX,p.getPosition().y*ZOOM*resolutionY));
			java.awt.Polygon drawablePoly = new java.awt.Polygon(xCoords, yCoords, xCoords.length);
			drawablePolys.add(drawablePoly);
			float r = rand.nextFloat();
			float g = rand.nextFloat();
			float b = rand.nextFloat();
			colors.add(new Color(r,g,b));
		}
		
        JPanel p = new JPanel() {
			private static final long serialVersionUID = 8429745665836985179L;

			@Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int counter = 0;
                for (java.awt.Polygon dPoly : drawablePolys) {
                    g.setColor(colors.get(counter));
                    ((Graphics2D)g).setStroke(new BasicStroke(5));
                	g.drawPolygon(dPoly);
                	g.setFont(new Font("default", Font.BOLD, 16));
                	g.drawString("P"+counter, (int)Math.round(centers.get(counter).x), (int)Math.round(centers.get(counter).y));
                	counter++;
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

}

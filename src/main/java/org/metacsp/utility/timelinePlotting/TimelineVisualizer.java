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
package org.metacsp.utility.timelinePlotting;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.metacsp.meta.symbolsAndTime.SymbolicTimeline;


/**
 * Utility class for visualizing {@link SymbolicTimeline}s that are published by a {@link TimelinePublisher}.
 * @author Federico Pecora
 *
 */
public class TimelineVisualizer extends JFrame {    

	private static final long serialVersionUID = -7341272226915075078L;
	private JPanel panel;
	private ImageIcon icon;
	private BufferedImage image = null;

	/**
	 * Force the visualizer to display a given image.
	 * @param im The image to be displayed.
	 */
	public void setImage(BufferedImage im) {
		icon.setImage(im);
		if (image == null) {
			this.image = im;
//			this.setSize(image.getWidth(), image.getHeight());
			int panelX = image.getWidth(); 
			int panelY = image.getHeight(); 
			panel.setSize(panelX, panelY);

//			Toolkit tk = Toolkit.getDefaultToolkit();  
//			int xSize = ((int) tk.getScreenSize().getWidth());  
//			int ySize = ((int) tk.getScreenSize().getHeight());
			
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedLookAndFeelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			Rectangle bounds = ge.getMaximumWindowBounds();

			int deltaX = 30;
			int deltaY = 50;
			int xSize = Math.min(bounds.width, panelX+deltaX);
			int ySize = Math.min(bounds.height, panelY+deltaY);
			
			this.setPreferredSize(new Dimension(xSize, ySize));
			
			this.setResizable(true);
			this.pack();
		}
		this.getContentPane().repaint();
		if (!this.isVisible()) this.setVisible(true);
	}

	/**
	 * Creates a new {@link TimelineVisualizer} that will open a window that shows
	 * {@link SymbolicTimeline}s published by the given {@link TimelinePublisher}.
	 * @param tp The {@link TimelinePublisher} that publishes the {@link SymbolicTimeline}s.
	 */
	public TimelineVisualizer(TimelinePublisher tp) {
		tp.registerTimelineVisualizer(this);
		panel = new JPanel();
		panel.setBackground(Color.GRAY);
		icon = new ImageIcon();
		JLabel label = new JLabel();
		label.setIcon(icon); 
		panel.add(label);
		JScrollPane sp = new JScrollPane(panel);
		sp.setAutoscrolls(true);
		this.getContentPane().add(sp);
		this.setVisible(false);
	}
	
}

package utility.timelinePlotting;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import meta.symbolsAndTime.SymbolicTimeline;


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

			int deltaX = 70;
			int deltaY = 30;
			int xSize = Math.min(bounds.width-deltaX, panelX);
			int ySize = Math.min(bounds.height-deltaY, panelY);
			
			System.out.println("x and y " + xSize + " " + ySize);
			
			this.setPreferredSize(new Dimension(xSize, ySize));
			
			this.setResizable(false);
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

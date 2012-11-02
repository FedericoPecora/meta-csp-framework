package utility.timelinePlotting;

import java.awt.Color;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import meta.symbolsAndTime.SymbolicTimeline;


/**
 * Utility class for visualizing {@link SymbolicTimeline}s that are published by a {@link TimelinePlotter}.
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
			panel.setSize(image.getWidth(), image.getHeight());
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
		this.getContentPane().add(panel);
		this.setVisible(false);
	}
	
}

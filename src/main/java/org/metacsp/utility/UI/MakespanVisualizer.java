package org.metacsp.utility.UI;

import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TreeMap;

import javax.swing.JWindow;

import org.metacsp.utility.UI.DigitalClock.RobotTaskStates;

public abstract class MakespanVisualizer extends JWindow {
	
	private static final long serialVersionUID = 6498078083115816518L;
	private DigitalClock dc = null;
	
	public abstract long getTime();
	
	public void setCompletionDate(long millis) {
		dc.setCompletionDate(millis);
	}

	public MakespanVisualizer() {
		this(0,0,new TreeMap<String,RobotTaskStates>());
	}

	public MakespanVisualizer(int bufferX, int bufferY, TreeMap<String,RobotTaskStates> currentRobotTasks) {
        super();
        this.setSize(500, 210+DigitalClock.bufferForNumRobots*currentRobotTasks.entrySet().size()); //added 60 for robot status
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
        Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
        int x = (int)rect.getMaxX()-this.getWidth()-bufferX;
        int y = (int)rect.getMaxY()-this.getHeight()-bufferY;
        this.setLocation(x,y);
        setBackground(new Color(0,0,0,0));
        setAlwaysOnTop(true);
        dc = new DigitalClock((long)(-1),currentRobotTasks) {
			private static final long serialVersionUID = -6677185300899092715L;
			@Override
			public long getCurrentTime() {
				return getTime();
			}
        };
        this.add(dc);
        this.setVisible(true);
    }
	
	private long getMillis(int h, int m, int s) {
		return s*1000+m*60*1000+h*60*60*1000;
	}

	public static void main(String[] args) {
		TreeMap<String,RobotTaskStates> currentTaskStatesForReport = new TreeMap<String,RobotTaskStates>();
		currentTaskStatesForReport.put("R2",RobotTaskStates.OTH);
		currentTaskStatesForReport.put("R3",RobotTaskStates.PRK);
		currentTaskStatesForReport.put("R1",RobotTaskStates.DRL);
		currentTaskStatesForReport.put("R5",RobotTaskStates.DRL);
		currentTaskStatesForReport.put("R4",RobotTaskStates.LVL);
		final long origin = Calendar.getInstance().getTimeInMillis();
		MakespanVisualizer mv = new MakespanVisualizer(50,50,currentTaskStatesForReport) {
			private static final long serialVersionUID = -911507104369026648L;
			@Override
			public long getTime() {
				return (long)(origin);
			}
		};
		mv.setCompletionDate(origin+230000);

	}
}
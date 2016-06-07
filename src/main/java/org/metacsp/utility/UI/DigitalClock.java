package org.metacsp.utility.UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.swing.JPanel;

/**
 * 
 * @author Federico Pecora, from original source by "The Man" (diego.caponera@gmail.com)
 *
 */

public abstract class DigitalClock extends JPanel implements Runnable {

	public static enum RobotTaskStates {OTH,DRV,LVL,DVL,PRK,DRL};

	public static int bufferForNumRobots = 22; //per robot
	private int numRobots = 0;
	private static final long serialVersionUID = 8979887823705469862L;
	private Color digitColor2 = new Color(1.0f,0.2f,0.2f,1.0f);
//	private Color digitColor = new Color(1.0f,0.2f,0.2f,0.8f);
//	private Color transpColor = new Color(230,230,230);
//	private Color transpColor = new Color(0.0f,0.0f,0.0f,0.0f);
	private Color transpColor = hex2Rgb("#64485b");
	private boolean ttc = true;	
	private int size=7;
	private DigitalNumber h1,h2,m1,m2,s1,s2;
	private boolean pulse = false;
	private Thread th;
	private Date completionDate = null;
	private String hS = "";
	private String mS = "";
	private String sS = "";
	private SimpleDateFormat formatterHH = new SimpleDateFormat("HH");
	private SimpleDateFormat formattermm = new SimpleDateFormat("mm");
	private SimpleDateFormat formatterss = new SimpleDateFormat("ss");
	
	private TreeMap<String,RobotTaskStates> currentRobotTasks = null;
	
	public static Color hex2Rgb(String colorStr) {
	    return new Color(
	            Integer.valueOf( colorStr.substring( 1, 3 ), 16 ),
	            Integer.valueOf( colorStr.substring( 3, 5 ), 16 ),
	            Integer.valueOf( colorStr.substring( 5, 7 ), 16 ) );
	}
	
	
	public DigitalClock(long completionTime, TreeMap<String,RobotTaskStates> currentRobotTasks) {
		this.currentRobotTasks = currentRobotTasks;
		this.numRobots = currentRobotTasks.entrySet().size();
		if (completionTime != -1) this.completionDate = new Date(completionTime);		
		h1=new DigitalNumber(20,110+numRobots*bufferForNumRobots,size);
		h2=new DigitalNumber(100,110+numRobots*bufferForNumRobots,size);
		m1=new DigitalNumber(200,110+numRobots*bufferForNumRobots,size);
		m2=new DigitalNumber(280,110+numRobots*bufferForNumRobots,size);
		s1=new DigitalNumber(360,70+numRobots*bufferForNumRobots,size/2);
		s2=new DigitalNumber(400,70+numRobots*bufferForNumRobots,size/2);
		
		//setBackground(Color.WHITE);
		setOpaque(false);
//		setBackground(new Color(0,0,0,0));
		setLayout(new BorderLayout());
		
		this.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) { }
			
			@Override
			public void mousePressed(MouseEvent e) { }
			
			@Override
			public void mouseExited(MouseEvent e) { }
			
			@Override
			public void mouseEntered(MouseEvent e) { }
			
			@Override
			public void mouseClicked(MouseEvent e) { ttc = !ttc; }
		});
		
		start();
	}
	
	public void start() {
		if(th==null) {
			th=new Thread(this);
			th.start();
		}
	}
	
	public abstract long getCurrentTime();
	
	public void setCompletionDate(long millis) {
		this.completionDate = new Date(millis);
	}
	
	public void run() {
		while(th!=null) {
			try {
				setHMS();
				showTime();
				pulse = !pulse;
				repaint();
				Thread.sleep(1000);
			} catch(Exception e) {}
		}
	}
	
	public void stop() {
		if(th!=null)th=null;
	}

	public void setHMS() {
		if (completionDate == null) {
			hS = "-";
			mS = "-";
			sS = "-";
		}
		else {
			if (!ttc) {
				hS = formatterHH.format(completionDate);
				mS = formattermm.format(completionDate);
				sS = formatterss.format(completionDate);
			}
			else {
				long dst = 0;
				//if (TimeZone.getDefault().inDaylightTime(new Date())) dst = 3600000;
				dst = 3600000;
				long deltaTime = completionDate.getTime() - getCurrentTime() - dst;
				hS = formatterHH.format(deltaTime);
				mS = formattermm.format(deltaTime);
				sS = formatterss.format(deltaTime);
			}
		}
	}
	
	public void showTime() {
		if (hS.equals("-")) {
			h1.setNumber(-1);
			h2.setNumber(-1);
			m1.setNumber(-1);
			m2.setNumber(-1);
			s1.setNumber(-1);
			s2.setNumber(-1);			
		}
		else {
			if (hS.charAt(0) == '0') h1.turnOffNumber();
			else h1.setNumber(Integer.parseInt(""+hS.charAt(0)));
			h2.setNumber(Integer.parseInt(""+hS.charAt(1)));
			m1.setNumber(Integer.parseInt(""+mS.charAt(0)));
			m2.setNumber(Integer.parseInt(""+mS.charAt(1)));
			s1.setNumber(Integer.parseInt(""+sS.charAt(0)));
			s2.setNumber(Integer.parseInt(""+sS.charAt(1)));
		}
	}
	
	public void showDots(Graphics2D g2) {
		g2.setColor(digitColor2);
		if (pulse) g2.setColor(transpColor);
		g2.fill(new Rectangle2D.Double(178,75+numRobots*bufferForNumRobots,14,14));
		g2.fill(new Rectangle2D.Double(178,145+numRobots*bufferForNumRobots,14,14));
	}
	
	public void showMode(Graphics2D g2) {
		if (ttc) g2.setColor(transpColor);
		else g2.setColor(digitColor2);
		g2.drawString("COMPLETION", 360, 125+numRobots*bufferForNumRobots); 
		g2.drawString("TIME", 360, 145+numRobots*bufferForNumRobots); 
		if (!ttc) g2.setColor(transpColor);
		else g2.setColor(digitColor2);
		g2.drawString("TIME TO", 360, 170+numRobots*bufferForNumRobots);
		g2.drawString("COMPLETION", 360, 190+numRobots*bufferForNumRobots);
		String legend = "OTH/Other | DRV/Drive | LVL/Level | DVL/Delevel | PRK/Park | DRL/Drill";
		//Write robot states
		if (currentRobotTasks != null) {
			Iterator<Entry<String, RobotTaskStates>> it = currentRobotTasks.entrySet().iterator();
			for (int y = 0; y < currentRobotTasks.entrySet().size(); y++) {
				Entry<String, RobotTaskStates> entry = it.next();
				RobotTaskStates state = entry.getValue();
				g2.setColor(digitColor2);
				g2.drawString(entry.getKey(), 20, (y+1)*bufferForNumRobots);
//				g2.setColor(transpColor);
				g2.drawString("-", 60, (y+1)*bufferForNumRobots);
				for (RobotTaskStates oneState : RobotTaskStates.values()) {
					if (oneState.equals(state)) g2.setColor(digitColor2);						
					else g2.setColor(transpColor);
					int spaces = (oneState.ordinal()+1)*63;
					g2.drawString(oneState.toString(), 20+spaces, (y+1)*bufferForNumRobots);
				}
			}
			g2.setColor(digitColor2);
			g2.setFont(g2.getFont().deriveFont(11.0f));
			g2.drawString(legend, 20, (currentRobotTasks.entrySet().size()+1)*bufferForNumRobots);
			g2.setFont(g2.getFont().deriveFont(16.0f));
		}

	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setFont(g.getFont().deriveFont(16.0f));
		g.setFont(g.getFont().deriveFont(Font.BOLD));
		Graphics2D g2 = (Graphics2D)g;
		h1.drawNumber(g2);
		h2.drawNumber(g2);
		m1.drawNumber(g2);
		m2.drawNumber(g2);
		s1.drawNumber(g2);
		s2.drawNumber(g2);
		showDots(g2);
		showMode(g2);
	}
		
	private class DigitalNumber {
		int x,y;
		int k;
		Led[] leds;
		
		public DigitalNumber(int x, int y, int k) {
			this.x=x;
			this.y=y;
			this.k=k;
			leds = new Led[7];
			leds[0] = new Led(x,y,"vert");
			leds[1] = new Led(x,y+10*k,"vert");
			leds[2] = new Led(x+8*k,y,"vert");
			leds[3] = new Led(x+8*k,y+10*k,"vert");
			leds[4] = new Led(x+2*k,y-9*k,"horiz");
			leds[5] = new Led(x+2*k,y+k,"horiz");
			leds[6] = new Led(x+2*k,y+11*k,"horiz");
		}
		
		public void setNumber(int num) {
			if(num==-1) {
				leds[0].setState(false);
				leds[1].setState(false);
				leds[2].setState(false);
				leds[3].setState(false);
				leds[4].setState(false);
				leds[5].setState(true);
				leds[6].setState(false);
			}
			else if(num==0) {
				leds[0].setState(true);
				leds[1].setState(true);
				leds[2].setState(true);
				leds[3].setState(true);
				leds[4].setState(true);
				leds[5].setState(false);
				leds[6].setState(true);
			}
			else if(num==1) {
				leds[0].setState(false);
				leds[1].setState(false);
				leds[2].setState(true);
				leds[3].setState(true);
				leds[4].setState(false);
				leds[5].setState(false);
				leds[6].setState(false);			
			}
			else if(num==2) {
				leds[0].setState(false);
				leds[1].setState(true);
				leds[2].setState(true);
				leds[3].setState(false);
				leds[4].setState(true);
				leds[5].setState(true);
				leds[6].setState(true);			
			}
			else if(num==3) {
				leds[0].setState(false);
				leds[1].setState(false);
				leds[2].setState(true);
				leds[3].setState(true);
				leds[4].setState(true);
				leds[5].setState(true);
				leds[6].setState(true);			
			}
			else if(num==4) {
				leds[0].setState(true);
				leds[1].setState(false);
				leds[2].setState(true);
				leds[3].setState(true);
				leds[4].setState(false);
				leds[5].setState(true);
				leds[6].setState(false);			
			}
			else if(num==5) {
				leds[0].setState(true);
				leds[1].setState(false);			
				leds[2].setState(false);
				leds[3].setState(true);
				leds[4].setState(true);
				leds[5].setState(true);
				leds[6].setState(true);			
			}
			else if(num==6) {
				leds[0].setState(true);
				leds[1].setState(true);			
				leds[2].setState(false);
				leds[3].setState(true);
				leds[4].setState(true);
				leds[5].setState(true);
				leds[6].setState(true);			
			}
			else if(num==7) {
				leds[0].setState(false);
				leds[1].setState(false);
				leds[2].setState(true);
				leds[3].setState(true);
				leds[4].setState(true);
				leds[5].setState(false);
				leds[6].setState(false);		
			}
			else if(num==8) {
				leds[0].setState(true);
				leds[1].setState(true);
				leds[2].setState(true);
				leds[3].setState(true);
				leds[4].setState(true);
				leds[5].setState(true);
				leds[6].setState(true);		
			}
			else if(num==9) {
				leds[0].setState(true);
				leds[1].setState(false);
				leds[2].setState(true);
				leds[3].setState(true);
				leds[4].setState(true);
				leds[5].setState(true);
				leds[6].setState(true);		
			}
		}
		
		public void turnOffNumber() {
			for(int i=0;i<7;i++) {
				leds[i].setState(false);
			}
		}
		
		public void drawNumber(Graphics2D g2) {
			for(int i=0; i<7; i++) {
				leds[i].render(g2);
			}
		}
		
		private class Led {
			int x, y;
			Polygon p;
			String type;
			boolean lightOn=false;
			
			public Led(int x, int y, String type) {
				this.x=x;
				this.y=y;
				this.type=type;

				p = new Polygon();
							
				if(type=="vert") {
					p.addPoint(x,y);
					p.addPoint(x+k,y+k);
					p.addPoint(x+2*k,y);
					p.addPoint(x+2*k,y-8*k);
					p.addPoint(x+k,y-9*k);
					p.addPoint(x,y-8*k);
				}
				
				if(type=="horiz") {
					p.addPoint(x,y);
					p.addPoint(x+k,y+k);
					p.addPoint(x+5*k,y+k);
					p.addPoint(x+6*k,y);
					p.addPoint(x+5*k,y-k);
					p.addPoint(x+k,y-k);				
				}
			}
			
			public void render(Graphics2D g2) {
				g2.setColor(transpColor);
				if(lightOn) g2.setColor(digitColor2);				
				g2.fillPolygon(p);
			}
			
			public void setState(boolean s) {
				lightOn = s;
			}
		}
	}
}

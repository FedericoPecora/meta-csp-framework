package org.metacsp.utility.logging;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.event.CaretEvent;
import javax.swing.text.BadLocationException;

public class LogBrowser extends JFrame {

	private static final long serialVersionUID = 3215265117700965721L;

	private JTabbedPane tabbedPane1 = null;
	private JTabbedPane tabbedPane2 = null;

	public static HashMap<String,TreeMap<Long,Integer>> timeToLine = new HashMap<String,TreeMap<Long,Integer>>();
	public static HashMap<String,TreeMap<Integer,Long>> lineToTime = new HashMap<String,TreeMap<Integer,Long>>();
	public static HashMap<String,JTextPane> tps1 = new HashMap<String,JTextPane>();
	public static HashMap<String,JTextPane> tps2 = new HashMap<String,JTextPane>();

	public LogBrowser() {
		init();
		tabbedPane1 = new JTabbedPane();
		tabbedPane2 = new JTabbedPane();
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabbedPane1, tabbedPane2);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation((int)(getHeight()/1.467));
		add(splitPane);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	private void addTab(final String tabName) {
		// Set up the panel
		JPanel aPanel1 = new JPanel(new BorderLayout());
		JPanel aPanel2 = new JPanel(new BorderLayout());
		final JTextPane tp1 = new JTextPane();
		final JTextPane tp2 = new JTextPane();
		tp1.setFont(new Font("monospaced", Font.PLAIN, 12));
		tp1.getCaret().setVisible(true);
		tp1.setEditable(false);
		aPanel1.add(tp1);
		tp2.setFont(new Font("monospaced", Font.PLAIN, 12));
		tp2.getCaret().setVisible(true);
		tp2.setEditable(false);
		aPanel2.add(tp2);
		JScrollPane sp1 = new JScrollPane(aPanel1);
		JScrollPane sp2 = new JScrollPane(aPanel2);

		final LinePainter lp1 = new LinePainter(tp1, Color.decode("#e6e600")){
			@Override
			public void caretUpdate(CaretEvent e) {
				super.caretUpdate(e);
				if (tp1.hasFocus()) {
					for (Entry<String,JTextPane> entry : tps1.entrySet()) {
						if (!entry.getValue().equals(tp1)) {
							int thisLine = getCurrentLine();
							JTextPane otherTP = entry.getValue();
							String otherTPName = entry.getKey();
							int line = getLine(getTime(thisLine, tabName), otherTPName);
							otherTP.setCaretPosition(otherTP.getDocument().getDefaultRootElement().getElement(line).getStartOffset());
							//System.out.println("Changed " + otherTPName + " to line " + line + " (thisLine is " + thisLine + ")");
						}
					}
					for (Entry<String,JTextPane> entry : tps2.entrySet()) {
						if (!entry.getValue().equals(tp1)) {
							int thisLine = getCurrentLine();
							JTextPane otherTP = entry.getValue();
							String otherTPName = entry.getKey();
							int line = getLine(getTime(thisLine, tabName), otherTPName);
							otherTP.setCaretPosition(otherTP.getDocument().getDefaultRootElement().getElement(line).getStartOffset());
							//System.out.println("Changed " + otherTPName + " to line " + line + " (thisLine is " + thisLine + ")");
						}
					}
				}
			}
		};
		tp1.addCaretListener(lp1);

		final LinePainter lp2 = new LinePainter(tp2, Color.decode("#e6e600")){
			@Override
			public void caretUpdate(CaretEvent e) {
				super.caretUpdate(e);
				if (tp2.hasFocus()) {
					for (Entry<String,JTextPane> entry : tps1.entrySet()) {
						if (!entry.getValue().equals(tp2)) {
							int thisLine = getCurrentLine();
							JTextPane otherTP = entry.getValue();
							String otherTPName = entry.getKey();
							int line = getLine(getTime(thisLine, tabName), otherTPName);
							otherTP.setCaretPosition(otherTP.getDocument().getDefaultRootElement().getElement(line).getStartOffset());
							//System.out.println("Changed " + otherTPName + " to line " + line + " (thisLine is " + thisLine + ")");
						}
					}
					for (Entry<String,JTextPane> entry : tps2.entrySet()) {
						if (!entry.getValue().equals(tp2)) {
							int thisLine = getCurrentLine();
							JTextPane otherTP = entry.getValue();
							String otherTPName = entry.getKey();
							int line = getLine(getTime(thisLine, tabName), otherTPName);
							otherTP.setCaretPosition(otherTP.getDocument().getDefaultRootElement().getElement(line).getStartOffset());
							//System.out.println("Changed " + otherTPName + " to line " + line + " (thisLine is " + thisLine + ")");
						}
					}
				}
			}
		};
		tp2.addCaretListener(lp2);

		// Add the panel to the tabbed pane
		tps1.put(tabName, tp1);
		tps2.put(tabName, tp2);
		tabbedPane1.addTab(tabName, sp1);
		tabbedPane2.addTab(tabName, sp2);
	}

	private void init() {
		setTitle("MetaCSP LogBrowser");
		setSize(1024, 768);
		setLocationRelativeTo(null);
		setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
	}

	public static void showLogs(String dir) {
		LogBrowser lb = new LogBrowser();
		File folder = new File(dir);
		lb.setTitle(lb.getTitle() + " - " + folder.getAbsolutePath());
		File[] listOfFiles = folder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".log");
			}
		});
		
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				int lineNum = 0;
				try {
					BufferedReader br = new BufferedReader(new FileReader(listOfFiles[i]));
					boolean added = false;
					String line;
					while ((line = br.readLine()) != null) {
						if (!added) {
							added = true;
							lb.addTab(listOfFiles[i].getName());
							timeToLine.put(listOfFiles[i].getName(),new TreeMap<Long,Integer>());
							lineToTime.put(listOfFiles[i].getName(),new TreeMap<Integer,Long>());
						}
						Long timeStamp = Long.parseLong(line.substring(0, line.indexOf("@")));
						String rest = line.substring(line.indexOf("@")+1);
						timeToLine.get(listOfFiles[i].getName()).put(timeStamp, lineNum);
						lineToTime.get(listOfFiles[i].getName()).put(lineNum, timeStamp);
						lineNum++;
						JTextPane tp1 = tps1.get(listOfFiles[i].getName());
						JTextPane tp2 = tps2.get(listOfFiles[i].getName());
						Date date = new Date(timeStamp);
						DateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");
						String dateFormatted = formatter.format(date);
						tp1.getDocument().insertString(tp1.getDocument().getEndPosition().getOffset()-1, dateFormatted + " " + rest+"\n", null);
						tp2.getDocument().insertString(tp2.getDocument().getEndPosition().getOffset()-1, dateFormatted + " " + rest+"\n", null);
					}
					br.close();
				}
				catch (FileNotFoundException e) { e.printStackTrace(); }
				catch (IOException e) { e.printStackTrace(); }
				catch (BadLocationException e) { e.printStackTrace(); }
			}
		}
	}
	
	private int getLine(long time, String logName) {
		TreeMap<Long,Integer> timeToLineMap = timeToLine.get(logName);
		int prevLine = 0;
		for (Entry<Long,Integer> e : timeToLineMap.entrySet()) {
			if (e.getKey().longValue() <= time) {
				prevLine = e.getValue();
			}
			else break;
		}
		return prevLine;
	}

	private long getTime(int line, String logName) {
		TreeMap<Integer,Long> lineToTimeMap = lineToTime.get(logName);
		long prevTime = 0;
		for (Entry<Integer,Long> e : lineToTimeMap.entrySet()) {
			if (e.getKey().intValue() <= line) {
				prevTime = e.getValue();
			}
			else break;
		}
		return prevTime;
	}

}

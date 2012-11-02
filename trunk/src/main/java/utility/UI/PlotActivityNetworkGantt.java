 package utility.UI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Date;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import meta.symbolsAndTime.SymbolicTimeline;
import multi.activity.ActivityNetworkSolver;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.renderer.category.GanttRenderer;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;

public class PlotActivityNetworkGantt extends JFrame {
	ChartPanel chartPanel;

	ActivityNetworkSolver solver;
	Vector<String> selectedVariables;
	
	private static final long serialVersionUID = 1L;

	/**
	 * Create a new Gantt JFrame
	 * @param s {@link ActivityNetworkSolver} to be plotted as Gantt
	 * @param selectedVariables {@link Vector} of {@link ActivityNetworkSolver}'s component names (variable names) that will be plotted.
	 * @param n {@link JFrame} title
	 */
	public PlotActivityNetworkGantt(ActivityNetworkSolver s, Vector<String> selectedVariables, String n ) {
		super(n);
		this.solver = s;
		this.selectedVariables = selectedVariables;

		GanttRenderer renderer = new GanttRenderer();
		renderer.setBaseItemLabelFont(new Font("Tahoma", Font.PLAIN, 11));
		
		JFreeChart chart = ChartFactory.createGanttChart(null,// "Channel", //
				"Activities & Resources", // domain axis label
				null, // "Time", // range axis label
				createDataset(), // data
				false, // do not include legend
				false, // no tooltips
				false // urls
				);
		
		chart.getCategoryPlot().setRenderer(renderer);
		renderer.setSeriesPaint(0, Color.green.darker());
		renderer.setSeriesPaint(1, Color.red.darker());
		renderer.setItemMargin(-0.5);

		chart.getCategoryPlot().setRangeAxis(new NumberAxis());
		
		chart.getCategoryPlot().getRangeAxis().setLabelFont(new Font("Arial", Font.PLAIN, 14));
		chart.getCategoryPlot().getDomainAxis().setLabelFont(new Font("Arial", Font.PLAIN, 14));
		chart.getCategoryPlot().getDomainAxis().setTickLabelsVisible(true);
		chart.getCategoryPlot().getRangeAxis().setAutoRange(false);

		chartPanel = new ChartPanel(chart);
		chartPanel.setDomainZoomable(true);
		chartPanel.setRangeZoomable(true);
	
		setContentPane(new JScrollPane(chartPanel));
		this.setPreferredSize(new Dimension(800,600));
		this.setSize(new Dimension(800,600));
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setVisible(true);
	}

	/**
	 * Creates a sample data set for a Gantt chart.
	 * 
	 * @return The data set.
	 */
	private IntervalCategoryDataset createDataset() {
		TaskSeries ts = null;
		TaskSeriesCollection collection = new TaskSeriesCollection();
		if ( solver.getVariables().length == 0) {
			return collection;
		}

		ts = new TaskSeries("All");

		for (int i = 0; i < solver.getVariables().length ; i++) {
						
			String label = solver.getVariables()[i].getComponent(); //dn.getNodes().elementAt(i).getLabel();
			
			if ( this.selectedVariables == null || this.selectedVariables.contains( label ) ) {
			
				SymbolicTimeline tl1 = new SymbolicTimeline(solver,label);
	
				for (int j = 0 ; j < tl1.getPulses().length-1 ; j++  ) {
					
					if ( tl1.getValues()[j] != null ) {
						long startTime = tl1.getPulses()[j].longValue();
						long endTime =   startTime + tl1.getDurations()[j].longValue();					
						
						Date startTask = new Date(startTime);
						Date endTask = new Date(endTime);
						Task task;
						
						String value = tl1.getValues()[j].toString().replace("[", "").replace("]", "");
						if ( value.equals("true") )
							task = new Task(label, startTask, endTask);
						else
							task = new Task(label + " := " + value, startTask, endTask);
								
					
						ts.add(task);	
					}
				}
			}
		}
		
		collection.add(ts);

		return collection;
	}
}
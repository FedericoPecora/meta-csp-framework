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
package org.metacsp.utility.UI;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.TextAnchor;
import org.metacsp.multi.activity.Timeline;



//ApplicationFrame
public class PlotBoxTLSmall extends JPanel {
	Timeline stl;
	Long[] startTimes;
	Long[] durations;
	Object[] values;
	boolean first;
	boolean last;
	
	private JFreeChart chart;
	
	/**
     * A custom label generator.
     */
     class LabelGenerator extends StandardCategoryItemLabelGenerator {
    	 

		private static final long serialVersionUID = 1L;
		private boolean show;
		
		public LabelGenerator(boolean sh) {
			super();
			show = sh;
		}

		/**
         * Generates an item label.
         * 
         * @param dataset  the dataset.
         * @param series  the series index.
         * @param category  the category index.
         * 
         * @return the label.
         */
        public String generateLabel(CategoryDataset dataset, int series, 
                                    int category) {
        	
        	//String label = "(null)";
        	String label = "";
        	if (values[new Integer(dataset.getRowKey(series).toString()).intValue()] != null) {
	        	label = new String(values[new Integer(dataset.getRowKey(series).toString()).intValue()].toString());
	        	//label = label.replaceAll(", ", "\n");
        	}
        	//getPositiveItemLabelPositionFallback() <-- this is called when it doesn't fit.
        	//if (!renderer.isItemLabelVisible(series, category))
            //return label;
        	if (show)
        		return label;
            return "";
        }
    }

     /**
      * @author PST 
      *
      */
     class PlotBoxTooltip implements CategoryToolTipGenerator {

     	//private String description = "";
     	
     	//public OnBoardTooltip(String description)
//     	{
     	//	this.description = description;
     	//}

     	/* (non-Javadoc)
     	 * @see org.jfree.chart.labels.ategoryToolTipGenerator#generateToolTip(org.jfree.data.category.CategoryDataset, int, int)
     	 */
     	public String generateToolTip(CategoryDataset dataset, int series, int item) {
     	
     		String tooltip = "(null)";
     		if (values[series] != null) { 
	     		tooltip = new String(values[series].toString());
	     		tooltip = tooltip.replaceAll(", ", "<br>&nbsp;&nbsp;");
     		}
     		
     		
    
     		return  "<html><body>" + tooltip + "</body></html>";
     		/*/
     		return "";//*/
     	}

		

     }

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String title = "";
	private String name = "";
	private int xsize = 900;
	private Range range = null;
	
	private boolean showlabels = true;

	public PlotBoxTLSmall(Timeline simpletimeline, String n, boolean f, boolean l, long min, long max) {
		this(simpletimeline, n, f, l, min, max, true);
	}
	
	public PlotBoxTLSmall(Timeline simpletimeline, String n, boolean f, boolean l, long min, long max, boolean showlabels) {
		//super(title);
		this.showlabels = showlabels;
		if (min != -1 && max != -1) {
			this.range = new Range(min, max);
		}
		first = f;
		last = l;
		
		this.title = "Simple Timeline";
		this.name = n;
		this.stl = simpletimeline;
		//this.range = Range.scale(range, 0.2);
		//estProfile = (SimpleTimeline) stateVariable.extractTimeline(0);
				
		JFreeChart chart = createChart(createDataset());
		
		ChartPanel chartPanel = new ChartPanel(chart);
		this.add(chartPanel);

		chartPanel.setPreferredSize(new java.awt.Dimension(xsize, 200));
		
		//this.setPreferredSize(new Dimension(500, 400));

	}
	
	public int getXSize() {
		return (int)(xsize+0.1*xsize);
	}

	/**
	 * Get a buffered image of this chart
	 * @param width The width of the image
	 * @param height The height of the image
	 * @return A buffered image of this chart.
	 */
	public BufferedImage getBufferedImage(int width, int height) {
		BufferedImage bi = chart.createBufferedImage(width, height);
		return bi;
	}
	
	/**
	 * Creates a chart for the PlotBoxBehavior
	 * 
	 * @param dataset  A dataset for the chart.
	 * 
	 * @return A chart where the PlotBoxBehavior will be plotted.
	 */
	@SuppressWarnings("deprecation")
	private JFreeChart createChart(CategoryDataset dataset) {

//		String s = name;
		String s = null;
		String tit = null;
		String ax = null;
//		if (first)
//			tit = title + " (EST)";
//		else if (last)
//			ax = "Time";

		tit = this.name;
		
        chart = ChartFactory.createStackedBarChart(
            tit,  // chart title
            s,                  // domain axis label
            ax,                     // range axis label
            dataset,                     // data
            PlotOrientation.HORIZONTAL,    // the plot orientation
            false,                        // legend
            false,                       // tooltips
            false                        // urls
        );
        CategoryPlot plot = chart.getCategoryPlot();

        chart.getTitle().setHorizontalAlignment(HorizontalAlignment.LEFT);
        plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);

        //plot.getCategories();
        //CategoryItemRenderer renderer = plot.getRenderer();
        StackedBarRenderer renderer = (StackedBarRenderer) plot.getRenderer();
        renderer.setItemLabelsVisible(true);
        renderer.setItemLabelGenerator(new LabelGenerator(showlabels));
        ItemLabelPosition pos = new ItemLabelPosition(ItemLabelAnchor.INSIDE1, TextAnchor.TOP_RIGHT);
    	renderer.setPositiveItemLabelPositionFallback(pos);
        for(int i = 0; i < dataset.getRowCount(); i++) {
        	renderer.setSeriesPositiveItemLabelPosition(i, pos);
        }
        
        /*
        if (values.elementAt(0) instanceof ResourceLevel) {
        	renderer.setItemLabelGenerator( new PlotBoxTL.LabelGenerator(true));
        }
        else
        	renderer.setItemLabelGenerator( new PlotBoxTL.LabelGenerator(false));
        */
        renderer.setToolTipGenerator(new PlotBoxTooltip());
        plot.setRenderer(renderer);
       // renderer.getSeriesStroke(0).
        plot.setForegroundAlpha(0.8f);
                        
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setLowerMargin(2.0);
        rangeAxis.setUpperMargin(2.0);
        
        //long origin = stl.getSerializableSimpleTimeline().getEarliestStartTime();
        //long horizon = stl.getSerializableSimpleTimeline().getLatestEndTime();
        long origin = stl.getPulses()[0].longValue();
        NumberFormat nf = new DecimalFormat();
        rangeAxis.setNumberFormatOverride ( nf ); 
        if (this.range != null) rangeAxis.setRange(range);
        //rangeAxis.setRange((new Double(origin)).doubleValue(), (new Double(horizon)).doubleValue());
   
        ///// 0 should be replaced by the start of the horizon
        renderer.setBase(origin);
        
        //renderer.setBase();
        
        

        for (int i = 0; i < durations.length; i++) {
	        if (stl.isInconsistent(values[i]))
	        	renderer.setSeriesPaint(i, new Color(198, 30, 69));
	        else if (stl.isCritical(values[i]))
	        		renderer.setSeriesPaint(i, new Color(238,234,111));
	        else if (stl.isUndetermined(values[i]))
        		renderer.setSeriesPaint(i, new Color(255,255,255));
        	else renderer.setSeriesPaint(i, new Color(111,180,238));
        	renderer.setSeriesOutlinePaint(i, Color.black);
		}
        
        renderer.setBaseSeriesVisibleInLegend(false, false);
        
        renderer.setSeriesStroke(0, new BasicStroke(40f));
        
        return chart;
	}

	/**
	 * Creates a dataset from the bsv vector.
	 * 
	 * @return A dataset.
	 */
	private CategoryDataset createDataset() {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		
		startTimes = new Long[stl.getPulses().length+1];
		startTimes[0] = new Long(0);
		for( int i = 1 ; i < startTimes.length ; i++ ) {
			startTimes[i] = stl.getPulses()[i-1];
		}
		
		durations = new Long[stl.getDurations().length+1];
		durations[0] = new Long(0);
		for( int i = 1 ; i < durations.length ; i++ ) {
			durations[i] = stl.getDurations()[i-1];
		}
		values = new Object[stl.getValues().length+1];
		
		values[0] = null;
		for( int i = 1 ; i < values.length ; i++ ) {
			values[i] = stl.getValues()[i-1];
		}
		for (int i = 0; i < durations.length; i++) {
			//Dbg.printMsg("dur" + i + ": " + durations.elementAt(i), LogLvl.Normal);
			//dataset.addValue((long)durations.elementAt(i), i+"", stateVar.getName());
			dataset.addValue((long)durations[i], i+"", "");
		}
		
		return dataset;
	}
	
	

}

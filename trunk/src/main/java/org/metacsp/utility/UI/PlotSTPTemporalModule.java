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
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.layout.Layout;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.DragControl;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Graph;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.PrefuseLib;
import prefuse.visual.DecoratorItem;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;
import org.metacsp.time.APSPSolver;
import org.metacsp.time.SimpleDistanceConstraint;
import org.metacsp.time.TimePoint;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.Variable;

public class PlotSTPTemporalModule extends Display {
	
	private static final long serialVersionUID = 1L;
	private APSPSolver tm;
	private Graph graph;
	private Visualization vis;
	private	TwoSpringForceLayout tsfdl;
	private DragControl dc;
	
	private ArrayList<Integer> nodeIndices = new ArrayList<Integer>();
    private ArrayList<Long> lbubPositions = new ArrayList<Long>();
    private ArrayList<Integer> lbubLabels = new ArrayList<Integer>();
    private double mult = 10.0;
    private int step = 50;
    
    private final static int minLag = 20;
    
    private final static int lbL = 10000; //Pseudo index for lower bound nodes
    private final static int lbH = 20000; //Pseudo index for upper bound nodes
	
	public PlotSTPTemporalModule(APSPSolver tm, int xSize, int ySize) {
		super();
		this.tm = tm;
		vis = getNodesAndEdges(); 
		setVisualization(vis);

		setSize(xSize, ySize); // set display size
		dc = new DragControl();
		addControlListener(dc); // drag items around
		addControlListener(new PanControl());  // pan with background left-drag
		addControlListener(new ZoomControl()); // zoom with vertical right-drag
		
		vis.run("color");  // assign the colors
		vis.run("layout"); // start up the animated layout		
		
		//touchLBUBNodes();
	}
	
	private class TwoSpringForceLayout extends ForceDirectedLayout { 
				
		public TwoSpringForceLayout(String graph) { 
			super(graph);
		}
		
		@Override
		protected float getSpringCoefficient(EdgeItem e) {
			
			if(e.get(VisualItem.LABEL).equals("LBUB")) {
				return 0.001f;
			}
			return super.getSpringCoefficient(e)*0.000000001f; //10.0f;
		}
		
		@Override
		protected float getSpringLength(EdgeItem e) {
			if(e.get(VisualItem.LABEL).equals("LBUB")) {
				return 0.1f;
			}
			return 200.0f;
			//return super.getSpringLength(e);
		}
		
		@Override
		protected float getMassValue(VisualItem n) {
			return super.getMassValue(n);
		}
		
	}
	
	private static Schema DECORATOR_SCHEMA = PrefuseLib.getVisualItemSchema(); 
	static {
		DECORATOR_SCHEMA.setDefault(VisualItem.INTERACTIVE, false); 
		DECORATOR_SCHEMA.setDefault(VisualItem.TEXTCOLOR, ColorLib.gray(100)); 
		DECORATOR_SCHEMA.setDefault(VisualItem.FONT, FontLib.getFont("Tahoma", 10));
	} 
	
    private static class LabelLayout extends Layout {
        public LabelLayout(String group) {
            super(group);
        }
        
		@Override
		public void run(double frac) {
            Iterator<?> iter = m_vis.items(m_group);
            while ( iter.hasNext() ) {
                DecoratorItem item = (DecoratorItem)iter.next();
                VisualItem node = item.getDecoratedItem();                
                //Dbg.printMsg("--> " + node, LogLvl.Normal);
                Rectangle2D bounds = node.getBounds();
                setX(item, null, bounds.getCenterX());
                setY(item, null, bounds.getCenterY());                
	            if (((String)node.get(m_group)).equals("FAKE")) {
	                item.setTextColor(ColorLib.alpha(0));	                
	            }
	            else if (((String)node.get(m_group)).equals("LBUB")) {
	                item.setTextColor(ColorLib.alpha(0));	                
	            }
            }
        }
    } // end of inner class LabelLayout
    
    
    String tableToString(Table table)
    {
    	StringBuilder strb = new StringBuilder();
    	for(int row = 0; row < table.getRowCount(); row++)
    	{
    		for(int col = 0; col < table.getColumnCount(); col++)
    		{
    			strb.append(table.get(row, col));
    			strb.append(" ");
    		}
    		strb.append("\n");
    	}
    	
    	return strb.toString();
    }
    
    private static String distToTag(Long distance)
    {
    	String str;
    	if(distance == Long.MAX_VALUE - 1)
    		str = "+INF";
    	else if(distance == Long.MIN_VALUE + 1)
    		str = "-INF";
    	else
    		str = "" + distance;
    	return str;
    }
    
    public Visualization getNodesAndEdges() {
		Table tabNodes = new Table();
		tabNodes.addColumn("TPId", int.class);
		tabNodes.addColumn("TPName", java.lang.String.class);
		tabNodes.addColumn("TPType", java.lang.String.class);
		
		Table tabEdges = new Table();
		tabEdges.addColumn("TPFrom", int.class);
		tabEdges.addColumn("TPTo", int.class);
		tabEdges.addColumn(VisualItem.LABEL, java.lang.String.class);
		
        //Make source node
		int ret = tabNodes.addRow();
		tabNodes.set(ret, 0, 0);
		tabNodes.set(ret, 1, "SOURCE");
		tabNodes.set(ret, 2, "BUILT-IN");
        //Dbg.printMsg("Added SOURCE to tab", LogLvl.Normal);

        //Make sink node
		ret = tabNodes.addRow();
		tabNodes.set(ret, 0, 1);
		tabNodes.set(ret, 1, "SINK");
		tabNodes.set(ret, 2, "BUILT-IN");
        //Dbg.printMsg("Added SINK to tab", LogLvl.Normal);
		
		ArrayList<TimePoint> timeInstants = new ArrayList<TimePoint>();
		ArrayList<SimpleDistanceConstraint> simpleDistanceConstraints = new ArrayList<SimpleDistanceConstraint>();
		
		//Decompose the Temporal elements into TimeInstants
		for (Variable e : tm.getVariables()) { timeInstants.add((TimePoint) e); }
		
		//Decompose the Temporal assertions into SimpleDistanceConstraints
		for(Constraint a : tm.getConstraints()) { simpleDistanceConstraints.add((SimpleDistanceConstraint) a); }
		
		//The HashSet is only to assert that we don't add duplicate time points
		//and that all temporal constraints actually lead to something
		HashSet<Integer> tpIds = new HashSet<Integer>();
		tpIds.add(0);
		tpIds.add(1);
		
		//Add the TimeInstant nodes
		for (TimePoint ti : timeInstants)
		{
			if(!tpIds.contains(ti.getID()))
			{
				nodeIndices.add(ti.getID());
				tpIds.add(ti.getID());
			}
			else
			{
				System.err.println("Duplicate time point #" + ti.getID());
				continue;
			}
			
    		ret = tabNodes.addRow();
    		tabNodes.set(ret, 0, ti.getID());
    		
    		tabNodes.set(ret, 1, "TP" + ti.getID());	
    		
    		//Add fake LB/UB nodes as well
    		ret = tabNodes.addRow();
    		tabNodes.set(ret, 0, lbL+ti.getID());
    		tabNodes.set(ret, 1, "[" + distToTag(ti.getLowerBound()));
    		tabNodes.set(ret, 2, "LBUB");
    		
    		ret = tabNodes.addRow();
    		tabNodes.set(ret, 0, lbH+ti.getID());
    		tabNodes.set(ret, 1, distToTag(ti.getUpperBound()) + "]");
    		tabNodes.set(ret, 2, "LBUB");
    		
    		lbubLabels.add(lbL+ti.getID());
    		lbubLabels.add(lbH+ti.getID());

    		lbubPositions.add(ti.getLowerBound());
    		lbubPositions.add(ti.getUpperBound()+minLag);
    		
    		//Add edges
    		ret = tabEdges.addRow();
    		tabEdges.set(ret, 0, lbL+ti.getID());
    		tabEdges.set(ret, 1, ti.getID());
    		tabEdges.set(ret, 2, "LBUB");
    		
    		ret = tabEdges.addRow();
    		tabEdges.set(ret, 0, lbH+ti.getID());
    		tabEdges.set(ret, 1, ti.getID());
    		tabEdges.set(ret, 2, "LBUB");
		}
		
		//Add the SimpleDistanceConstraint edges
		for(SimpleDistanceConstraint sdc : simpleDistanceConstraints)
		{
			if(!tpIds.contains(sdc.getFrom().getID()))
			{
				System.err.println("Missing timepoint #" + sdc.getFrom().getID() + " in " + sdc);
				continue;
			}
			else if(!tpIds.contains(sdc.getTo().getID()))
			{
				System.err.println("Missing timepoint #" + sdc.getTo().getID() + " in " + sdc);
				continue;
			}
			
    		ret = tabEdges.addRow();
    		tabEdges.set(ret, 0, sdc.getFrom().getID());
    		tabEdges.set(ret, 1, sdc.getTo().getID());
    		tabEdges.set(ret, 2, "[" + distToTag(sdc.getMinimum()) + "," + distToTag(sdc.getMaximum()) + "]");
		}
		
		graph = new Graph(tabNodes, tabEdges, true, "TPId", "TPFrom", "TPTo");
		

		if(vis == null)
			vis = new Visualization();
		else
			vis.reset();
		
		vis.add("graph", graph);
		
		//draw the "TPName" label for NodeItems
		LabelRenderer r = new LabelRenderer("TPName");
		r.setRoundedCorner(8, 8); // round the corners

		LabelRenderer r1 = new LabelRenderer("TPName");

		EdgeRenderer er = new EdgeRenderer(Constants.EDGE_TYPE_CURVE, Constants.EDGE_ARROW_FORWARD);
		EdgeRenderer er1 = new EdgeRenderer(Constants.EDGE_TYPE_LINE, Constants.EDGE_ARROW_NONE);
		EdgeRenderer er2 = new EdgeRenderer(Constants.EDGE_TYPE_LINE, Constants.EDGE_ARROW_NONE);
		
		//create a new default renderer factory
		//return our name label renderer as the default for all non-EdgeItems
		//includes straight line edges for EdgeItems by default
		DefaultRendererFactory rf = new DefaultRendererFactory();

		
		Predicate normalEdges = (Predicate)ExpressionParser.parse(("ISEDGE() AND " + VisualItem.LABEL + "<>'FAKE' AND " + VisualItem.LABEL + "<>'LBUB'"), true);
		Predicate notFake = (Predicate)ExpressionParser.parse(("ISEDGE() AND " + VisualItem.LABEL + "<>'FAKE'"), true);
		Predicate fake = (Predicate)ExpressionParser.parse(("ISEDGE() AND " + VisualItem.LABEL + "=='FAKE'"), true);
		Predicate lbub = (Predicate)ExpressionParser.parse(("ISEDGE() AND " + VisualItem.LABEL + "=='LBUB'"), true);
		Predicate lbubNode = (Predicate)ExpressionParser.parse(("ISNODE() AND TPType =='LBUB'"), true);
		Predicate notLbubNode = (Predicate)ExpressionParser.parse(("ISNODE() AND TPType <>'LBUB'"), true);
		rf.add(normalEdges, er);
		rf.add(fake, er1);
		rf.add(lbub, er2);
		rf.add(notLbubNode, r);
		rf.add(lbubNode, r1);
		
		
		LabelRenderer decoRenderer = new LabelRenderer(VisualItem.LABEL);
		rf.add(new InGroupPredicate(VisualItem.LABEL), decoRenderer);
		
		vis.setRendererFactory(rf);
		
		/*
		TupleSet ts = graph.getEdges();
		for (int i = 0; i < ts.getTupleCount(); i++){
			Dbg.printMsg("Edge " + i + ": " + graph.getEdge(i).getString(VisualItem.LABEL), LogLvl.Normal);
		}
		*/
		

		vis.addDecorators(VisualItem.LABEL, "graph.edges", DECORATOR_SCHEMA);
		
		//Create nominal color palette
		//Pink for user timepoints, blue for SOURCE and SINK
		int[] palette = new int[] {
		    ColorLib.rgb(255,180,180), ColorLib.rgb(190,190,255)
		};
		//map nominal data values to colors using our provided palette
		DataColorAction fill = new DataColorAction("graph.nodes", "TPType",
				Constants.NOMINAL, VisualItem.FILLCOLOR, palette);
		//use black for node text
		ColorAction text = new ColorAction("graph.nodes",
				VisualItem.TEXTCOLOR, ColorLib.gray(0));
		//use light grey for edges
		ColorAction edges = new ColorAction("graph.edges", notFake,
				VisualItem.STROKECOLOR, ColorLib.gray(100));
		ColorAction edgesLBUB = new ColorAction("graph.edges", lbub,
				VisualItem.STROKECOLOR, ColorLib.gray(100));
		//use light grey for edge arrows
		ColorAction edgeEnds = new ColorAction("graph.edges",
				VisualItem.FILLCOLOR, ColorLib.gray(100));

		//create an action list containing all color assignments
		ActionList color = new ActionList();
		color.add(fill);
		color.add(text);
		color.add(edges);
		color.add(edgesLBUB);
		color.add(edgeEnds);
		
		//create an action list with an animated layout
		//the INFINITY parameter tells the action list to run indefinitely
		ActionList layout = new ActionList(Activity.INFINITY);
		
		//TwoSpringForceLayout tsfdl = new TwoSpringForceLayout("graph");
		tsfdl = new TwoSpringForceLayout("graph");
		//ForceDirectedLayout fdl = new ForceDirectedLayout("graph");
		//CircleLayout cl = new CircleLayout("graph");
		//AxisLayout al = new AxisLayout("graph", "TPId");
		//CollapsedStackLayout csl = new CollapsedStackLayout("graph", "TPId");
		//FruchtermanReingoldLayout frl = new FruchtermanReingoldLayout("graph");
		//BalloonTreeLayout btl = new BalloonTreeLayout("graph");
		
		
		Tuple src;
		VisualItem srcVis;
		
		//FIX LBUB labels
        Vector<Long> lbubXs = new Vector<Long>();
		//step = step*10;
        double offset = step;
		double xoffset = 250;
	
		double maxX = 0;
		
		for (int i = 0; i < lbubLabels.size(); i++) {
			//Dbg.printMsg(">>>> GETTING " + lbubLabels.elementAt(i).intValue(), LogLvl.Normal);
			src = graph.getNodeFromKey(lbubLabels.get(i).intValue());
			//src = graph.getNode(lbubLabels.elementAt(i).intValue());
			srcVis = vis.getVisualItem("graph.nodes", src);
			srcVis.setFixed(true);
			srcVis.setX(xoffset + mult*lbubPositions.get(i).doubleValue());
			lbubXs.add((long)(xoffset + mult*lbubPositions.get(i)));
			if (i%2==0)
				offset += step;
			srcVis.setY(offset);
			if (maxX < mult*lbubPositions.get(i).doubleValue())
				maxX = mult*lbubPositions.get(i).doubleValue();
		}
		
		//FIX source and sink nodes
		src = graph.getNode(0);
		srcVis = vis.getVisualItem("graph.nodes", src);
		srcVis.setFixed(true);
		srcVis.setX(xoffset-60);
		srcVis.setY(step+offset/2);
		
		src = graph.getNode(1);
		srcVis = vis.getVisualItem("graph.nodes", src);
		srcVis.setFixed(true);
		srcVis.setX(xoffset+maxX+60);
		srcVis.setY(step+offset/2);
		
		layout.add(tsfdl);
		layout.add(new LabelLayout(VisualItem.LABEL));
		layout.add(new RepaintAction());
		
		//add the actions to the visualization
		vis.putAction("color", color);
		vis.putAction("layout", layout);
		
		return vis;
    }
    
    
    public void touchLBUBNodes() {
    	
    	assert lbubLabels.size() == lbubPositions.size();
    	assert 2*nodeIndices.size() == lbubPositions.size();
    	
    	for (int i = 0; i < lbubLabels.size()-1; i+=2) {
			Tuple src = graph.getNodeFromKey(lbubLabels.get(i).longValue());
			VisualItem srcVis = vis.getVisualItem("graph.nodes", src);
			Tuple src1 = null;
			src1 = graph.getNodeFromKey(lbubLabels.get(i+1).longValue());
			VisualItem srcVis1 = vis.getVisualItem("graph.nodes", src1);

	        double dx = 2.0;
	        double dy = -5.0;
	        double x = srcVis.getX();
	        double y = srcVis.getY();

	        srcVis.setStartX(x);  srcVis.setStartY(y);
	        srcVis.setX(x+dx);    srcVis.setY(y+dy);
	        srcVis.setEndX(x+dx); srcVis.setEndY(y+dy);
	        
	        srcVis.getVisualization().repaint();
	        
	        x = srcVis1.getX();
	        y = srcVis1.getY();

	        srcVis1.setStartX(x);  srcVis1.setStartY(y);
	        srcVis1.setX(x+dx);    srcVis1.setY(y+dy);
	        srcVis1.setEndX(x+dx); srcVis1.setEndY(y+dy);
	        
	        srcVis1.getVisualization().repaint();
		}
    	
    	//Put nodes in starting positions
		double offset = step;
		final double xoffset = 250;
		for(int i = 0; i<nodeIndices.size(); i++) {
			Tuple src = graph.getNodeFromKey(nodeIndices.get(i));
		
			VisualItem srcVis = vis.getVisualItem("graph.nodes", src);
			
	        double x = srcVis.getX();
	        double y = srcVis.getY();

	        offset += step;
	        
	        double xPos = mult*((lbubPositions.get(i*2).doubleValue()+lbubPositions.get(i*2+1).doubleValue())/2  );
	        
	        srcVis.setStartX(x);
	        srcVis.setStartY(y);
	        srcVis.setX(xoffset + xPos);
	        srcVis.setY(offset);
	        srcVis.setEndX(xoffset + xPos);
	        srcVis.setEndY(offset);	        
	        srcVis.getVisualization().repaint();
		}
    }
    
}


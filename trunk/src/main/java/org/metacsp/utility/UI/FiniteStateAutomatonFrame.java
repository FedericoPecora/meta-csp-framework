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

/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 */


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Timer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JRootPane;

import org.apache.commons.collections15.Transformer;

import org.metacsp.time.Interval;
import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout2;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.algorithms.layout.util.VisRunner;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.ObservableGraph;
import edu.uci.ics.jung.graph.event.GraphEvent;
import edu.uci.ics.jung.graph.event.GraphEventListener;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.util.Animator;

/**
 * A variation of AddNodeDemo that animates transitions between graph states.
 *
 * @author Tom Nelson
 */
public class FiniteStateAutomatonFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2198471048277328599L;

	private ObservableGraph<String[],Interval> g = null;

    private VisualizationViewer<String[],Interval> vv = null;

    private AbstractLayout<String[],Interval> layout = null;

    Timer timer;

    boolean done;

    protected JButton switchLayout;
    
//    private int layer = 0;

    //public static final int EDGE_LENGTH = 100;
    
    public FiniteStateAutomatonFrame(ObservableGraph<String[],Interval> graph) {
    	super("Constraint Network");

        g = graph;
        g.addGraphEventListener(new GraphEventListener<String[],Interval>() {

			public void handleGraphEvent(GraphEvent<String[],Interval> evt) {
				System.err.println("got "+evt);
				/****/
		    	vv.getRenderContext().getPickedVertexState().clear();
		    	vv.getRenderContext().getPickedEdgeState().clear();
		        try {

		                layout.initialize();

		                try {
		        			Relaxer relaxer = new VisRunner((IterativeContext)layout);
		        			relaxer.stop();
		        			relaxer.prerelax();
		                } catch (java.lang.ClassCastException e) {}
		                
		        		StaticLayout<String[],Interval> staticLayout =
		        			new StaticLayout<String[],Interval>(g, layout);
						LayoutTransition<String[],Interval> lt =
							new LayoutTransition<String[],Interval>(vv, vv.getGraphLayout(),
									staticLayout);
						Animator animator = new Animator(lt);
						animator.start();
//						vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
						vv.repaint();

		        } catch (Exception e) {
		            System.out.println(e);

		        }				
				/****/
			}});

        //create a graphdraw
        //layout = new FRLayout<State,Interval>(g);
        //layout = new SpringLayout<State,Interval>(g);
        //layout = new StaticLayout<State,Interval>(g,new STNTransformer());
        layout = new FRLayout2<String[],Interval>(g);
        //layout = new CircleLayout<State,Interval>(g);
        //layout = new ISOMLayout<State,Interval>(g);
        //layout = new KKLayout<State,Interval>(g);
        layout.setSize(new Dimension(600,600));
        
        
        try {
			Relaxer relaxer = new VisRunner((IterativeContext)layout);
			relaxer.stop();
			relaxer.prerelax();
        } catch (java.lang.ClassCastException e) {}
        
        
        Layout<String[],Interval> staticLayout =
			new StaticLayout<String[],Interval>(g, layout);

        vv = new VisualizationViewer<String[],Interval>(staticLayout, new Dimension(600,600));

        JRootPane rp = this.getRootPane();
        rp.putClientProperty("defeatSystemEventQueueCheck", Boolean.TRUE);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(java.awt.Color.lightGray);
        getContentPane().setFont(new Font("Serif", Font.PLAIN, 12));

        vv.setGraphMouse(new DefaultModalGraphMouse<String[],Interval>());

        vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.S);
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<String[]>());
        vv.setForeground(Color.black);
        
        //draw edge labels
        Transformer<Interval,String> stringer = new Transformer<Interval,String>(){
            public String transform(Interval e) {
                return e.toString();
            }
        };
        
        vv.getRenderContext().setEdgeLabelTransformer(stringer);
        vv.getRenderContext().setEdgeDrawPaintTransformer(new PickableEdgePaintTransformer<Interval>(vv.getPickedEdgeState(), Color.black, Color.cyan));

        vv.addComponentListener(new ComponentAdapter() {

			/**
			 * @see java.awt.event.ComponentAdapter#componentResized(java.awt.event.ComponentEvent)
			 */
			@Override
			public void componentResized(ComponentEvent arg0) {
				super.componentResized(arg0);
				System.err.println("resized");
				layout.setSize(arg0.getComponent().getSize());
			}});

        getContentPane().add(vv);
        switchLayout = new JButton("Switch to SpringLayout");
        switchLayout.addActionListener(new ActionListener() {

            @SuppressWarnings("unchecked")
            public void actionPerformed(ActionEvent ae) {
            	Dimension d = vv.getSize();//new Dimension(600,600);
                if (switchLayout.getText().indexOf("Spring") > 0) {
                    switchLayout.setText("Switch to FRLayout");
                    //layout = new SpringLayout<State,Interval>(g, new ConstantTransformer(EDGE_LENGTH));
                    layout = new SpringLayout<String[],Interval>(g);
                    layout.setSize(d);
                    
                    try {
            			Relaxer relaxer = new VisRunner((IterativeContext)layout);
            			relaxer.stop();
            			relaxer.prerelax();
                    } catch (java.lang.ClassCastException e) {}
            		
            		StaticLayout<String[],Interval> staticLayout =
            			new StaticLayout<String[],Interval>(g, layout);
    				LayoutTransition<String[],Interval> lt =
    					new LayoutTransition<String[],Interval>(vv, vv.getGraphLayout(),
    							staticLayout);
    				Animator animator = new Animator(lt);
    				animator.start();
    			//	vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
    				vv.repaint();

                } else {
                    switchLayout.setText("Switch to SpringLayout");
                    layout = new FRLayout<String[],Interval>(g, d);
                    layout.setSize(d);
                    
                    try {
            			Relaxer relaxer = new VisRunner((IterativeContext)layout);
            			relaxer.stop();
            			relaxer.prerelax();
                    } catch (java.lang.ClassCastException e) {}
                    
            		StaticLayout<String[],Interval> staticLayout =
            			new StaticLayout<String[],Interval>(g, layout);
    				LayoutTransition<String[],Interval> lt =
    					new LayoutTransition<String[],Interval>(vv, vv.getGraphLayout(),
    							staticLayout);
    				Animator animator = new Animator(lt);
    				animator.start();
    			//	vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
    				vv.repaint();

                }
            }
        });

        getContentPane().add(switchLayout, BorderLayout.SOUTH);

    	this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	this.pack();
    	this.setVisible(true);

    }

/*
    public static void main(String[] args) {
    	AnimatingAddNodeDemo and = new AnimatingAddNodeDemo();
    	JFrame frame = new JFrame();
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.getContentPane().add(and);

    	and.init();
    	and.start();
    	frame.pack();
    	frame.setVisible(true);
    }
    */
}

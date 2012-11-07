package utility.UI;

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
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JRootPane;

import org.apache.commons.collections15.Transformer;

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
import framework.Constraint;
import framework.Variable;

/**
 * A variation of AddNodeDemo that animates transitions between graph states.
 *
 * @author Tom Nelson
 */
public class ConstraintNetworkFrame extends JFrame {
	
	private static final Logger logger = Logger.getLogger(ConstraintNetworkFrame.class.getPackage().getName());
	
	private static final long serialVersionUID = 3896624751126451811L;

	private ObservableGraph<Variable,Constraint> g = null;

    private VisualizationViewer<Variable,Constraint> vv = null;

    private AbstractLayout<Variable,Constraint> layout = null;

    //Timer timer;

    boolean done;

    protected JButton switchLayout;
    protected JButton performOperation;
    
    private Callback cb;
    
    //private int layer = 0;

    //public static final int EDGE_LENGTH = 100;
    
//	private class STNTransformer implements Transformer<Variable,Point2D> {
//		@Override
//		public Point2D transform(Variable arg0) {
//			Point ret = new Point((int) ((Long)arg0.getDomain().chooseValue("ET")*20), layer);
//			layer+=100;
//			return ret;
//		}
//	}

    
    public ConstraintNetworkFrame(ObservableGraph<Variable,Constraint> graph, String title, final Callback cb) {
    	super(title);
    	this.cb = cb;
        g = graph;
        g.addGraphEventListener(new GraphEventListener<Variable,Constraint>() {

			@Override
			public void handleGraphEvent(GraphEvent<Variable,Constraint> evt) {
				logger.log(Level.FINE, "Got " + evt);
				
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
		                
		        		StaticLayout<Variable,Constraint> staticLayout =
		        			new StaticLayout<Variable,Constraint>(g, layout);
						LayoutTransition<Variable,Constraint> lt =
							new LayoutTransition<Variable,Constraint>(vv, vv.getGraphLayout(),
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
        //layout = new FRLayout<Variable,Constraint>(g);
        //layout = new SpringLayout<Variable,Constraint>(g);
        //layout = new StaticLayout<Variable,Constraint>(g,new STNTransformer());
        layout = new FRLayout2<Variable,Constraint>(g);
        //layout = new CircleLayout<Variable,Constraint>(g);
        //layout = new ISOMLayout<Variable,Constraint>(g);
        //layout = new KKLayout<Variable,Constraint>(g);
        layout.setSize(new Dimension(600,600));
        
        
        try {
			Relaxer relaxer = new VisRunner((IterativeContext)layout);
			relaxer.stop();
			relaxer.prerelax();
        } catch (java.lang.ClassCastException e) {}
        
        
        Layout<Variable,Constraint> staticLayout =
			new StaticLayout<Variable,Constraint>(g, layout);

        vv = new VisualizationViewer<Variable,Constraint>(staticLayout, new Dimension(600,600));

        JRootPane rp = this.getRootPane();
        rp.putClientProperty("defeatSystemEventQueueCheck", Boolean.TRUE);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(java.awt.Color.lightGray);
        getContentPane().setFont(new Font("Serif", Font.PLAIN, 12));

        vv.setGraphMouse(new DefaultModalGraphMouse<Variable,Constraint>());

        vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.S);
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<Variable>());
        vv.setForeground(Color.black);
        
        //draw edge labels
        Transformer<Constraint,String> stringer = new Transformer<Constraint,String>(){
            @Override
			public String transform(Constraint e) {
                return e.getEdgeLabel();
            }
        };
        
        Transformer<Variable,Paint> vertexPaint = new Transformer<Variable,Paint>() {
            public Paint transform(Variable v) {
                return v.getColor();
            }
        };  
        
        vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
        
        vv.getRenderContext().setEdgeLabelTransformer(stringer);
        vv.getRenderContext().setEdgeDrawPaintTransformer(new PickableEdgePaintTransformer<Constraint>(vv.getPickedEdgeState(), Color.black, Color.cyan));

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
        
        performOperation = new JButton("Perform operation");
        performOperation.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent arg0) {
        		if (cb != null) cb.performOperation();
        	}
        });
        
        switchLayout = new JButton("Switch to SpringLayout");
        switchLayout.addActionListener(new ActionListener() {
        	
        	@Override
            public void actionPerformed(ActionEvent ae) {
            	Dimension d = vv.getSize();//new Dimension(600,600);
                if (switchLayout.getText().indexOf("Spring") > 0) {
                    switchLayout.setText("Switch to FRLayout");
                    //layout = new SpringLayout<Variable,Constraint>(g, new ConstantTransformer(EDGE_LENGTH));
                    layout = new SpringLayout<Variable,Constraint>(g);
                    layout.setSize(d);
                    
                    try {
            			Relaxer relaxer = new VisRunner((IterativeContext)layout);
            			relaxer.stop();
            			relaxer.prerelax();
                    } catch (java.lang.ClassCastException e) {}
            		
            		StaticLayout<Variable,Constraint> staticLayout =
            			new StaticLayout<Variable,Constraint>(g, layout);
    				LayoutTransition<Variable,Constraint> lt =
    					new LayoutTransition<Variable,Constraint>(vv, vv.getGraphLayout(),
    							staticLayout);
    				Animator animator = new Animator(lt);
    				animator.start();
    			//	vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
    				vv.repaint();

                } else {
                    switchLayout.setText("Switch to SpringLayout");
                    layout = new FRLayout<Variable,Constraint>(g, d);
                    layout.setSize(d);
                    
                    try {
            			Relaxer relaxer = new VisRunner((IterativeContext)layout);
            			relaxer.stop();
            			relaxer.prerelax();
                    } catch (java.lang.ClassCastException e) {}
                    
            		StaticLayout<Variable,Constraint> staticLayout =
            			new StaticLayout<Variable,Constraint>(g, layout);
    				LayoutTransition<Variable,Constraint> lt =
    					new LayoutTransition<Variable,Constraint>(vv, vv.getGraphLayout(),
    							staticLayout);
    				Animator animator = new Animator(lt);
    				animator.start();
    			//	vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
    				vv.repaint();

                }
            }
        });

        getContentPane().add(performOperation, BorderLayout.SOUTH);
        //getContentPane().add(switchLayout, BorderLayout.SOUTH);

    	this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
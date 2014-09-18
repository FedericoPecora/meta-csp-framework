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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ConcurrentModificationException;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JRootPane;

import org.apache.commons.collections15.Transformer;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.Variable;

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
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.util.Animator;

/**
 * Based on variation of AddNodeDemo that animates transitions between graph states, by Tom Nelson.
 *
 * @author Federico Pecora
 */
public class ConstraintNetworkFrame extends JFrame {

	private static final Logger logger = Logger.getLogger(ConstraintNetworkFrame.class.getPackage().getName());
	private static final long serialVersionUID = 3896624751126451811L;
	private ObservableGraph<Variable,Constraint> g = null;
	private VisualizationViewer<Variable,Constraint> vv = null;
	private AbstractLayout<Variable,Constraint> layout = null;

	boolean done;
	protected JButton switchLayout;
	protected JButton performOperation;
	private Animator animator = null;
	private LayoutTransition<Variable,Constraint> lt = null;
	
	//An implementation of Animator which ignores ConcurrentModificationExceptions
	private class MyAnimator extends Animator {
		public MyAnimator(IterativeContext process) { super(process); }
		public void run() {
			try { super.run(); }
			catch (ConcurrentModificationException e) { /* ignore */ }
		}
	}

	public ConstraintNetworkFrame(ObservableGraph<Variable,Constraint> graph, String title, final Callback cb) {
		super(title);
		this.g = graph;
		
		g.addGraphEventListener(new GraphEventListener<Variable,Constraint>() {
			@Override
			public void handleGraphEvent(GraphEvent<Variable,Constraint> evt) {
				vv.getRenderContext().getPickedVertexState().clear();
				vv.getRenderContext().getPickedEdgeState().clear();
				try {
					layout.initialize();
					try {
						Relaxer relaxer = new VisRunner((IterativeContext)layout);
						relaxer.stop();
						relaxer.prerelax();
					}
					catch (java.lang.ClassCastException e) { e.printStackTrace(); }

					StaticLayout<Variable,Constraint> staticLayout = new StaticLayout<Variable,Constraint>(g, layout);
					lt = new LayoutTransition<Variable,Constraint>(vv, vv.getGraphLayout(), staticLayout);
					animator = new MyAnimator(lt);
					animator.start();

					//vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
					vv.repaint();
				}
				catch (Exception e) { e.printStackTrace(); }
			}
		});

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
		}
		catch (java.lang.ClassCastException e) { e.printStackTrace(); }

		Layout<Variable,Constraint> staticLayout = new StaticLayout<Variable,Constraint>(g, layout);
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
				try { return e.getEdgeLabel(); }
				catch (NullPointerException ex) { return ""; }
			}
		};

		Transformer<Variable,Paint> vertexPaint = new Transformer<Variable,Paint>() {
			public Paint transform(Variable v) {
				return v.getColor();
			}
		};
		
		Transformer<Constraint,Paint> constraintPaint = new Transformer<Constraint,Paint>() {
			public Paint transform(Constraint c) {
				return c.getColor();
			}
		};  


		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
//		vv.getRenderContext().setEdgeFillPaintTransformer(constraintPaint);
		vv.getRenderContext().setEdgeLabelTransformer(stringer);
//		vv.getRenderContext().setEdgeDrawPaintTransformer(new PickableEdgePaintTransformer<Constraint>(vv.getPickedEdgeState(), Color.black, Color.cyan));
		vv.getRenderContext().setEdgeDrawPaintTransformer(constraintPaint);
		
		
		vv.addComponentListener(new ComponentAdapter() {

			/**
			 * @see java.awt.event.ComponentAdapter#componentResized(java.awt.event.ComponentEvent)
			 */
			@Override
			public void componentResized(ComponentEvent arg0) {
				super.componentResized(arg0);
				layout.setSize(arg0.getComponent().getSize());
			}});

		getContentPane().add(vv);
		performOperation = new JButton("Perform operation");
		if (cb == null) performOperation.setEnabled(false);
		else performOperation.setEnabled(true);

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
					} catch (java.lang.ClassCastException e) { e.printStackTrace(); }

					StaticLayout<Variable,Constraint> staticLayout = new StaticLayout<Variable,Constraint>(g, layout);
					lt = new LayoutTransition<Variable,Constraint>(vv, vv.getGraphLayout(), staticLayout);
					Animator animator = new MyAnimator(lt);
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

					StaticLayout<Variable,Constraint> staticLayout = new StaticLayout<Variable,Constraint>(g, layout);
					lt = new LayoutTransition<Variable,Constraint>(vv, vv.getGraphLayout(), staticLayout);
					Animator animator = new MyAnimator(lt);
					animator.start();
					//vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
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
}

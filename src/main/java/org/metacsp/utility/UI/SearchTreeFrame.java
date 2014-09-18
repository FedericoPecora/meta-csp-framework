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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.apache.commons.collections15.Transformer;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.meta.MetaVariable;

import edu.uci.ics.jung.algorithms.layout.PolarPoint;
import edu.uci.ics.jung.algorithms.layout.RadialTreeLayout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import edu.uci.ics.jung.visualization.util.Animator;

public class SearchTreeFrame extends JFrame {

	private static final long serialVersionUID = -4515896372373179742L;

	/**
	 * the graph
	 */
	Forest<MetaVariable,ConstraintNetwork> graph;

	/**
	 * the visual component and renderer for the graph
	 */
	VisualizationViewer<MetaVariable,ConstraintNetwork> vv;

	VisualizationServer.Paintable rings;

	String root;

	TreeLayout<MetaVariable,ConstraintNetwork> treeLayout;

	RadialTreeLayout<MetaVariable,ConstraintNetwork> radialLayout;

	public SearchTreeFrame(DelegateForest<MetaVariable,ConstraintNetwork> graph) {

		this.graph = graph;
		treeLayout = new TreeLayout<MetaVariable,ConstraintNetwork>(graph);
		radialLayout = new RadialTreeLayout<MetaVariable,ConstraintNetwork>(graph);
		radialLayout.setSize(new Dimension(600,600));
		vv =  new VisualizationViewer<MetaVariable,ConstraintNetwork>(treeLayout, new Dimension(600,600));
		vv.setBackground(Color.white);
		vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line<MetaVariable,ConstraintNetwork>());
		vv.getRenderContext().setVertexLabelTransformer(new Transformer<MetaVariable, String>() {
			@Override
			public String transform(MetaVariable arg0) {
				return arg0.toString();
			}
		});

		vv.setVertexToolTipTransformer(new Transformer<MetaVariable, String>() {
			@Override
			public String transform(MetaVariable arg0) {
				return arg0.toString();
			}
		});

		//vv.getRenderContext().setArrowFillPaintTransformer(new ConstantTransformer(Color.lightGray));
		vv.getRenderContext().setArrowFillPaintTransformer(new Transformer<ConstraintNetwork, Paint>() {
			@Override
			public Paint transform(ConstraintNetwork arg0) {
				return Color.lightGray;
			}
		});

		//System.out.println(graph.getVertices());
		rings = new Rings();

		Container content = getContentPane();
		final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
		content.add(panel);

		final DefaultModalGraphMouse<?, ?> graphMouse = new DefaultModalGraphMouse<Object, Object>();

		vv.setGraphMouse(graphMouse);

		JComboBox modeBox = graphMouse.getModeComboBox();
		modeBox.addItemListener(graphMouse.getModeListener());
		graphMouse.setMode(Mode.TRANSFORMING);

		final ScalingControl scaler = new CrossoverScalingControl();

		JButton plus = new JButton("+");
		plus.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				scaler.scale(vv, 1.1f, vv.getCenter());
			}
		});
		JButton minus = new JButton("-");
		minus.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				scaler.scale(vv, 1/1.1f, vv.getCenter());
			}
		});

		JToggleButton radial = new JToggleButton("Radial");
		radial.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {

					LayoutTransition<MetaVariable,ConstraintNetwork> lt =
							new LayoutTransition<MetaVariable,ConstraintNetwork>(vv, treeLayout, radialLayout);
					Animator animator = new Animator(lt);
					animator.start();
					vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
					vv.addPreRenderPaintable(rings);
				} else {
					LayoutTransition<MetaVariable,ConstraintNetwork> lt =
							new LayoutTransition<MetaVariable,ConstraintNetwork>(vv, radialLayout, treeLayout);
					Animator animator = new Animator(lt);
					animator.start();
					vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
					vv.removePreRenderPaintable(rings);
				}
				vv.repaint();
			}});

		JPanel scaleGrid = new JPanel(new GridLayout(1,0));
		scaleGrid.setBorder(BorderFactory.createTitledBorder("Zoom"));

		JPanel controls = new JPanel();
		scaleGrid.add(plus);
		scaleGrid.add(minus);
		controls.add(radial);
		controls.add(scaleGrid);
		controls.add(modeBox);

		content.add(controls, BorderLayout.SOUTH);
	}

	class Rings implements VisualizationServer.Paintable {

		Collection<Double> depths;

		public Rings() {
			MetaVariable[] mvars = graph.getVertices().toArray(new MetaVariable[graph.getVertexCount()]);
//			for (int i = 0; i < mvars.length; i++) {
//				System.out.println(mvars[i]);
//			}
			depths = getDepths();
		}

		private Collection<Double> getDepths() {
			Set<Double> depths = new HashSet<Double>();
			Map<MetaVariable,PolarPoint> polarLocations = radialLayout.getPolarLocations();
			for(MetaVariable v : graph.getVertices()) {
				PolarPoint pp = polarLocations.get(v);
				depths.add(pp.getRadius());
			}
			return depths;
		}

		@Override
		public void paint(Graphics g) {
			g.setColor(Color.lightGray);

			Graphics2D g2d = (Graphics2D)g;
			Point2D center = radialLayout.getCenter();

			Ellipse2D ellipse = new Ellipse2D.Double();
			for(double d : depths) {
				ellipse.setFrameFromDiagonal(center.getX()-d, center.getY()-d, 
						center.getX()+d, center.getY()+d);
				Shape shape = vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT).transform(ellipse);
				g2d.draw(shape);
			}
		}

		@Override
		public boolean useTransform() {
			return true;
		}
	}

	public static void draw(DelegateForest<MetaVariable,ConstraintNetwork> graph) {
		SearchTreeFrame stf = new SearchTreeFrame(graph);
		stf.setTitle(SearchTreeFrame.class.getSimpleName());
		stf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		stf.pack();
		stf.setVisible(true);
	}

}

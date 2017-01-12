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
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JRootPane;

import org.apache.commons.collections15.Transformer;
import org.metacsp.framework.Constraint;
import org.metacsp.framework.Variable;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;

/**
 * Visualization of trees.
 *
 * @author Federico Pecora
 */
public class VariableHierarchyFrame extends JFrame {

	private static final Logger logger = Logger.getLogger(VariableHierarchyFrame.class.getPackage().getName());
	private static final long serialVersionUID = 3896624751126451811L;
	private DelegateTree<Variable,String> g = null;
	private VisualizationViewer<Variable,String> vv = null;
	private TreeLayout<Variable, String> layout = null;

	public VariableHierarchyFrame(DelegateTree<Variable,String> tree, String title) {
		super(title);
		this.g = tree;
		
		layout = new TreeLayout<Variable,String>(g);

		Layout<Variable,String> staticLayout = new StaticLayout<Variable,String>(g, layout);
		vv = new VisualizationViewer<Variable,String>(staticLayout, new Dimension(600,600));
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
		Transformer<String,String> stringer = new Transformer<String,String>(){
			@Override
			public String transform(String e) {
				try { return e.substring(0,e.indexOf("(")); }
				catch (NullPointerException ex) { return ""; }
			}
		};

		Transformer<Variable,String> vertexPaint = new Transformer<Variable,String>() {
			public String transform(Variable e) {
				try { return e.getClass().getSimpleName(); }
				catch (NullPointerException ex) { return ""; }
			}
		};
		

		vv.getRenderContext().setVertexLabelTransformer(vertexPaint);
		vv.getRenderContext().setEdgeLabelTransformer(stringer);
		

		getContentPane().add(vv);
		
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.pack();
		this.setVisible(true);
	}
}

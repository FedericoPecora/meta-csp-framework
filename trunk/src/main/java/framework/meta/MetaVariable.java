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
package framework.meta;

import java.util.Arrays;
import java.util.logging.Logger;

import utility.logging.MetaCSPLogging;
import framework.Constraint;
import framework.ConstraintNetwork;
import framework.Variable;

/**
 * {@link MetaVariable}s are objects used by {@link MetaConstraintSolver}s for backtracking search.
 * They contain {@link ConstraintNetwork}s representing meta variables for the meta-CSP search.
 * A {@link MetaVariable} also has a reference to the {@link MetaConstraint} which was used
 * to obtain it.
 * 
 * {@link MetaVariable}s are used internally by the {@link MetaConstraintSolver} and 
 * should not be instantiated explicitly (hence the constructors are not accessible
 * outside this package).
 * 
 * @author Federico Pecora
 *
 */
public class MetaVariable {
	private MetaConstraint metaConstraint;
	private ConstraintNetwork mv;
	private String annotation = null;
	
	private transient Logger logger = MetaCSPLogging.getLogger(this.getClass());
	
	//MetaVariables should not be created w/o arguments
	//private MetaVariable() {}
	protected MetaVariable() {}
	
	/**
	 * Create a new {@link MetaVariable} with a given {@link MetaConstraint}.  The variable is represented as
	 * a {@link ConstraintNetwork}.
	 * @param df The {@link MetaConstraint} that created this {@link MetaVariable}.
	 * @param mv The actual {@link MetaVariable}.
	 */
	public MetaVariable(MetaConstraint df, ConstraintNetwork mv) {
		this.metaConstraint = df;
		this.mv = mv;
		this.annotation = null;
		logger.finest("Created MetaVariable " + this);
	}

	/**
	 * Create a new {@link MetaVariable} with a given {@link MetaConstraint}.  The variable is represented as
	 * a {@link ConstraintNetwork}.
	 * @param metaConstraint The {@link MetaConstraint} that created this {@link MetaVariable}.
	 * @param mv The actual {@link MetaVariable}.
	 * @param annotation An annotation describing the {@link MetaVariable}.
	 */
	public MetaVariable(MetaConstraint metaConstraint, ConstraintNetwork mv, String annotation) {
		this.metaConstraint = metaConstraint;
		this.mv = mv;
		this.annotation = annotation;
		logger.finest("Created MetaVariable " + this);
	}
	
	/**
	 * Get the {@link MetaConstraint} that is responsible for defining this
	 * {@link MetaVariable}. 
	 * @return The {@link MetaConstraint} that is responsible for defining this
	 * {@link MetaVariable}.
	 */
	public MetaConstraint getMetaConstraint() {return metaConstraint;}
	
	/**
	 * Get the network of {@link Variable}s and {@link Constraint}s that
	 * define this {@link MetaVariable}.
	 * @return The network of {@link Variable}s and {@link Constraint}s that
	 * define this {@link MetaVariable}.
	 */
	public ConstraintNetwork getConstraintNetwork() {return mv;}
	
	/**
	 * Get a {@link String} representation of this {@link MetaVariable}.
	 */
	public String toString() {
		String ret = "[" + this.metaConstraint + "] " + (mv.getVariables().length != 0 ? "Vars = " + Arrays.toString(mv.getVariables()) : "") + (mv.getConstraints().length != 0 ? " Cons = " + Arrays.toString(mv.getConstraints()) : "") + (annotation != null ? (" (" + annotation + ")") : "" );
		return ret;
	}

}

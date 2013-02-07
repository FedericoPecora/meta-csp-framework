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
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import meta.TCSP.TCSPSolver;
import meta.symbolsAndTime.Scheduler;
import multi.activity.ActivityNetworkSolver;
import multi.allenInterval.AllenIntervalNetworkSolver;
import time.APSPSolver;
import utility.UI.SearchTreeFrame;
import utility.logging.MetaCSPLogging;
import edu.uci.ics.jung.graph.DelegateForest;
import framework.Constraint;
import framework.ConstraintNetwork;
import framework.ConstraintSolver;
import framework.Variable;
import framework.multi.MultiConstraintSolver;

/**
 * A meta-CSP is a high-level CSP whose variables and/or constraints are defined implicitly.  These
 * variables and constraints are called meta-variables and meta-constraints.  They typically represent
 * the elements of a higher-level problem defined over a so.called ground-CSP.  Many known problems
 * can be cast as meta-CSPs.  For instance, a resource scheduling problem is a meta-CSP whose
 * meta-variables are sets of possibly concurrent activities that over-consume a resource.  These
 * activities are themselves variables in a ground-CSP which decides their placement in time
 * according to temporal constraints.  The activities and temporal constraints are, repsectively,
 * the variables and constraints of the ground-CSP, while the sets of possibly overlapping
 * and over-consuming activities are meta-variables in the meta-CSP.  The constraints of the meta-CSP
 * (meta-constraints) are the resources themselves.
 * 
 * Solving a meta-CSP consists in finding values for the meta-variables such that no meta-constraint
 * is violated.  The values of meta-variables are {@link ConstraintNetwork}s - i.e., variables and
 * constraints which should be posted to the ground-CSP(s) in order to satisfy the meta-constraints.
 * In resource scheduling for instance, a value of a meta-variable is a temporal constraint that
 * eliminates the temporal overlap of the concurrent over-consuming activities.
 *    
 * This class provides the fundamental mechanisms to define and solve a meta-CSP.  It implements
 * a backtracking search over the search space defined by meta-variables, meta-values and
 * meta-constraints.  Examples of concrete {@link MetaConstraintSolver}s that implement this
 * class are given in this framework (e.g., the {@link TCSPSolver}, which implements
 * a Temporal Constraint Satisfaction Problem solver as a meta-CSP, and the {@link Scheduler}, which
 * implements the ESTA algorithm for solving resource scheduling problems).  
 * 
 * This class also provides limited support for constraint optimization through branch-and-bound.
 * 
 * @author Federico Pecora
 *
 */
public abstract class MetaConstraintSolver extends MultiConstraintSolver {

	public int resolvedConflictCounter = 0;
	public int triedValuesCounter = 0;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7343190680692608215L;
	protected Vector<MetaConstraint> metaConstraints = null;
	protected DelegateForest<MetaVariable,ConstraintNetwork> g;
	private MetaVariable currentVertex = null;
	private boolean breakSearch = false;
	private HashMap<ConstraintNetwork,ConstraintNetwork> resolvers;
	private long animationTime = 0;
	private int counterMoves;

	private transient Logger logger = MetaCSPLogging.getLogger(this.getClass());
	
	/**
	 * Get the final list of resolvers (meta-values) added to the ground-CSP(s)
	 * for obtaining a solution to the meta-CSP.
	 * @return List of resolvers that were added to obtain the solution of the
	 * meta-CSP.
	 */
	public ConstraintNetwork[] getAddedResolvers() {
		Collection<ConstraintNetwork> ret = resolvers.values();
		return ret.toArray(new ConstraintNetwork[ret.size()]);
	}
	
	/**
	 * Retract all resolvers added to the ground-CSP(s) in order to obtain the
	 * current solution to the meta-CSP.  This is useful if one wants to "reset"
	 * the meta-CSP to its original unsolved state.
	 */
	public void retractResolvers() {
		Set<ConstraintNetwork> vars = resolvers.keySet();
		for (ConstraintNetwork var : vars) {
			ConstraintNetwork value = resolvers.get(var);
			logger.fine("Retracting value: " + Arrays.toString(value.getConstraints()));
			this.retractResolver(var, value);
		}
		this.resolvers = new HashMap<ConstraintNetwork, ConstraintNetwork>();
	}
	
	private class TerminalNode extends MetaVariable {
		private boolean success;
		public TerminalNode(boolean success) {
			super(null, null);
			this.success = success;
		}
		public String toString() {
			if (success) return "SUCCESS";
			return "FAILURE";
		}
	}
	
	protected MetaConstraintSolver(Class<?>[] constraintTypes, long animationTime, ConstraintSolver ... internalSolvers) {
		super(constraintTypes, new Class[]{MetaVariable.class}, internalSolvers);
		g = new DelegateForest<MetaVariable,ConstraintNetwork>();
		this.animationTime = animationTime;
		this.resolvers = new HashMap<ConstraintNetwork,ConstraintNetwork>();
		this.counterMoves=0;
	}
		
	/**
	 * Method to add a {@link MetaConstraint} to this meta-CSP solver.
	 * @param metaConstraint  The {@link MetaConstraint} to add.
	 */
	public void addMetaConstraint(MetaConstraint metaConstraint) {
		if (this.metaConstraints == null) this.metaConstraints = new Vector<MetaConstraint>();
		// here the metaConstraint is added
		this.metaConstraints.add(metaConstraint);
		// the meta constraint now refers to the MetaConstraintSolver
		metaConstraint.setMetaSolver(this);
		boolean found = false;
		// Here I simply check if the type of the metaConstraint is already taken into account, 
		// otherwise the array of types is extended
		for (Class<?> cl : this.constraintTypes) if (cl.equals(metaConstraint.getClass())) found = true;
		if (!found) {
			Class<?>[] newConstraintTypes = new Class<?>[this.constraintTypes.length+1];
			for (int i = 0; i < this.constraintTypes.length; i++) newConstraintTypes[i] = this.constraintTypes[i];
			newConstraintTypes[this.constraintTypes.length] = metaConstraint.getClass();
			this.constraintTypes = newConstraintTypes;
		}
	}
	
	protected MetaVariable getConflict() {
		
		if(this.metaConstraints==null) return null;
		for (MetaConstraint df : this.metaConstraints) {
			ConstraintNetwork cn = df.getMetaVariable();
			if (cn != null) 
				return new MetaVariable(df, cn);
		}
		return null;
	}
	
	/**
	 * Implement this method to define any extra operations that should be performed
	 * before backtracking.
	 */
	public abstract void preBacktrack();

	/**
	 * Implement this method to define any extra operations that should be performed
	 * after backtracking.
	 */
	public abstract void postBacktrack(MetaVariable metaVariable);
	
	/**
	 * Initiates CSP-style backtracking search on the meta-CSP.  
	 * @return <code>true</code> iff a set of assignments to all {@link MetaVariable}s which
	 * satisfies the {@link MetaConstraint}s was found.
	 */
	public boolean backtrack() {
		resolvedConflictCounter = 0;
		triedValuesCounter = 0;
//		logger.setLevel(Level.FINEST);
		g = new DelegateForest<MetaVariable,ConstraintNetwork>();
//		logger.info("Starting search...");
//		preBacktrack();
		MetaVariable conflict = null;
		if ((conflict = this.getConflict()) != null) {
			currentVertex = conflict;
			if (backtrackHelper(conflict)) {
//				postBacktrack();
//				logger.info("... solution found");
				return true;
			}
//			postBacktrack();
			return false;
		}
//		postBacktrack();
//		logger.info("... no conflicts found");		
		return true;
	}
	
	/**
	 * Initiates CSP-style backtracking search on the meta-CSP with intial time.  
	 * @return <code>true</code> iff a set of assignments to all {@link MetaVariable}s which
	 * satisfies the {@link MetaConstraint}s was found. The initial_time parameter constraints all the moves
	 * of the constraint solvers to act after such instant
	 */
	public boolean backtrack(int initial_time) {
		g = new DelegateForest<MetaVariable,ConstraintNetwork>();
		logger.info("Starting search...");
//		preBacktrack();
		MetaVariable conflict = null;
		if ((conflict = this.getConflict()) != null) {
			currentVertex = conflict;
			if (backtrackHelper(conflict, initial_time)) {
//				postBacktrack();
				logger.info("... solution found");
				return true;
			}
//			postBacktrack();
			return false;
		}
//		postBacktrack();
		logger.info("... no conflicts found");		
		return true;
	}
	
	private boolean backtrackHelper(MetaVariable metaVariable) {
		preBacktrack();
		if (this.g.getRoot() == null) this.g.addVertex(currentVertex);
		ConstraintNetwork mostProblematicNetwork = metaVariable.getConstraintNetwork();
		logger.fine("Solving conflict: " + metaVariable);
		ConstraintNetwork[] values = metaVariable.getMetaConstraint().getMetaValues(metaVariable);	
		if (metaVariable.getMetaConstraint().valOH != null && values!=null) Arrays.sort(values, metaVariable.getMetaConstraint().valOH);
		if (values == null || values.length == 0) {
			this.g.addEdge(new NullConstraintNetwork(null), currentVertex, new TerminalNode(false));
			logger.fine("Failure (1)...");		
		}
		else {
			logger.fine("num values: " + values.length);
			for (ConstraintNetwork value : values) {
				triedValuesCounter++;
				if (animationTime != 0) {
					try { Thread.sleep(animationTime); }
					catch (InterruptedException e) { e.printStackTrace(); }
				}
				logger.fine("Trying value: " + Arrays.toString(value.getConstraints()));		
								
				if (this.addResolver(mostProblematicNetwork, value)) {
					
					this.resolvers.put(mostProblematicNetwork, value);
					this.counterMoves++;
					logger.finest("I am incrementing the metaconstraintsolver counterMoves!!!: "+ this.counterMoves);

					logger.fine("Success...");		
					resolvedConflictCounter++;
					metaVariable.getMetaConstraint().markResolvedSub(metaVariable, value);
					MetaVariable newConflict = this.getConflict();
					
					if (newConflict == null || breakSearch) {
						this.g.addEdge(value, currentVertex, new TerminalNode(true));
						breakSearch = false;
						return true;
					}
					this.g.addEdge(value, currentVertex, newConflict);
					currentVertex = newConflict;
					if (backtrackHelper(newConflict)) return true;					
					logger.fine("Retracting value: " + Arrays.toString(value.getConstraints()));		
					this.retractResolver(mostProblematicNetwork, value);
					this.resolvers.remove(mostProblematicNetwork);			
					this.counterMoves--;
					logger.finest("I am decrementing the metaconstraintsolver counterMoves!!!: "+ this.counterMoves);

				}
				else {
					this.g.addEdge(value, currentVertex, new TerminalNode(false));
					logger.fine("Failure... (2)");
				}
			}
		}
		logger.fine("Backtracking...");
		currentVertex = this.g.getParent(currentVertex);
		postBacktrack(metaVariable);
		return false;
	}
	
	
	private boolean backtrackHelper(MetaVariable metaVariable, int initial_time) {
		preBacktrack();
		if (this.g.getRoot() == null) this.g.addVertex(currentVertex);
		logger.finest("WWWWWWWWWWWWWWWWWW  METACS G LEN "+ this.getVariables().length);
		ConstraintNetwork mostProblematicNetwork = metaVariable.getConstraintNetwork();
		logger.fine("Solving conflict: " + metaVariable);
		ConstraintNetwork[] values = metaVariable.getMetaConstraint().getMetaValues(metaVariable, initial_time);	
		if (metaVariable.getMetaConstraint().valOH != null && values!=null){
			Arrays.sort(values, metaVariable.getMetaConstraint().valOH);
		}
		if (values == null || values.length == 0) {
			this.g.addEdge(new NullConstraintNetwork(null), currentVertex, new TerminalNode(false));
			logger.fine("Failure (1)...");		
		}
		else {
			logger.finest("FOUND " + values.length+" MOVES");
			for (ConstraintNetwork value : values) {
				if (animationTime != 0) {
					try { Thread.sleep(animationTime); }
					catch (InterruptedException e) { e.printStackTrace(); }
				}
				logger.fine("Trying value: " + Arrays.toString(value.getConstraints()));		
				
				if (this.addResolver(mostProblematicNetwork, value)) {
					this.resolvers.put(mostProblematicNetwork, value);
					this.counterMoves++;
					logger.finest("I am incrementing the metaconstraintsolver counterMoves!!!: "+ this.counterMoves);
					

					logger.fine("Success...");		
					
					metaVariable.getMetaConstraint().markResolvedSub(metaVariable, value);
					MetaVariable newConflict = this.getConflict();
					if (newConflict == null || breakSearch) {
						this.g.addEdge(value, currentVertex, new TerminalNode(true));
						breakSearch = false;
						return true;
					}
					// addEdege(e,v,v)
					this.g.addEdge(value, currentVertex, newConflict);
					currentVertex = newConflict;
					if (backtrackHelper(newConflict, initial_time)) return true;					
					logger.fine("Retracting value: " + Arrays.toString(value.getConstraints()));		
					this.retractResolver(mostProblematicNetwork, value);
					this.resolvers.remove(mostProblematicNetwork);	
					this.counterMoves--;
					logger.finest("I am decrementing the metaconstraintsolver counterMoves!!!"+ this.counterMoves);

				}
				else {
					this.g.addEdge(value, currentVertex, new TerminalNode(false));
					logger.fine("Failure... (2)");
				}
			}
		}
		
		logger.fine("Backtracking...");
		currentVertex = this.g.getParent(currentVertex);
		postBacktrack(metaVariable);
		return false;
	}

	

	private final boolean addResolver(ConstraintNetwork metaVarConstraintNetwork, ConstraintNetwork resolverNetwork) {
		this.addResolverSub(metaVarConstraintNetwork, resolverNetwork);	
		Constraint[] resolverNetworkConstraints = resolverNetwork.getConstraints();
		HashMap<ConstraintSolver, Vector<Constraint>> solvers2constraints = 
				new HashMap<ConstraintSolver, Vector<Constraint>>();		
		for (Constraint c : resolverNetworkConstraints) {
			if (!solvers2constraints.containsKey(c.getScope()[0].getConstraintSolver())) {
				Vector<Constraint> newVec = new Vector<Constraint>();
				solvers2constraints.put(c.getScope()[0].getConstraintSolver(), newVec);				
			}
			solvers2constraints.get(c.getScope()[0].getConstraintSolver()).add(c);
		}
		Vector<Constraint[]> addedConstraints = new Vector<Constraint[]>();
		for (ConstraintSolver cs : solvers2constraints.keySet()) {
			Constraint[] toAddOneSolver = 
					solvers2constraints.get(cs).toArray(new Constraint[solvers2constraints.get(cs).size()]);
			if (cs.addConstraints(toAddOneSolver)) addedConstraints.add(toAddOneSolver);
			else {
				for (Constraint[] toDel : addedConstraints) {
					toDel[0].getScope()[0].getConstraintSolver().removeConstraints(toDel);
				}
				this.retractResolverSub(metaVarConstraintNetwork, resolverNetwork);
				return false;
			}
		}		
		return true;
	}

	private final void retractResolver(ConstraintNetwork metaVar, ConstraintNetwork res) {
		Constraint[] groundConstraints = res.getConstraints();
		HashMap<ConstraintSolver, Vector<Constraint>> solvers2constraints = new HashMap<ConstraintSolver, Vector<Constraint>>();
		for (Constraint c : groundConstraints) {
			if (!solvers2constraints.containsKey(c.getScope()[0].getConstraintSolver())) {
				Vector<Constraint> newVec = new Vector<Constraint>();
				solvers2constraints.put(c.getScope()[0].getConstraintSolver(), newVec);
			}
			solvers2constraints.get(c.getScope()[0].getConstraintSolver()).add(c);
		}			
		for (ConstraintSolver cs : solvers2constraints.keySet()) {
			Constraint[] toAddOneSolver = solvers2constraints.get(cs).toArray(new Constraint[solvers2constraints.get(cs).size()]);
			cs.removeConstraints(toAddOneSolver);
		}
		this.retractResolverSub(metaVar, res);
	}

	@Override
	protected ConstraintNetwork createConstraintNetwork() {
		return new MetaVariableConstraintNetwork(this);
	}
	
	@Override
	public boolean propagate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected Variable[] createVariablesSub(int num) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void removeVariableSub(Variable v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void removeVariablesSub(Variable[] v) {
		// TODO Auto-generated method stub		
	}
	
	/**
	 * Implement this method to define any extra operations that should happen
	 * after retracting a meta-value in the meta-CSP search (e.g., when backtracking).
	 * @param metaVariable The {@link MetaVariable} over which backtracking occurs.
	 * @param metaValue The meta-value that has been retracted.
	 */
	protected abstract void retractResolverSub(ConstraintNetwork metaVariable, ConstraintNetwork metaValue);

	/**
	 * Implement this method to define any additional operations that should happen before
	 * adding a meta-value in the meta-CSP search (e.g., when branching).
	 * @param metaVariable The {@link MetaVariable} over which the search is branching.
	 * @param metaValue The meta-value that has been selected (the branch). 
	 */
	protected abstract void addResolverSub(ConstraintNetwork metaVariable, ConstraintNetwork metaValue);
	
	/**
	 * Get all the {@link Variable}s of all ground solvers used by this {@link MetaConstraintSolver}. 
	 * @return The list of variables for each ground solver.
	 */
	public HashMap<ConstraintSolver,Variable[]> getGroundVariables() {
		HashMap<ConstraintSolver,Variable[]> ret = new HashMap<ConstraintSolver,Variable[]>();
		for (ConstraintSolver cs : this.constraintSolvers) {
			ret.put(cs, cs.getVariables());
		}
		return ret;
	}
	
	/**
	 * Draws the search space of the meta-CSP.
	 */
	public void draw() {
		SearchTreeFrame.draw(this.g);
	}

	/**
	 * Get all the ground constraint solvers of this meta-CSP. 
	 * @return All ground constraint solvers of this meta-CSP.
	 */
	public ConstraintSolver[] getConstraintSolvers() {
		return this.constraintSolvers;
	}
	
	/**
	 * Interrupt the current meta-CSP search. 
	 */
	public void breakSearch() { breakSearch = true; }
	
	/**
	 * Perform branch-and-bound search for an optimal solution to this meta-CSP. The cost
	 * of assignments is defined in the implementing class through the upper/lower-bound getter and
	 * setter methods.
	 * @return <code>true</code> iff IRAN: complete this please!
	 */
	public boolean branchAndBound() {
		g = new DelegateForest<MetaVariable,ConstraintNetwork>();
		//graph = new ObservableGraph<MetaVariable,ConstraintNetwork>(g);
		logger.info("Starting search...");
//		preBacktrack();
		MetaVariable con = null;
		if ((con = this.getConflict()) != null) {
			currentVertex = con;
			if (branchAndBoundHelper(con)) {
//				postBacktrack();
				logger.info("... solution found");
				return true;
			}
//			postBacktrack();
			return false;
		}
//		postBacktrack();
		logger.info("... no conflicts found");
		return true;
	}
	
	private boolean branchAndBoundHelper(MetaVariable metaVariable) {
		
		if(metaVariable == null)
			return false;

		preBacktrack();
			
		if (this.g.getRoot() == null) this.g.addVertex(currentVertex);
		ConstraintNetwork cn = metaVariable.getConstraintNetwork();
		
		logger.fine("Solving conflict: " + metaVariable);
		ConstraintNetwork[] values = metaVariable.getMetaConstraint().getMetaValues(metaVariable);
		
		if (metaVariable.getMetaConstraint().valOH != null) Arrays.sort(values, metaVariable.getMetaConstraint().valOH);
		
		if (values == null || values.length == 0) {
			this.g.addEdge(new NullConstraintNetwork(null), currentVertex, new TerminalNode(false));
			logger.fine("Failure... (1)");
		}
		else {
			for (ConstraintNetwork value : values) {
				if (animationTime != 0) {
					try { Thread.sleep(animationTime); }
					catch (InterruptedException e) { e.printStackTrace(); }
				}
				logger.fine("Trying value: " + Arrays.toString(value.getConstraints()));

				if(hasConflictClause(value))
					continue;

				this.addResolver(cn, value);
				setUpperBound();
//				System.out.println("test: " + "U: " + getUpperBound() + " L: " + getLowerBound());
				if(getUpperBound() <= getLowerBound()){						
					this.retractResolver(cn, value);
					continue;
				}
				
				logger.fine("Success...");

				metaVariable.getMetaConstraint().markResolvedSub(metaVariable, value);
				MetaVariable newCon = this.getConflict();
				if(newCon != null ){
					this.g.addEdge(value, currentVertex, newCon);
					currentVertex = newCon;
				}
				if(newCon == null)
					setLowerBound();
				if(branchAndBoundHelper(newCon))
					return true;
				logger.fine("Retracting value: " + Arrays.toString(value.getConstraints()));
				this.retractResolver(cn, value);
				logger.fine("Failure... (2)");
			}
		}
		resetFalseClause();
		logger.fine("Backtracking...");
		currentVertex = this.g.getParent(currentVertex);
		postBacktrack(metaVariable);
		return false;
	}
	
	protected abstract double getUpperBound();
	
	protected abstract void setUpperBound();
	
	protected abstract double getLowerBound();
	
	protected abstract void setLowerBound();
	
	protected abstract boolean hasConflictClause(ConstraintNetwork metaValue);
	
	protected abstract void resetFalseClause();
	
	@Override
	public String getDescription() {
		String spacer = "";
		for (int i = 0; i < nesting; i++) spacer += spacing;
		String ret = spacer + "[" + this.getClass().getSimpleName() + " vars: [";
		for (int i = 0; i < this.variableTypes.length; i++) {
			ret += this.variableTypes[i].getSimpleName();
			if (i != this.variableTypes.length-1) ret += ",";
		}
		ret += "] constraints: [";
		for (int i = 0; i < this.constraintTypes.length; i++) {
			ret += this.constraintTypes[i].getSimpleName();
			if (i != this.constraintTypes.length-1) ret += ",";
		}
		ret += "]";
		nesting++;
		for (ConstraintSolver cs : this.getConstraintSolvers()) ret += "\n" + cs.getDescription();
		nesting--;
		ret += "]";
		return ret;
	}

	public void failurePruning(int failure_time){
		super.failurePruning(failure_time);
		this.counterMoves=0;
		this.g=new DelegateForest<MetaVariable,ConstraintNetwork>();
		this.resolvers.clear();
		
	}

	public int getCounterMoves() {
		return counterMoves;
	}

	public void setCounterMoves(int counterMoves) {
		this.counterMoves = counterMoves;
	}

	public HashMap<ConstraintNetwork, ConstraintNetwork> getResolvers() {
		return resolvers;
	}

	public void setResolvers(HashMap<ConstraintNetwork, ConstraintNetwork> resolvers) {
		this.resolvers = resolvers;
	}

}

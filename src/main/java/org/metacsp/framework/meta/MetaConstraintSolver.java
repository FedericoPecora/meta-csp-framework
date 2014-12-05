package org.metacsp.framework.meta;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import org.metacsp.framework.Constraint;
import org.metacsp.framework.ConstraintNetwork;
import org.metacsp.framework.ConstraintSolver;
import org.metacsp.framework.Variable;
import org.metacsp.framework.multi.MultiConstraintSolver;
import org.metacsp.meta.TCSP.TCSPSolver;
import org.metacsp.meta.symbolsAndTime.Scheduler;
import org.metacsp.throwables.NoFocusDefinedException;
import org.metacsp.utility.UI.SearchTreeFrame;
import org.metacsp.utility.logging.MetaCSPLogging;

import edu.uci.ics.jung.graph.DelegateForest;

/**
 * A meta-CSP is a high-level CSP whose variables and/or constraints are defined implicitly.  These
 * variables and constraints are called meta-variables and meta-constraints.  They typically represent
 * the elements of a higher-level problem defined over a so.called ground-CSP.  Many known problems
 * can be cast as meta-CSPs.  For instance, a resource scheduling problem is a meta-CSP whose
 * meta-variables are sets of possibly concurrent activities that over-consume a resource.  These
 * activities are themselves variables in a ground-CSP which decides their placement in time
 * according to temporal constraints.  The activities and temporal constraints are, respectively,
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

	/**
	 * 
	 */
	private static final long serialVersionUID = -7343190680692608215L;
	protected Vector<MetaConstraint> metaConstraints = null;
	protected DelegateForest<MetaVariable,ConstraintNetwork> g;
	protected MetaVariable currentVertex = null;
	protected boolean breakSearch = false;
	protected HashMap<ConstraintNetwork,MetaConstraint> metaVarsToMetaCons;
	protected HashMap<ConstraintNetwork,ConstraintNetwork> resolvers;
	protected HashMap<ConstraintNetwork,ConstraintNetwork> resolversInverseMapping;
	protected long animationTime = 0;
	protected int counterMoves;
	protected FocusConstraint currentFocus = null;
	
	private Vector<HashMap<ConstraintSolver,byte[]>> backedUpCNs = new Vector<HashMap<ConstraintSolver,byte[]>>();
	
	//private Vector<HashMap<ConstraintSolver,ConstraintNetwork>> statesAlongCurrentBranch = new Vector<HashMap<ConstraintSolver,ConstraintNetwork>>(); 

	private transient Logger logger = MetaCSPLogging.getLogger(this.getClass());
		
	public MetaConstraint[] getMetaConstraints() {
		return this.metaConstraints.toArray(new MetaConstraint[this.metaConstraints.size()]);
	}
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
			logger.fine("=== ||| === Retracting value: " + Arrays.toString(value.getConstraints()));
			this.retractResolver(var, value);
		}
		this.resolvers = new HashMap<ConstraintNetwork, ConstraintNetwork>();
		this.resolversInverseMapping = new HashMap<ConstraintNetwork, ConstraintNetwork>();
		this.metaVarsToMetaCons.clear();
	}

	/**
	 * Clear memory of all resolvers added to the ground-CSP(s) in order to obtain the
	 * current solution to the meta-CSP.  This is useful if one wants to restart solving
	 * from the current solved state.  Note that resolvers are not retracted - to "reset"
	 * the meta-CSP to its original unsolved state, use method {@link MetaConstraintSolver#retractResolvers()}.
	 */
	public void clearResolvers() {
		this.resolvers = new HashMap<ConstraintNetwork, ConstraintNetwork>();
		this.metaVarsToMetaCons = new HashMap<ConstraintNetwork, MetaConstraint>();
	}

	protected class TerminalNode extends MetaVariable {
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
		super(constraintTypes, MetaVariable.class, internalSolvers, null);
		g = new DelegateForest<MetaVariable,ConstraintNetwork>();
		this.animationTime = animationTime;
		this.resolvers = new HashMap<ConstraintNetwork,ConstraintNetwork>();
		this.metaVarsToMetaCons= new HashMap<ConstraintNetwork, MetaConstraint>(); 
		this.resolversInverseMapping = new HashMap<ConstraintNetwork,ConstraintNetwork>();
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
		g = new DelegateForest<MetaVariable,ConstraintNetwork>();
		logger.info("Starting search...");
//		preBacktrack();
		MetaVariable conflict = null;
		if ((conflict = this.getConflict()) != null) {
			currentVertex = conflict;
			if (backtrackHelper(conflict)) {
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
	
	//FPA: Is this used? Seems not... please remove! (Iran: it is used for hybrid planner benchmarking)
	private boolean timeout = false;
	public boolean getTimeOut(){
		return timeout;
	}
	
	private boolean backtrackHelper(MetaVariable metaVariable) {
		preBacktrack();
		if (this.g.getRoot() == null) this.g.addVertex(currentVertex);
		ConstraintNetwork mostProblematicNetwork = metaVariable.getConstraintNetwork();
		logger.fine("Solving conflict: " + metaVariable);
		ConstraintNetwork[] values = metaVariable.getMetaConstraint().getMetaValues(metaVariable);	
		if (metaVariable.getMetaConstraint().valOH != null && values!=null) {
			//System.out.println("SORTING with " + metaVariable.getMetaConstraint().valOH.getClass());
			Arrays.sort(values, metaVariable.getMetaConstraint().valOH);
		}
		if (values == null || values.length == 0) {
			this.g.addEdge(new NullConstraintNetwork(null), currentVertex, new TerminalNode(false));
			logger.fine("Failure (1)...");		
		}
		else {
			for (ConstraintNetwork value : values) {
				if (animationTime != 0) {
					try { Thread.sleep(animationTime); }
					catch (InterruptedException e) { e.printStackTrace(); }
				}
				String valString = "";
				if (value.getVariables().length != 0) valString += "Vars = " + Arrays.toString(value.getVariables());
				if (value.getConstraints().length != 0) valString += " Cons = " + Arrays.toString(value.getConstraints());
				logger.fine("Trying value: " + valString);
				
				if (this.addResolver(mostProblematicNetwork, value)) {
					this.resolvers.put(mostProblematicNetwork, value);
					this.metaVarsToMetaCons.put(mostProblematicNetwork, metaVariable.getMetaConstraint());
					this.resolversInverseMapping.put(value,mostProblematicNetwork);
					this.counterMoves++;

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
					if (backtrackHelper(newConflict)) return true;					
					logger.fine("Retracting value: " + Arrays.toString(value.getConstraints()));		
					this.retractResolver(mostProblematicNetwork, value);
					this.resolvers.remove(mostProblematicNetwork);		
					this.metaVarsToMetaCons.remove(mostProblematicNetwork);
					this.resolversInverseMapping.remove(value);
					this.counterMoves--;

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

	/**
	 * Service method for backtracking with serialization-based saving of {@link ConstraintNetwork}s.
	 * This method backs up {@link ConstraintNetwork}s before branching. 
	 */
	private HashMap<ConstraintSolver,byte[]> backupCNs(MultiConstraintSolver conSol) {
		//Here we want to save the CNs
		ByteArrayOutputStream bos = null;
		ObjectOutputStream oos = null;
		HashMap<ConstraintSolver,byte[]> currentLevel = new HashMap<ConstraintSolver,byte[]>();
		try {
			bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			for (ConstraintSolver cs : conSol.getConstraintSolvers()) {
				logger.finest("Backing up CN of " + cs.getClass().getSimpleName());
				ConstraintNetwork cn = cs.getConstraintNetwork();
				oos.writeObject(cn);
				byte[] backup = bos.toByteArray();
		        currentLevel.put(cs,backup);
		        if (cs instanceof MultiConstraintSolver) {
		        	//System.out.println("RECURSIVE on " + cs.getClass().getSimpleName());
		        	HashMap<ConstraintSolver,byte[]> lower = backupCNs((MultiConstraintSolver)cs);
		        	currentLevel.putAll(lower);
		        }
			}
	        return currentLevel;
		}
		catch (NotSerializableException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
		return null;
	}

	/**
	 * Service method for backtracking with serialization-based saving of {@link ConstraintNetwork}s.
	 * This method reinstates old {@link ConstraintNetwork}s after backtraking. 
	 */
	private void restoreCNs() {
		//REINSTATE OLD CNs
		HashMap<ConstraintSolver,byte[]> backup = backedUpCNs.lastElement();
		for (Entry<ConstraintSolver,byte[]> entry : backup.entrySet()) {
			byte[] backedUpNetwork = entry.getValue();
			ConstraintSolver cs = entry.getKey();
			logger.finest("Restoring CN of " + cs.getClass().getSimpleName());
			ByteArrayInputStream bis = new ByteArrayInputStream(backedUpNetwork);
	        ObjectInputStream in = null;
			try {
				in = new ObjectInputStream(bis);
		        ConstraintNetwork old = (ConstraintNetwork)in.readObject();
		        cs.setConstraintNetwork(old);
			}
			catch (IOException e) { e.printStackTrace(); }
			catch (ClassNotFoundException e) { e.printStackTrace(); }
		}
		backedUpCNs.remove(backup);
		backup.clear();
		logger.info("backup queue: " + (backedUpCNs.size()+1) + " --> " + backedUpCNs.size());
	}
	
	/**
	 * This backtrack method uses serialization to back up {@link ConstraintNetwork}s before branching.  This allows to
	 * backtrack without propagation - but is very memory intensive.  In practice, this does not work on reasonably
	 * sized problems.
	 */
	private boolean backtrackHelperWithSerialization(MetaVariable metaVariable) {
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
			for (ConstraintNetwork value : values) {
				if (animationTime != 0) {
					try { Thread.sleep(animationTime); }
					catch (InterruptedException e) { e.printStackTrace(); }
				}
				String valString = "";
				if (value.getVariables().length != 0) valString += "Vars = " + Arrays.toString(value.getVariables());
				if (value.getConstraints().length != 0) valString += " Cons = " + Arrays.toString(value.getConstraints());
				logger.fine("Trying value: " + valString);
								
				this.backedUpCNs.add(backupCNs(this));
				
				/*** PRINT INFO ***/
				/*
				long sizeOfBackup = 0;
				for (HashMap<ConstraintSolver,byte[]> oneHM : backedUpCNs) {
					for (byte[] oneCN : oneHM.values())
						sizeOfBackup += oneCN.length;
				}
				DecimalFormat df = new DecimalFormat("#.##");
				logger.info("Current backup size: " + df.format((sizeOfBackup/1024.00)) + " KB");
				*/
				/*** END PRINT INFO ***/
				
				if (this.addResolver(mostProblematicNetwork, value)) {
					this.resolvers.put(mostProblematicNetwork, value);
					this.metaVarsToMetaCons.put(mostProblematicNetwork,metaVariable.getMetaConstraint());
					this.resolversInverseMapping.put(value,mostProblematicNetwork);
					this.counterMoves++;

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
					if (backtrackHelper(newConflict)) return true;					
					logger.fine("Retracting value: " + Arrays.toString(value.getConstraints()));
					
					//this.retractResolver(mostProblematicNetwork, value);
					this.restoreCNs();
					this.retractResolverSub(mostProblematicNetwork, value);
					this.resolvers.remove(mostProblematicNetwork);
					this.metaVarsToMetaCons.remove(mostProblematicNetwork);
					this.resolversInverseMapping.remove(value);
					this.counterMoves--;
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

	protected final boolean addResolver(ConstraintNetwork metaVarConstraintNetwork, ConstraintNetwork resolverNetwork) {		
		if (!this.addResolverSub(metaVarConstraintNetwork, resolverNetwork)) return false;
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

	protected final void retractResolver(ConstraintNetwork metaVar, ConstraintNetwork res) {
		this.logger.finest("Retracting resolver:");
		this.logger.finest("  MetaVariable: " + metaVar.toString());
		this.logger.finest("  MetaValue: " + res.toString());
		
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
		this.logger.finest("Done retracting resolver.");
	}
	
	@Override
	public boolean propagate() {
		// TODO Auto-generated method stub
		return false;
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
	protected abstract boolean addResolverSub(ConstraintNetwork metaVariable, ConstraintNetwork metaValue);
	
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
		ret += this.variableType.getSimpleName();
		ret += "] constraints: [";
		for (int i = 0; i < this.constraintTypes.length; i++) {
			ret += this.constraintTypes[i].getSimpleName();
			if (i != this.constraintTypes.length-1) ret += ",";
		}
		ret += "]";
		nesting++;
//		for (MetaConstraintSolver cs : this.nextMetaConstraintSolvers) ret += "\n" + cs.getDescription();
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
		this.metaVarsToMetaCons.clear();
	}

	public int getCounterMoves() {
		return counterMoves;
	}
	
	public FocusConstraint getCurrentFocusConstraint() {
		return currentFocus;
	}

//	private void removeFromConstraintSolvers() {
//		if (currentFocus != null) {
//			for (ConstraintSolver cs : this.getConstraintSolvers()) {
//				cs.removeConstraint(currentFocus);
//			}
//		}		
//	}
//	
//	private void addToConstraintSolvers() {
//		for (ConstraintSolver cs : this.getConstraintSolvers()) {
//			cs.addConstraint(currentFocus);
//		}
//	}
	
	public synchronized void setCurrentFocusConstraint(FocusConstraint focus) {
		this.currentFocus = focus;
	}
	
	public synchronized Variable[] getFocused() {
		if (currentFocus != null) return currentFocus.getScope();
		return null;
	}
	
	public synchronized void setFocus(Variable ... vars) {
		currentFocus = new FocusConstraint();
		currentFocus.setScope(vars);
	}
	
	public synchronized boolean isFocused(Variable var) {
		if (currentFocus == null) return false;
		for (Variable v : getFocused()) {
			if (v.equals(var)) return true;
		}
		return false;
	}

	public synchronized void focus(Variable ... vars) {
		if (currentFocus == null) {
			currentFocus = new FocusConstraint();
		}
		Vector<Variable> scopeVars = new Vector<Variable>();
		for (Variable v : currentFocus.getScope()) scopeVars.add(v);
		for (Variable v : vars) scopeVars.add(v);
		currentFocus.setScope(scopeVars.toArray(new Variable[scopeVars.size()]));
	}
	
	public synchronized void removeFromCurrentFocus(Variable ... vars) {
		if (currentFocus == null) throw new NoFocusDefinedException(vars);
		Vector<Variable> newScope = new Vector<Variable>();
		for (Variable vOld : currentFocus.getScope()) {
			boolean found = false;
			for (Variable vToRem : vars) {
				if (vOld.equals(vToRem)) found = true;
			}
			if (!found) newScope.add(vOld);
		}
		currentFocus.setScope(newScope.toArray(new Variable[newScope.size()]));
	}

	public void setCounterMoves(int counterMoves) {
		this.counterMoves = counterMoves;
	}

	public HashMap<ConstraintNetwork, ConstraintNetwork> getResolvers() {
		return resolvers;
	}
	
	public HashMap<ConstraintNetwork, ConstraintNetwork> getResolversInverseMapping() {
		return resolversInverseMapping;
	}

	public void setResolvers(HashMap<ConstraintNetwork, ConstraintNetwork> resolvers) {
		this.resolvers = resolvers;
	}
	
	public MetaConstraint getMetaConstraint(ConstraintNetwork metaVariable) {
		return this.metaVarsToMetaCons.get(metaVariable);
	}
	

	@Override
	public void registerValueChoiceFunctions() {
		// TODO Auto-generated method stub		
	}


}
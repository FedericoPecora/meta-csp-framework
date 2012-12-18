package framework;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Logger;

import throwables.ConstraintNotFound;
import throwables.IllegalVariableRemoval;
import throwables.VariableNotFound;
import utility.logging.MetaCSPLogging;
import framework.multi.MultiBinaryConstraint;
import framework.multi.MultiConstraint;
import framework.multi.MultiConstraintSolver;
import framework.multi.MultiVariable;

/**
 * This class provides common infrastructure and functionality for
 * all constraint solvers. All constraint solvers should implement either
 * this class or the {@link MultiConstraint} class.  The latter is in fact an extension of this class,
 * as it implements the addConstraintSub(), addConstraintsSub(), removeConstraintSub() and
 * removeConstraintsSub() methods.
 * 
 * This abstract class also maintains a {@link ConstraintNetwork}, which is
 * represented internally as a graph.  Any specific solver must implement this abstract class
 * to provide methods to create/remove variables, add/remove constraints, and perform
 * propagation.
 * 
 * @author Federico Pecora
 */
public abstract class ConstraintSolver implements Serializable {
	
	protected Class<?>[] constraintTypes = {};
	protected Class<?>[] variableTypes = {};
	protected static int nesting = 0;
	protected static String spacing = "  ";
	private static final long serialVersionUID = 7526472295622776147L;
	
	/**
	 * Access to the underlying constraint network.
	 */
	protected ConstraintNetwork theNetwork;
	
	/**
	 * General class options. Options currently available:
	 * 
	 * <ul>
	 * <li> {@code AUTO_PROPAGATE}: if set, the constraint solver will call (user implemented) propagate method
	 * automatically.  Do not set if propagations must be dealt with in a more sophisticated way (e.g., incremental propagators).
	 * </li>
	 * <li> {@code DOMAINS_AUTO_INSTANTIATED}: if set, the constraint solver will not check whether domains
	 * are instantiated before propagation.
	 * </li>
	 * </ul>
	 */
	public static enum OPTIONS {AUTO_PROPAGATE,MANUAL_PROPAGATE,DOMAINS_AUTO_INSTANTIATED,DOMAINS_MANUALLY_INSTANTIATED};

	//internal options
	protected boolean autoprop = false;
	private boolean domainsAutoInstantiated = false;
	//protected boolean seqConstraints = false;
	
	//have domains been instantiated? if not, propagation will be delayed...
	private boolean domainsInstantiated = false;
	
	protected HashMap<String,ArrayList<Variable>> components = new HashMap<String,ArrayList<Variable>>();

	private transient  Logger logger = MetaCSPLogging.getLogger(this.getClass());
	
	/**
	 * Default constructor for this class.  Calls to subclass constructors will
	 * call this by default.
	 */
	protected ConstraintSolver(Class<?>[] constraintTypes, Class<?>[] variableTypes) {
		this.theNetwork = this.createConstraintNetwork();
		this.constraintTypes = constraintTypes;
		this.variableTypes = variableTypes;
	}
	
	/**
	 * Set options for this {@link ConstraintSolver}.
	 * @param ops Options to set (see {@link OPTIONS}).
	 */
	public void setOptions(OPTIONS ...ops) {
		for (OPTIONS op : ops)
			if (op.equals(OPTIONS.AUTO_PROPAGATE)) autoprop = true;
			else if (op.equals(OPTIONS.MANUAL_PROPAGATE)) autoprop = false;
		    else if (op.equals(OPTIONS.DOMAINS_AUTO_INSTANTIATED)) domainsAutoInstantiated = true;
			else if (op.equals(OPTIONS.DOMAINS_MANUALLY_INSTANTIATED)) domainsAutoInstantiated = false;

	}

	/**
	 * Get option value for this {@link ConstraintSolver}.
	 * @param op The option to get (see {@link OPTIONS}).
	 */
	public boolean getOption(OPTIONS op) {
		if (op.equals(OPTIONS.AUTO_PROPAGATE)) return autoprop;
		else if (op.equals(OPTIONS.MANUAL_PROPAGATE)) return !autoprop;
		else if (op.equals(OPTIONS.DOMAINS_AUTO_INSTANTIATED)) return domainsAutoInstantiated;
		else if (op.equals(OPTIONS.DOMAINS_MANUALLY_INSTANTIATED)) return !domainsAutoInstantiated;

		return false;
	}
	
	/**
	 * Assess whether a {@link Constraint} is compatible with this {@link ConstraintSolver}.  This is used
	 * internally to ignore processing of incompatible constraints by {@link ConstraintSolver}s that are aggregated into
	 * {@link MultiConstraintSolver}s.
	 * @param c The {@link Constraint} to check.
	 * @return <code>true</code> iff the given {@link Constraint} is compatible with this {@link ConstraintSolver}.
	 */
	public boolean isCompatible(Constraint c) {
		for (Class<?> conType : constraintTypes) if (conType.isInstance(c)) return true;
			//if (conType.equals(c.getClass())) return true;
		return false;
	}
	
	/**
	 * Method to create a {@link ConstraintNetwork} for this {@link ConstraintSolver}.
	 * This method must be implemented by the developer of the 
	 * specific {@link ConstraintSolver} implementation.  It is called by the
	 * default constructor upon class instantiation. 
	 * @return The {@link ConstraintNetwork} that will be used by this {@link ConstraintSolver}.
	 */
	protected abstract ConstraintNetwork createConstraintNetwork();
	
	/**
	 * Propagate the constraint network.
	 * @return A boolean value indicating the results of the propagation.
	 */
	public abstract boolean propagate();

	/**
	 * Add a constraint between {@link Variable}s.
	 * @param c The constraint to add.
	 * @return <code>true</code> iff the constraint was added successfully.
	 */
	public final boolean addConstraint(Constraint c) {
		 return this.addConstraints(c);
	}
//	public final boolean addConstraint(Constraint c) {
//		if (c == null) return true;		
//		if (isCompatible(c)) {
//			/**/
//			// if we are in presence of a multi constraint, i.e. a constraint which entails multiple constraints...
//			if (c instanceof MultiConstraint) {
//				// MC: our current MultiConstraint
//				MultiConstraint mc = (MultiConstraint)c;
//				// MV: our source node which the MC refers to
//				MultiVariable mv = (MultiVariable)mc.getScope()[0];
//				
//				for (ConstraintSolver cs : mv.getInternalConstraintSolvers()) {
//					boolean prop = false;
//					if (mc.propagateImmediately() && cs.addConstraints(mc.getInternalConstraints())) {
//						prop = true;
//					}
//					if (!prop && !((MultiConstraintSolver)mc.getScope()[0].getConstraintSolver()).getOption(MultiConstraintSolver.OPTIONS.ALLOW_INCONSISTENCIES)) {
//						return false;
//					}
//				}
//			}/**/
//			if (addConstraintSub(c)) {
//				this.theNetwork.addConstraint(c);
//				if (autoprop && checkDomainsInstantiated()) { 
//					if (this.propagate()) {
//						logger.finest("Added constraint " + c);
//						return true;
//					}
//					logger.finest("Failed to add constraint " + c);
//					this.theNetwork.removeConstraint(c);
//				}
//				else {
//					logger.finest("Added constraint " + c);
//					return true;
//				}
//			}	
//			/**/
//			if (autoprop && checkDomainsInstantiated()) 
//				this.propagate();
//			return false;
//		}
//		return true;
//	}
	
	/**
	 * This method must be implemented by the developer of the specific {@link ConstraintSolver}
	 * class.  Should implement all operations necessary to add a constraint, and should return
	 * <code>true</code>upon success, <code>false</code> otherwise. 
	 * @param c  The constraint to add.
	 * @return <code>true</code> iff the constraint was added to the {@link ConstraintNetwork}. 
	 */
	protected abstract boolean addConstraintSub(Constraint c);
	
	/**
	 * Add a batch of constraints between {@link Variable}s.  This method is
	 * NOT implemented so as to perform only one propagation, rather it adds all constraints
	 * sequentially, and returns the first constraint that fails, {@code null} if no constraint
	 * fails. 
	 * @param c The constraints to add.
	 * @return The first constraint that fails, {@code null} if no constraint
	 * fails. 
	 */
	public final Constraint addConstraintsDebug(Constraint... c) {
		ArrayList<Constraint> addedSoFar = new ArrayList<Constraint>(c.length);
		for (Constraint con : c) {
			if (!this.addConstraint(con)) {
				this.removeConstraints(addedSoFar.toArray(new Constraint[addedSoFar.size()]));
				return con;
			}
			else { addedSoFar.add(con); }
		}
		return null;
	}
	
	
	/**
	 * Add a batch of constraints between {@link Variable}s.  This method is
	 * implemented so as to perform only one propagation, thus being more convenient than
	 * performing {@code c.length} invocations of {@link #addConstraint(Constraint c)}.
	 * In addition, all constraints are either accepted or rejected together.
	 * @param c The constraints to add.
	 * @return A boolean value indicating the success of adding the constraints.
	 */
	public final boolean addConstraints(Constraint... c) {
		if (c == null || c.length == 0){ 
			return true;
		}
		
		//if (c.length == 1) return this.addConstraint(c[0]);
		
		ArrayList<MultiConstraint> added = new ArrayList<MultiConstraint>(c.length);
		HashMap<ConstraintSolver, ArrayList<Constraint>> sortedCons = new HashMap<ConstraintSolver, ArrayList<Constraint>>();
		ArrayList<Constraint> incomp = new ArrayList<Constraint>(c.length);
		for (Constraint con : c) {
			if (isCompatible(con)) {
				/**/
				// if we are in presence of a multi constraint, i.e. a constraint which entails multiple constraints...
				if (con instanceof MultiConstraint) {
					// MC: our current MultiConstraint
					MultiConstraint mc = (MultiConstraint)con;
					// MV: our source node which the MC refers to
					MultiVariable mv = (MultiVariable)mc.getScope()[0];
					
					for (ConstraintSolver cs : mv.getInternalConstraintSolvers()) {
						if (mc.propagateImmediately()) {
							added.add(mc);
							if (!sortedCons.containsKey(cs)) {
								sortedCons.put(cs, new ArrayList<Constraint>());
							}
							if (mc.getInternalConstraints() != null) {
								for (Constraint ic : mc.getInternalConstraints()) {
									sortedCons.get(cs).add(ic);
								}
							}
						}

//						if (mc.propagateImmediately() && cs.addConstraints(mc.getInternalConstraints())) {
//							added.add(mc);
//							prop = true;
//						}
//						if (!prop && !((MultiConstraintSolver)mc.getScope()[0].getConstraintSolver()).getOption(MultiConstraintSolver.OPTIONS.ALLOW_INCONSISTENCIES)) {
//							for (MultiConstraint mcadded : added) {
//								MultiVariable mvadded = (MultiVariable)mcadded.getScope()[0];
//								for (ConstraintSolver cs1 : mvadded.getInternalConstraintSolvers()) {
//									logger.finest("hahaha " + Arrays.toString(mc.getInternalConstraints()));
//									cs1.removeConstraints(mcadded.getInternalConstraints());
//								}
//							}
//							return false;
//						}
					}
				}/**/
			}
			else incomp.add(con);
		}
				
		//now filter out the incompatible constraints (we don't wanna fail, just let them pass silently)		
		ArrayList<Constraint> toAdd = new ArrayList<Constraint>(c.length);

		for (Constraint con : c) if (!incomp.contains(con)) toAdd.add(con);
		Constraint[] toAddArray = toAdd.toArray(new Constraint[toAdd.size()]);
		
		if (toAddArray.length == 0) return true;
		
		//add collected internal constraints for each internal solver...
		HashMap<ConstraintSolver, ArrayList<Constraint>> sortedConsRetract = new HashMap<ConstraintSolver, ArrayList<Constraint>>();
		for (ConstraintSolver cs : sortedCons.keySet()) {
			if (cs.addConstraints(sortedCons.get(cs).toArray(new Constraint[sortedCons.get(cs).size()]))) sortedConsRetract.put(cs, sortedCons.get(cs));
			else {
				for (ConstraintSolver cs1 : sortedConsRetract.keySet()) cs1.removeConstraints(sortedConsRetract.get(cs1).toArray(new Constraint[sortedConsRetract.get(cs1).size()]));
				logger.finest("Failed to add constraints " + Arrays.toString(toAddArray));
				return false;
			}
		}
		if (addConstraintsSub(toAddArray)) {
			for (Constraint con : toAddArray) this.theNetwork.addConstraint(con);
			if (autoprop && checkDomainsInstantiated()) { 
				if (this.propagate()) {
					logger.finest("Added constraints " + Arrays.toString(toAddArray));
					return true;
				}
				for (Constraint con : toAddArray) {
					logger.finest("Failed to add constraints " + Arrays.toString(toAddArray));
					this.theNetwork.removeConstraint(con);
				}
			}
			else {
				logger.finest("Added constraints " + Arrays.toString(toAddArray));
				return true;
			}
		}
		//something went wrong... retract what's already added
		/**/
		
		for (ConstraintSolver cs1 : sortedConsRetract.keySet()) cs1.removeConstraints(sortedConsRetract.get(cs1).toArray(new Constraint[sortedConsRetract.get(cs1).size()]));
		logger.finest("Failed to add constraints " + Arrays.toString(toAddArray));		
//		for (MultiConstraint mcadded : added) {
//			MultiVariable mvadded = (MultiVariable)mcadded.getScope()[0];
//			for (ConstraintSolver cs : mvadded.getInternalConstraintSolvers())
//				cs.removeConstraints(mcadded.getInternalConstraints());
//		}/**/
		
		if (autoprop && checkDomainsInstantiated()) this.propagate();
		return false;
	}

	/**
	 * This method must be implemented by the developer of the specific {@link ConstraintSolver}
	 * class.  It should implement all operations necessary to add multiple constraints, and should return
	 * <code>true</code> upon success, <code>false</code> otherwise. 
	 * @param c  The constraints to add.
	 * @return <code>true</code> iff the constraints were added to the {@link ConstraintNetwork}. 
	 */
	protected abstract boolean addConstraintsSub(Constraint[] c);
		
	/**
	 * Retract a constraint between {@link Variable}s.
	 * @param c The constraint to retract.
	 */
	public final void removeConstraint(Constraint c) throws ConstraintNotFound {
		if (c != null) {
			if (isCompatible(c)) {
				if (!this.theNetwork.containsConstraint(c)) throw new ConstraintNotFound(c);
				/**/
				if (c instanceof MultiConstraint) {
					MultiConstraint mc = (MultiConstraint)c;
					MultiVariable mv = (MultiVariable)mc.getScope()[0];
					if (mc.getInternalConstraints() != null) for (ConstraintSolver cs : mv.getInternalConstraintSolvers()) cs.removeConstraints(mc.getInternalConstraints());
				}/**/
				removeConstraintSub(c);
				this.theNetwork.removeConstraint(c);
				if (autoprop && checkDomainsInstantiated())
					this.propagate();
			}
		}
	}
	
	/**
	 * This method must be implemented by the developer of the specific {@link ConstraintSolver}
	 * class.  Should implement all operations necessary to remove a constraint. 
	 * @param c  The constraint to remove. 
	 */
	protected abstract void removeConstraintSub(Constraint c);

	/**
	 * Retract a batch of constraints between {@link Variable}s.  This method should
	 * be implemented so as to perform only one propagation, thus being more convenient than
	 * performing {@code c.length} invocations of {@link #removeConstraint(Constraint c)}.
	 * In addition, all constraints should be either accepted or rejected together.
	 * @param c The constraints to add.
	 */
	public final void removeConstraints(Constraint[] c) throws ConstraintNotFound {
		if (c != null && c.length != 0) {
			Vector<Constraint> incomp = new Vector<Constraint>();
			HashMap<ConstraintSolver,ArrayList<Constraint>> internalCons = new HashMap<ConstraintSolver, ArrayList<Constraint>>();
			for (Constraint con : c) {
				if (isCompatible(con)) {
					if (!this.theNetwork.containsConstraint(con)) throw new ConstraintNotFound(con);
					/**/
					if (con instanceof MultiConstraint) {
						MultiConstraint mc = (MultiConstraint)con;
						MultiVariable mv = (MultiVariable)mc.getScope()[0];
						for (ConstraintSolver cs : mv.getInternalConstraintSolvers()) {
							//cs.removeConstraints(mc.getInternalConstraints());
							if (!internalCons.containsKey(cs)) internalCons.put(cs,new ArrayList<Constraint>());
							if (mc.getInternalConstraints() != null) for (Constraint c1 : mc.getInternalConstraints()) internalCons.get(cs).add(c1);
						}
					}
					/**/
				}
				else incomp.add(con);
			}
			
			//now filter out the incompatible constraints (we don't wanna fail, just let them pass silently)
			Vector<Constraint> toRemove = new Vector<Constraint>();
			for (Constraint con : c) if (!incomp.contains(con)) toRemove.add(con);
			Constraint[] toRemoveArray = toRemove.toArray(new Constraint[toRemove.size()]);

			//get rid of internal constraints
			for (ConstraintSolver cs : internalCons.keySet()) {
				cs.removeConstraints(internalCons.get(cs).toArray(new Constraint[internalCons.get(cs).size()]));
			}
			
			removeConstraintsSub(toRemoveArray);
			for (Constraint con : toRemove) this.theNetwork.removeConstraint(con);
			if (autoprop && checkDomainsInstantiated()) this.propagate();
		}
	}

	/**
	 * This method must be implemented by the developer of the specific {@link ConstraintSolver}
	 * class.  Should implement all operations necessary to remove a batch of constraints. 
	 * @param c  The constraints to remove. 
	 */
	protected abstract void removeConstraintsSub(Constraint[] c);


	/**
	 * Create a new {@link Variable} for this {@link ConstraintSolver}, and
	 * assign it to the given component label.
	 * @param component The component label to which assign the new variable.
	 * @return A new {@link Variable}.
	 */
	public final Variable createVariable(String component) {
		Variable ret = createVariable();
		if (!components.containsKey(component)) components.put(component, new ArrayList<Variable>()); 
		components.get(component).add(ret);
		return ret;
	}

	
	/**
	 * Factory method for creating a new {@link Variable} for this {@link ConstraintSolver}.
	 * @return A new {@link Variable}.
	 */
	public final Variable createVariable() {
		Variable ret = createVariableSub();
		this.theNetwork.addVariable(ret);
		/**/
		if (ret instanceof MultiVariable) {
			MultiVariable mv = (MultiVariable)ret;
			HashMap<ConstraintSolver, Constraint[]> added = new HashMap<ConstraintSolver, Constraint[]>();
			boolean oneFailed = false;
			for (ConstraintSolver cs : mv.getInternalConstraintSolvers()) {
				if (!cs.addConstraints(mv.getInternalConstraints())) {
					oneFailed = true;
					this.removeVariable(ret);
				}
				else added.put(cs, mv.getInternalConstraints());
			}
			if (oneFailed) {
				for (ConstraintSolver cs : added.keySet()) {
					cs.removeConstraints(added.get(cs));
				}
				return null;
			}
			
		}
		/**/
		if (this.getOption(OPTIONS.DOMAINS_MANUALLY_INSTANTIATED)) this.domainsInstantiated = false;
		if (autoprop && checkDomainsInstantiated()) this.propagate();
		logger.finest("Created variable " + ret);
		return ret;
	}
	
	/**
	 * This method must be implemented by the developer of the specific {@link ConstraintSolver}
	 * class.  It should implement all operations necessary to create a variable for the specific
	 * type of {@link ConstraintSolver}. 
	 * @return  A new {@link Variable} for this {@link ConstraintSolver}.
	 */
	protected abstract Variable createVariableSub();


	/**
	 * Create a batch of new {@link Variable}s for this {@link ConstraintSolver}.
	 * @param num The number of variables to create.
	 * @param component The component tag to associate to these new variables.
	 * @return A batch of new {@link Variable}s.
	 */
	public final Variable[] createVariables(int num, String component) {
		Variable[] ret = createVariables(num);
		if (!components.containsKey(component)) components.put(component, new ArrayList<Variable>());
		for (Variable var : ret) components.get(component).add(var);
		return ret;
	}
	
	/**
	 * Create a batch of new {@link Variable}s for this {@link ConstraintSolver}.
	 * @param num The number of variables to create.
	 * @return A batch of new {@link Variable}s.
	 */
	public final Variable[] createVariables(int num) {
		Variable[] ret = createVariablesSub(num);
		Vector<MultiVariable> added = new Vector<MultiVariable>();
		//need to add all to network so if sth goes wrong I can delete all of them concurrently
		for (Variable v : ret) this.theNetwork.addVariable(v);
		for (Variable v : ret) {
			/**/
			if (v instanceof MultiVariable) {
				MultiVariable mv = (MultiVariable)v;
				for (ConstraintSolver cs : mv.getInternalConstraintSolvers()) {
					if (cs.addConstraints(mv.getInternalConstraints())) added.add(mv);
					else {
						for (MultiVariable mvadded : added) {
							for (ConstraintSolver cs1 : mvadded.getInternalConstraintSolvers())
								cs1.removeConstraints(mv.getInternalConstraints());
						}
						this.removeVariablesSub(ret);
						return null;
					}					
				}
			}/**/
		}
		if (autoprop && checkDomainsInstantiated()) this.propagate();
		logger.finest("Created variables " + Arrays.toString(ret));
		return ret;
	}
	
	private boolean checkDomainsInstantiated() {
		if (domainsInstantiated) return true;
		if (this.theNetwork.checkDomainsInstantiated() == null) {
			domainsInstantiated = true;
			return true;
		}
		return false;
	}

	/**
	 * This method must be implemented by the developer of the specific {@link ConstraintSolver}
	 * class.  It should implement all operations necessary to create a batch of variable for the specific
	 * type of {@link ConstraintSolver}. 
	 * @return  A batch of new {@link Variable} for this {@link ConstraintSolver}.
	 */
	protected abstract Variable[] createVariablesSub(int num);

	/**
	 * Remove a {@link Variable} from this {@link ConstraintSolver}.
	 * @param v The {@link Variable} to remove.
	 */
	public final void removeVariable(Variable v) throws VariableNotFound, IllegalVariableRemoval {
		if (!this.theNetwork.containsVariable(v)) throw new VariableNotFound(v);
		if (this.theNetwork.getIncidentEdges(v) != null && this.theNetwork.getIncidentEdges(v).length != 0)
			throw new IllegalVariableRemoval(v, this.theNetwork.getIncidentEdges(v));
		/**/
		if (v instanceof MultiVariable) {
			MultiVariable mv = (MultiVariable)v;
			Constraint[] internalCons = mv.getInternalConstraints();
			for (ConstraintSolver cs : mv.getInternalConstraintSolvers())
				cs.removeConstraints(internalCons);
		}/**/
//		Constraint[] incidentEdges = this.theNetwork.getIncidentEdges(v); 
//		this.removeConstraints(incidentEdges);
		this.theNetwork.removeVariable(v);
		removeVariableSub(v);
		for (ArrayList<Variable> vec : components.values()) {
			if (vec.contains(v)) vec.remove(v);
		}
		if (autoprop && checkDomainsInstantiated()) this.propagate();
		logger.finest("Removed variable " + v);

	} 
	
	/**
	 * This method must be implemented by the developer of the specific {@link ConstraintSolver}
	 * class.  It should implement all operations necessary to remove a variable for
	 * the specific type of {@link ConstraintSolver}. 
	 * @param v The {@link Variable} to remove.
	 */
	protected abstract void removeVariableSub(Variable v);
	
	/**
	 * Remove a batch of {@link Variable}s from this {@link ConstraintSolver}.
	 * @param v The batch of {@link Variable}s to remove.
	 */
	public final void removeVariables(Variable[] v) throws VariableNotFound, IllegalVariableRemoval {
		for (Variable var : v) {
			if (!this.theNetwork.containsVariable(var)) throw new VariableNotFound(var);
			if (this.theNetwork.getIncidentEdges(var) != null && this.theNetwork.getIncidentEdges(var).length != 0)
//				throw new IllegalVariableRemoval(var, this.theNetwork.getIncidentEdges(var));
				continue;
			/**/
			if (var instanceof MultiVariable) {
				MultiVariable mv = (MultiVariable)var;
				for (ConstraintSolver cs : mv.getInternalConstraintSolvers())
					cs.removeConstraints(mv.getInternalConstraints());
			}/**/
		}
		for (Variable var : v) {
			this.theNetwork.removeVariable(var);
			removeVariablesSub(v);
			for (ArrayList<Variable> vec : components.values()) {
				vec.removeAll(Arrays.asList(v));
			}
		}
		if (autoprop && checkDomainsInstantiated()) this.propagate();
		logger.finest("Removed variables " + Arrays.toString(v));
	}

	/**
	 * This method must be implemented by the developer of the specific {@link ConstraintSolver}
	 * class.  It should implement all operations necessary to remove a batch of variables for
	 * the specific type of {@link ConstraintSolver}. 
	 * @param v The {@link Variable}s to remove.
	 */
	protected abstract void removeVariablesSub(Variable[] v);
	
	/**
	 * Get the {@link ConstraintNetwork} of this {@link ConstraintSolver}.
	 * @return the {@link ConstraintNetwork} of this {@link ConstraintSolver}.
	 */
	public ConstraintNetwork getConstraintNetwork() { return theNetwork; }
	
	/**
	 * Get a {@link Variable} given its ID. 
	 * @param id The ID of the {@link Variable} to get.
	 * @return The {@link Variable} whose ID is id.
	 */
	public Variable getVariable(int id) {
		return this.theNetwork.getVariable(id);
	}
	
	/**
	 * Get the ID of a {@link Variable}. 
	 * @param var The {@link Variable} of which to get the ID.
	 * @return The ID of the given variable.
	 */
	public int getID(Variable var) {
		Variable[] vars = this.getVariables();
		for (int i = 0; i < vars.length; i++) {
			if (vars[i].equals(var)) return i;
		}
		return -1;
	}
	
	/**
	 * Get all the {@link Variable}s contained in this {@link ConstraintSolver}'s {@link ConstraintNetwork}.
	 * @return all the {@link Variable}s contained in this {@link ConstraintSolver}'s {@link ConstraintNetwork}.
	 */
	public Variable[] getVariables() {
		return this.theNetwork.getVariables();
	}

	/**
	 * Get all the {@link Variable}s contained in this {@link ConstraintSolver}'s {@link ConstraintNetwork}.
	 * @return all the {@link Variable}s contained in this {@link ConstraintSolver}'s {@link ConstraintNetwork}.
	 */
	public Variable[] getVariables(String component) {
		ArrayList<Variable> ret = this.components.get(component);
		if (ret == null) return new Variable[0];
		return ret.toArray(new Variable[ret.size()]);
	}

	/**
	 * Get all the {@link Constraint}s contained in this {@link ConstraintSolver}'s {@link ConstraintNetwork}.
	 * @return all the {@link Constraint}s contained in this {@link ConstraintSolver}'s {@link ConstraintNetwork}.
	 */
	public Constraint[] getConstraints() {
		return this.theNetwork.getConstraints();
	}
	
	/**
	 * Get all {@link Constraint}s contained in this {@link ConstraintSolver}'s {@link ConstraintNetwork}
	 * with given source and destination {@link Variable}s.
	 * @param from The source {@link Variable} of the {@link Constraint}s to be obtained.
	 * @param to The destination {@link Variable} of the {@link Constraint}s to be obtained.
	 * @return The {@link Constraint}s contained in this {@link ConstraintSolver}'s {@link ConstraintNetwork}
	 * with given source and destination {@link Variable}s.
	 */
	public Constraint[] getConstraints(Variable from, Variable to) {
		Vector<Constraint> ret = new Vector<Constraint>();
		for (Constraint con : this.getConstraints()) {
			if (con instanceof BinaryConstraint) {
				if (((BinaryConstraint) con).getFrom().equals(from) &&
						((BinaryConstraint) con).getTo().equals(to)) ret.add(con);
			}
			/**/
			else if (con instanceof MultiBinaryConstraint) {
				if (((MultiBinaryConstraint) con).getFrom().equals(from) &&
						((MultiBinaryConstraint) con).getTo().equals(to)) ret.add(con);
			}/**/
		}
		return ret.toArray(new Constraint[ret.size()]);
	}
	
	/**
	 * Get the component of a given {@link Variable}.  A component is a {@link String} labeling of {@link Variable}s.
	 * @param v The {@link Variable} of which to get the component.
	 * @return A {@link String} representing the component of the given {@link Variable}.
	 */
	public String getComponent(Variable v) {
		for (String s : components.keySet()) {
			if (components.get(s).contains(v)) return s;
		}
		return null;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": "+ Arrays.asList(this.getVariables());
	}
	
	/**
	 * Gets a description of this {@link MultiConstraintSolver} stating which variable and constraint types it supports.  
	 * @return A description of this {@link MultiConstraintSolver} stating which variable and constraint types it supports.
	 */
	public String getDescription() {
		String spacer = "";
		for (int i = 0; i < nesting; i++) spacer += spacing;
		String ret = spacer + "[" + this.getClass().getSimpleName() + " vars: [";
		for (Class<?> c : this.variableTypes) ret += c.getSimpleName();
		ret += "] constraints: [";
		for (Class<?> c : this.constraintTypes) ret += c.getSimpleName();
		return ret + "]]";
	}

	public HashMap<String, ArrayList<Variable>> getComponents() {
		return components;
	}

	public void setComponents(HashMap<String, ArrayList<Variable>> components) {
		this.components = components;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		logger = MetaCSPLogging.getLogger(this.getClass());
	}
}

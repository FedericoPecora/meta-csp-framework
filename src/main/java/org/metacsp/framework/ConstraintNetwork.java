package org.metacsp.framework;

import java.awt.EventQueue;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.JFrame;

import org.apache.commons.collections15.map.HashedMap;
import org.metacsp.framework.meta.MetaConstraintSolver;
import org.metacsp.framework.multi.MultiBinaryConstraint;
import org.metacsp.throwables.NonInstantiatedDomain;
import org.metacsp.utility.UI.Callback;
import org.metacsp.utility.UI.ConstraintNetworkFrame;
import org.metacsp.utility.logging.MetaCSPLogging;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.ObservableGraph;


/**
 * This class implements the necessary functionality to maintain networks of {@link Constraint}s.  It is used by all
 * {@link ConstraintSolver}s and provides methods to add and remove {@link Variable}s, {@link Constraint}s, querying the
 * network for constraints, incident edges, etc.  It also provide basic graphical rendering functions.
 * 
 * @author Federico Pecora
 */

public class ConstraintNetwork implements Cloneable, Serializable  {

	//For changelistener
	private List<ConstraintNetworkChangeListener> listeners = null;

	public void addConstraintNetworkChangeListener(ConstraintNetworkChangeListener listener) {
		if (listeners == null) listeners = new ArrayList<ConstraintNetworkChangeListener>();
		listeners.add(listener);
	}

	private void dispatchEvent(ConstraintNetwork added, ConstraintNetwork removed) {
		final ConstraintNetworkChangeEvent event = new ConstraintNetworkChangeEvent(this, added, removed);
		for (ConstraintNetworkChangeListener l : listeners) {
			dispatchRunnableOnEventQueue(l, event);
		}
	}

	private void dispatchRunnableOnEventQueue(final ConstraintNetworkChangeListener listener, final ConstraintNetworkChangeEvent event) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				listener.stateChanged(event);
			}
		});
	}
	//end for changelistener
	
	public static HashMap<FieldOfObject,Object> backupForSerialization = new HashMap<FieldOfObject,Object>();

	private class FieldOfObject {
		private Field field;
		private int ID;
		private FieldOfObject(Field field) {
			this.ID = getID();
			this.field = field;
		}
		public boolean equals(Object o) {
			FieldOfObject foo = (FieldOfObject)o;
			return (foo.ID == this.ID && foo.field.getName().equals(this.field.getName()));
		}
		public int hashCode() {
			return this.toString().hashCode();
		}
		public String toString() {
			return "FieldOfObject <" + ID + "," + field.getName() + ">";
		}
	}

	protected ConstraintSolver solver;
	protected ObservableGraph<Variable,Constraint> graph;
	protected DirectedSparseMultigraph<Variable,Constraint> g;
	protected HashMap<Integer, Variable> variables = new HashMap<Integer, Variable>();
	protected HashMap<Variable, Integer> variablesR = new HashMap<Variable, Integer>();
	protected HashMap<VariablePrototype,Variable> substitutions = new HashMap<VariablePrototype, Variable>();
	protected HashMap<Variable,VariablePrototype> substituted = new HashMap<Variable,VariablePrototype>();

	protected HashMap<Constraint,DummyVariable> hyperEdges = new HashMap<Constraint, DummyVariable>();

	public ObservableGraph<Variable,Constraint> getGraph() { return graph; }
	
	private transient Logger logger = MetaCSPLogging.getLogger(this.getClass());
	private static final long serialVersionUID = 7526472295622776148L;

	private double weight=-1;

	public transient Object annotation;
	public transient Object specilizedAnnotation;
	public ConstraintNetworkMarking marking; // to mark the ConstraintNetwork in the backtracking process

	public static int IDs = 0;
	public int ID = IDs++;

	public int getID() { return this.ID; }

	public Object getSpecilizedAnnotation() {
		return specilizedAnnotation;
	}

	public void setSpecilizedAnnotation(Object specilizedAnnotation) {
		this.specilizedAnnotation = specilizedAnnotation;
	}

	//This is so that subclasses must invoke 1-arg constructor of ConstraintNetwork (below)
	@SuppressWarnings("unused")
	private ConstraintNetwork() {};

	/**
	 * Convenience method to keep track of correspondences between {@link VariablePrototype}s and {@link Variable}s (useful
	 * when {@link ConstraintNetwork}s are used as meta-values in {@link MetaConstraintSolver}s).
	 * @param vp The {@link VariablePrototype}.
	 * @param v The {@link Variable} that corresponds to the given {@link VariablePrototype}.
	 */
	public void addSubstitution(VariablePrototype vp, Variable v) {
		substitutions.put(vp, v);
		substituted.put(v,vp);
		logger.finest("Added susbstitution " + vp + " <-- " + v);
	}

	/**
	 * Convenience method to keep track of correspondences between {@link VariablePrototype}s and {@link Variable}s (useful
	 * when {@link ConstraintNetwork}s are used as meta-values in {@link MetaConstraintSolver}s).
	 * @param vp2v Mapping between {@link VariablePrototype}s and {@link Variable}s.
	 */	
	public void addSubstitutions(HashedMap<VariablePrototype,Variable> vp2v) {

		for(VariablePrototype vp: vp2v.keySet()){
			substitutions.put(vp, vp2v.get(vp));
			substituted.put(vp2v.get(vp),vp);
			logger.finest("Added susbstitution " + vp + " <-- " + vp2v.get(vp));
		}
	}


	/**
	 * Get the {@link Variable} corresponding to a given {@link VariablePrototype} (see addSubstitution() method).
	 * @param vp The {@link VariablePrototype} to look up.
	 * @return The {@link Variable} corresponding to the given {@link VariablePrototype}.
	 */
	public Variable getSubstitution(VariablePrototype vp) {
		return substitutions.get(vp);
	}

	/**
	 * Get the {@link VariablePrototype} corresponding to a given {@link Variable} (see addSubstitution() method).
	 * @param v The {@link Variable} to look up.
	 * @return The {@link VariablePrototype} corresponding to the given {@link Variable}.
	 */
	public VariablePrototype getSubstituted(Variable v) {
		return substituted.get(v);
	}

	/**
	 * Remove the {@link Variable} corresponding to a given {@link VariablePrototype} (see addSubstitution() method).
	 * @param vp The {@link VariablePrototype} to look up.
	 */
	public void removeSubstitution(VariablePrototype vp) {
		Variable v= this.substitutions.get(vp);
		substitutions.remove(vp);
		substituted.remove(v);
	}


	/**
	 * Instantiates a new {@link ConstraintNetwork}.  Note that all implementing classes must call this constructor
	 * (i.e., specifying a {@link ConstraintSolver} is mandatory).
	 * @param sol The {@link ConstraintSolver} which maintains this {@link ConstraintNetwork}.
	 */
	public ConstraintNetwork(ConstraintSolver sol) {
		solver = sol;
		g = new DirectedSparseMultigraph<Variable,Constraint>();
		graph = new ObservableGraph<Variable,Constraint>(g);
		this.weight=-1;
		this.annotation="NONE";
		this.marking=new ConstraintNetworkMarking();
	}

	/**
	 * Returns the {@link Constraint} between two given {@link Variable}s (if it exists).
	 * If more than one exists, it returns one of them (no guarantees as to which one).
	 * @param from The source variable of the {@link Constraint} to find.
	 * @param to The destination variable of the {@link Constraint} to find.
	 * @return The {@link Constraint} between the two given {@link Variable}s, <code>null</code> if
	 * no such {@link Constraint} exists. 
	 */
	public Constraint getConstraint(Variable from, Variable to) {
		return this.graph.findEdge(from, to);
	}

	/**
	 * Returns all {@link Constraint}s between two given {@link Variable}s.
	 * @param from The source variable of the {@link Constraint}s to find.
	 * @param to The destination variable of the {@link Constraint}s to find.
	 * @return All {@link Constraint}s between the two given {@link Variable}s, <code>null</code> if
	 * no such {@link Constraint} exists.
	 */
	public Constraint[] getConstraints(Variable from, Variable to) {
		Collection<Constraint> edges = this.graph.findEdgeSet(from, to);
		return edges.toArray(new Constraint[edges.size()]);
	}

	/**
	 * Adds a given {@link Variable} to the network.
	 * @param v The {@link Variable} to add to the network.
	 */
	public void addVariable(Variable v) {
		this.graph.addVertex(v);
		this.variables.put(Integer.valueOf(v.getID()), v);
		this.variablesR.put(v, Integer.valueOf(v.getID()));
		logger.finest("Added variable " + v);
		if (listeners != null) {
			ConstraintNetwork added = new ConstraintNetwork(this.solver);
			added.addVariable(v);
			dispatchEvent(added, null);
		}
	}

	/**
	 * Removes a given {@link Variable} from the network.
	 * @param v The {@link Variable} to remove from the network.
	 */
	public void removeVariable(Variable v) {
		this.graph.removeVertex(v);
		this.variables.remove(Integer.valueOf(v.getID()));
		this.variablesR.remove(v);
		logger.finest("Removed variable " + v);
		if (listeners != null) {
			ConstraintNetwork removed = new ConstraintNetwork(this.solver);
			removed.addVariable(v);
			dispatchEvent(null, removed);
		}
	}

	/**
	 * Adds a set of {@link Constraint}s to the network.
	 * @param cons The {@link Constraint}s to add to the network. NOTE: this only works for
	 * {@link BinaryConstraint}s and {@link MultiBinaryConstraint}s in the current implementation. 
	 */
	public void addConstraints(Constraint ... cons) {
		for (Constraint c : cons) this.addConstraint(c);
	}
	
	/**
	 * Adds a {@link Constraint} to the network.
	 * @param c The {@link Constraint} to add to the network. NOTE: this only works for
	 * {@link BinaryConstraint}s and {@link MultiBinaryConstraint}s in the current implementation. 
	 */
	public void addConstraint(Constraint c) {
		if (c instanceof BinaryConstraint || c instanceof MultiBinaryConstraint) {
			this.graph.addEdge(c, c.getScope()[0], c.getScope()[1]);
			logger.finest("Added binary constraint " + c);
			if (listeners != null) {
				ConstraintNetwork added = new ConstraintNetwork(this.solver);
				added.addConstraint(c);
				dispatchEvent(added, null);
			}
		}
		else {
			DummyVariable dv = new DummyVariable(this.solver, c.getEdgeLabel());
			hyperEdges.put(c, dv);
			graph.addVertex(dv);
			for (Variable var : c.getScope()) {
				DummyConstraint dm = new DummyConstraint("");
				dm.setScope(new Variable[] {dv, var});
				this.graph.addEdge(dm, dv, var);
			}
			logger.finest("Added constraint " + c);
			if (listeners != null) {
				ConstraintNetwork added = new ConstraintNetwork(this.solver);
				added.addConstraint(c);
				dispatchEvent(added, null);
			}
		}
	}

	/**
	 * Removes a given {@link Constraint} from the network.
	 * @param c The {@link Constraint} to remove from the network.
	 */
	public void removeConstraint(Constraint c) {
		if (c instanceof BinaryConstraint || c instanceof MultiBinaryConstraint) {
			this.graph.removeEdge(c);
			logger.finest("Removed binary constraint " + c);
			if (listeners != null) {
				ConstraintNetwork removed = new ConstraintNetwork(this.solver);
				removed.addConstraint(c);
				dispatchEvent(null, removed);
			}
		}
		else {
			if (!(c instanceof DummyConstraint)) {
				DummyVariable dv = hyperEdges.get(c);
				Collection<Constraint> incident = graph.getIncidentEdges(dv);
				for (Constraint auxCon : incident) this.graph.removeEdge(auxCon);
				graph.removeVertex(dv);
				hyperEdges.remove(c);
				logger.finest("Removed constraint " + c);
				if (listeners != null) {
					ConstraintNetwork removed = new ConstraintNetwork(this.solver);
					removed.addConstraint(c);
					dispatchEvent(null, removed);
				}
			}
		}
	}

	/**
	 * Gets the source {@link Variable} of a given (binary) {@link Constraint}.
	 * @param c The {@link Constraint} from which to get the source {@link Variable}.
	 * @return The source {@link Variable} of the given {@link Constraint}.
	 */
	public Variable getVariableFrom(Constraint c) {
		return graph.getSource(c);
	}

	/**
	 * Gets the destination {@link Variable} of a given (binary) {@link Constraint}.
	 * @param c The {@link Constraint} from which to get the destination {@link Variable}.
	 * @return The destination {@link Variable} of the given {@link Constraint}.
	 */
	public Variable getVariableTo(Constraint c) {
		return graph.getDest(c);
	}

	/**
	 * Gets a {@link Variable} given its ID.
	 * @param id The ID of the {@link Variable}.
	 * @return The {@link Variable} with the given ID (if it exists).
	 */
	public Variable getVariable(int id) {
		return this.variables.get(id);
	}

	/**
	 * Checks whether the domains of the {@link Variable}s in the {@link ConstraintNetwork} are instantiated.
	 * This is used by the {@link ConstraintSolver} class to assess whether automatic propagation
	 * should occur (in some CSPs, domains of {@link Variable}s cannot be instantiated by the {@link Variable} constructor,
	 * rather an explicit call to a dedicated method is necessary).
	 * @return <code>null</code> if all domains are instantiated; a {@link Variable} whose domain is not instantiated if one exists. 
	 */
	public Variable checkDomainsInstantiated() {
		for (Variable v : this.getVariables())
			if (v.getDomain() == null)
				return v;
		return null;
	}

	/*
	 * draw() is static so you can call this high-level
	 * version to draw an object of a subclass (Java
	 * enforces late-binding)
	 */
	/**
	 * A static method for drawing {@link ConstraintNetwork}s.  This method is static so that
	 * it can be called to draw an object of a subclass (Java enforces late-binding). This method instantiated a {@link JFrame}
	 * containing a rendering of the {@link ConstraintNetwork}.  The rendering is dynamic and attempts to animate transitions
	 * when {@link Constraint} and/or {@link Variable}s are added/removed from the network.
	 * @param cn The {@link ConstraintNetwork} to draw.
	 * @param title The title of the {@link JFrame} containing the rendered {@link ConstraintNetwork}. 
	 */
	public static void draw(ConstraintNetwork cn, String title) {
		Variable v = cn.checkDomainsInstantiated(); 
		if (v == null) new ConstraintNetworkFrame(cn.graph, title, null);
		else throw new NonInstantiatedDomain(v);
	}

	/**
	 * A static method for drawing {@link ConstraintNetwork}s.  This method is static so that
	 * it can be called to draw an object of a subclass (Java enforces late-binding). This method instantiated a {@link JFrame}
	 * containing a rendering of the {@link ConstraintNetwork}.  The rendering is dynamic and attempts to animate transitions
	 * when {@link Constraint} and/or {@link Variable}s are added/removed from the network.
	 * @param cn The {@link ConstraintNetwork} to draw.
	 * @param title The title of the {@link JFrame} containing the rendered {@link ConstraintNetwork}.
	 * @param cb A {@link Callback} object to use when the button is pressed. 
	 */
	public static void draw(ConstraintNetwork cn, String title, Callback cb) {
		Variable v = cn.checkDomainsInstantiated(); 
		if (v == null) new ConstraintNetworkFrame(cn.graph, title, cb);
		else throw new NonInstantiatedDomain(v);
	}

	/**
	 * Same as the static two-argument draw method, using the {@link ConstraintNetwork}'s simple class name as the
	 * title of the {@link JFrame}.
	 * @param cn The {@link ConstraintNetwork} to draw.
	 */
	public static void draw(ConstraintNetwork cn) {
		if (cn.getVariables().length > 0) ConstraintNetwork.draw(cn, cn.getVariable(0).getConstraintSolver().getClass().getSimpleName(), null);
		else ConstraintNetwork.draw(cn, cn.getClass().getSimpleName(), null);
	}

	/**
	 * Same as the static two-argument draw method, using the {@link ConstraintNetwork}'s simple class name as the
	 * title of the {@link JFrame}.
	 * @param cn The {@link ConstraintNetwork} to draw.
	 * @param cb A {@link Callback} object to use when the button is pressed. 
	 */
	public static void draw(ConstraintNetwork cn, Callback cb) {
		if (cn.getVariables().length > 0) ConstraintNetwork.draw(cn, cn.getVariable(0).getConstraintSolver().getClass().getSimpleName(), cb);
		else ConstraintNetwork.draw(cn, cn.getClass().getSimpleName(), cb);

	}

	/**
	 * Get all {@link Constraint}s involving a given {@link Variable}. 
	 * @param v The {@link Variable} involved in the {@link Constraint}s.
	 * @return All {@link Constraint}s involving the given {@link Variable}.
	 */
	public Constraint[] getIncidentEdges(Variable v) {
		Collection<Constraint> in = this.graph.getInEdges(v);
		Constraint[] inArray = new Constraint[0];
		if (in != null) inArray = in.toArray(new Constraint[in.size()]);
		Collection<Constraint> out = this.graph.getOutEdges(v);
		Constraint[] outArray = new Constraint[0];
		if (out != null) outArray = out.toArray(new Constraint[out.size()]);
		if (in == null && out == null) return null;
		HashSet<Constraint> retSet = new HashSet<Constraint>();
		for (int i = 0; i < in.size(); i++) retSet.add(inArray[i]);
		for (int i = 0; i < out.size(); i++) retSet.add(outArray[i]);
		Constraint[] ret = retSet.toArray(new Constraint[retSet.size()]);
		//		if(ret.length==0){return null;}
		return ret;
	}

	/**
	 * Get all {@link Constraint}s for which a given {@link Variable} is source. 
	 * @param v The {@link Variable} involved in the {@link Constraint}s.
	 * @return All {@link Constraint}s for which a given {@link Variable} is source.
	 */
	public Constraint[] getIngoingEdges(Variable v) {
		Collection<Constraint> in = this.graph.getInEdges(v);
		if (in == null) return null;
		Constraint[] inArray = in.toArray(new Constraint[in.size()]);
		HashSet<Constraint> retSet = new HashSet<Constraint>();
		for (int i = 0; i < in.size(); i++) retSet.add(inArray[i]);
		Constraint[] ret = retSet.toArray(new Constraint[retSet.size()]);
		return ret;
	}


	/**
	 * Get all {@link Constraint}s for which a given {@link Variable} is destination. 
	 * @param v The {@link Variable} involved in the {@link Constraint}s.
	 * @return All {@link Constraint}s for which a given {@link Variable} is destination.
	 */
	public Constraint[] getOutgoingEdges(Variable v) {
		Collection<Constraint> out = this.graph.getOutEdges(v);
		if (out == null) return null;
		Constraint[] outArray = out.toArray(new Constraint[out.size()]);
		HashSet<Constraint> retSet = new HashSet<Constraint>();
		for (int i = 0; i < out.size(); i++) retSet.add(outArray[i]);
		Constraint[] ret = retSet.toArray(new Constraint[retSet.size()]);
		return ret;
	}

	/**
	 * Get all variables in the network.
	 * @return All variables in the network.
	 */
	public Variable[] getVariables() {
		HashSet<Variable> ret = new HashSet<Variable>();
		for (Variable v : this.graph.getVertices()) {
			if (!hyperEdges.containsValue(v)) ret.add(v);
		}
		for (Constraint c : hyperEdges.keySet()) {
			for (Variable v : c.getScope()) {
				ret.add(v);
			}
		}
		return ret.toArray(new Variable[ret.size()]);
		//return this.graph.getVertices().toArray(new Variable[this.graph.getVertexCount()]);
	}

	/**
	 * Get all variables in the network with a given component.
	 * @return All variables in the network with a given component.
	 */
	public Variable[] getVariables(String component, Object ... markingsToExclude) {
		return this.solver.getVariables(component, markingsToExclude);
	}

	/**
	 * Get all variables in the network with a given component.
	 * @return All variables in the network with a given component.
	 */
	public Variable[] getVariables(String component) {
		return this.solver.getVariables(component);
	}

	/**
	 * Get all {@link Constraint}s in the network.
	 * @return All the {@link Constraint}s in the network.
	 */
	public Constraint[] getConstraints() {
		Vector<Constraint> ret = new Vector<Constraint>();
		for (Constraint c : this.graph.getEdges()) {
			//if (c.getScope().length == 2) {
			if (c instanceof BinaryConstraint || c instanceof MultiBinaryConstraint) {
				ret.add(c);
			}
		}
		for (Constraint c : hyperEdges.keySet()) ret.add(c);
		return ret.toArray(new Constraint[ret.size()]);
		//return this.graph.getEdges().toArray(new Constraint[this.graph.getEdgeCount()]);
	}


	/**
	 * Query the network for the existence of a given {@link Constraint}. 
	 * @param c The {@link Constraint} for the query. 
	 * @return <code>true</code> iff the network contains the given {@link Constraint} 
	 */
	public boolean containsConstraint(Constraint c) {
		return (this.graph.containsEdge(c) || this.hyperEdges.containsKey(c));
	}

	/**
	 * Query the network for the existence of a given {@link Variable}. 
	 * @param v The {@link Variable} for the query. 
	 * @return <code>true</code> iff the network contains the given {@link Variable} 
	 */
	public boolean containsVariable(Variable v) {
		return this.graph.containsVertex(v);
	}

	/**
	 * Query the network for the existence of a {@link Variable} with a given ID. 
	 * @param ID The ID of the {@link Variable} for the query. 
	 * @return <code>true</code> iff the network contains the {@link Variable} with the given ID.
	 */
	public boolean containsVariable(int ID) {
		Collection<Variable> vars = this.graph.getVertices();
		for (Variable v : vars) if (v.getID() == ID) return true;
		return false;
	}

	/**
	 * Get a {@link String} representation of this {@link ConstraintNetwork}
	 * (note that {@link MetaConstraintSolver}s use {@link ConstraintNetwork}s as resolvers,
	 * therefore this method is used to draw the edge label in the search tree rendering).
	 * @return A {@link String} representation of this {@link Constraint}.
	 */
	public String getEdgeLabel() {
		String ret = "";
		for (Constraint con : this.getConstraints()) ret += con.toString() + "\n";
		return ret;
	}

	/**
	 * Get a {@link String} representation of this {@link ConstraintNetwork}.
	 * @return A {@link String} representation of this {@link ConstraintNetwork}.
	 */
	public String toString() {
		return "[ConstraintNetwork]: \n\tVertices: " + Arrays.toString(this.getVariables()) + "\n\tConstriants: " + Arrays.toString(this.getConstraints());		
		//		return "[ConstraintNetwork]: marking -> "+ this.getMarking().getState()+"\n\tVertices: " + Arrays.toString(this.getVariables()) + "\n\tConstriants: " + Arrays.toString(this.getConstraints());
	}

	/**
	 * Merges the given {@link ConstraintNetwork} to this.
	 * @param cn The {@link ConstraintNetwork} to merge into this.
	 */
	public void join(ConstraintNetwork cn) {
		for (Variable var : cn.getVariables()) this.addVariable(var);
		for (Constraint con : cn.getConstraints()) this.addConstraint(con);
	}

	/**
	 * Checks if the given {@link ConstraintNetwork} is equal to this.
	 * @return <code>true</code> iff the given {@link ConstraintNetwork} has the same
	 * {@link Variable}s and the same {@link Constraint}s as this.
	 */
	public boolean equals(Object o) {		
		if (!(o instanceof ConstraintNetwork)) return false;
		ConstraintNetwork otherNetwork = (ConstraintNetwork)o;
		for (Variable v : otherNetwork.getVariables()) if (!this.containsVariable(v)) return false;
		for (Constraint c : otherNetwork.getConstraints()) if (!this.containsConstraint(c)) return false;
		for(Variable v: this.getVariables()) if(!otherNetwork.containsVariable(v)) return false;
		for(Constraint c: this.getConstraints()) if (!this.containsConstraint(c))return false;
		return true;
	}

	//	private void writeObject(ObjectOutputStream out) throws IOException {
	//		out.defaultWriteObject();
	//	}
	//
	//	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
	//		in.defaultReadObject();
	//		logger = MetaCSPLogging.getLogger(this.getClass());
	//	}

	/**
	 * Weight associated to the {@link ConstraintNetwork} for some metrics.
	 * @return Weight related to the {@link ConstraintNetwork} 
	 */
	public double getWeight() {
		return weight;
	}

	/**
	 * Weight to associate to the {@link ConstraintNetwork} for some metrics.
	 * @param weight Weight related to the {@link ConstraintNetwork}.
	 */
	public void setWeight(double weight) {
		this.weight = weight;
	}

	/**
	 * Clone this {@link ConstraintNetwork}.
	 * @return A new {@link ConstraintNetwork} of the runtime type of the original.
	 */
	public Object clone() {

		try {
			Constructor<?> c = this.getClass().getConstructor(new Class[] {ConstraintSolver.class});
			ConstraintNetwork ret = (ConstraintNetwork)c.newInstance(new Object[] {this.solver});
			for (Variable v : this.getVariables()) ret.addVariable(v);
			for (Constraint con : this.getConstraints()) ret.addConstraint(con);
			return ret;
		}
		catch (SecurityException e) { e.printStackTrace(); }
		catch (NoSuchMethodException e) { e.printStackTrace(); }
		catch (IllegalArgumentException e) { e.printStackTrace(); }
		catch (InstantiationException e) { e.printStackTrace(); }
		catch (IllegalAccessException e) { e.printStackTrace(); }
		catch (InvocationTargetException e) { e.printStackTrace(); }
		return null;
	}

	/**
	 * Set an annotation for this {@link ConstraintNetwork}.
	 * @param ann An object that should be used to annotate this {@link ConstraintNetwork}.
	 */
	public void setAnnotation(Object ann) { this.annotation = ann; }

	/**
	 * Get the annotation for this {@link ConstraintNetwork}.
	 * @return An object that is used to annotate this {@link ConstraintNetwork}.
	 */
	public Object getAnnotation() { return this.annotation; }

	/**
	 * Get all the substitutions of {@link VariablePrototype}s to {@link Variable}s.
	 * @return All the substitutions of {@link VariablePrototype}s to {@link Variable}s.
	 */
	public HashMap<VariablePrototype, Variable> getSubstitutions() {
		return substitutions;
	}

	/**
	 * Get all the inverse substitutions of {@link VariablePrototype}s to {@link Variable}s.
	 * @return All the inverse substitutions of {@link VariablePrototype}s to {@link Variable}s.
	 */
	public HashMap<Variable, VariablePrototype> getInverseSubstitutions() {
		return substituted;
	}

	/**
	 * Get the marking of this {@link ConstraintNetwork}.
	 * @return The marking of this {@link ConstraintNetwork}.
	 */
	public ConstraintNetworkMarking getMarking() {
		return marking;
	}
	/**
	 * Set the marking of this {@link ConstraintNetwork}. 
	 * @param marking The marking of this {@link ConstraintNetwork}.
	 */
	public void setMarking(ConstraintNetworkMarking marking) {
		this.marking = marking;
	}

	/**
	 * This method returns the variables which have been created in the network based on {@link VariablePrototype}s (i.e.,
	 * the ones that first were {@link VariablePrototype}s) 
	 * @return The variables which have been created in the network based on {@link VariablePrototype}s. 
	 */
	Variable[] getNativeVariables(){
		return this.substituted.keySet().toArray(new Variable[this.substituted.keySet().size()]);
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		for (Field f : ConstraintNetwork.class.getDeclaredFields()) {
			if (Modifier.isTransient(f.getModifiers())) {
				try { backupForSerialization.put(new FieldOfObject(f), f.get(this)); }
				catch (IllegalArgumentException e) { e.printStackTrace(); }
				catch (IllegalAccessException e) { e.printStackTrace(); }
			}
		}
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		for (Field f : ConstraintNetwork.class.getDeclaredFields()) {
			if (Modifier.isTransient(f.getModifiers())) {
				Object foo = backupForSerialization.get(new FieldOfObject(f));
				try { f.set(this, foo); }
				catch (IllegalArgumentException e) { e.printStackTrace(); }
				catch (IllegalAccessException e) { e.printStackTrace(); }
			}
		}
	}

	/**
	 * Get all {@link Variable}s that are directly connected to a given {@link Variable} through one {@link Constraint}.
	 * @param var The {@link Variable} from which the connected {@link Variable}s are to be computed. 
	 * @return All {@link Variable}s that are directly connected to a given {@link Variable} through one {@link Constraint}.
	 */
	public Variable[] getNeighboringVariables(Variable var) {
		HashSet<Variable> ret = new HashSet<Variable>();
		if (var instanceof DummyVariable) return ret.toArray(new Variable[ret.size()]);
		Collection<Variable> neighbors = this.g.getNeighbors(var);
		for (Variable neighbor : neighbors) {
			if (neighbor instanceof DummyVariable) {
				Collection<Variable> neighborsOfNeighbor = this.g.getNeighbors(neighbor);
				ret.addAll(neighborsOfNeighbor);
				ret.remove(var);
			}
			else {
				ret.add(neighbor);
			}
		}
		return ret.toArray(new Variable[ret.size()]);
	}


	/**
	 * Masks all {@link Constraint}s in this {@link ConstraintNetwork}.
	 */
	public void maskConstraints() {
		for (Constraint con : this.getConstraints()) con.mask();
	}

	/**
	 * Unmasks all {@link Constraint}s in this {@link ConstraintNetwork}.
	 */
	public void unmaskConstraints() {
		for (Constraint con : this.getConstraints()) con.unmask();
	}

	/**
	 * Masks all given {@link Constraint}s.
	 * @param cons The {@link Constraint}s to mask.
	 */
	public static void maskConstraints(Constraint[] cons) {
		for (Constraint con : cons) con.mask();
	}

	/**
	 * Unmasks all given {@link Constraint}s.
	 * @param cons The {@link Constraint}s to unmask.
	 */
	public static void unmaskConstraints(Constraint[] cons) {
		for (Constraint con : cons) con.unmask();
	}

	/**
	 * Get all {@link Constraint}s that are not masked.
	 * @return All {@link Constraint}s that are not masked.
	 */
	public Constraint[] getUnmaskedConstraints() {
		Vector<Constraint> ret = new Vector<Constraint>();
		for (Constraint con : this.getConstraints()) {
			if (!con.isMasked()) ret.add(con);
		}
		return ret.toArray(new Constraint[ret.size()]);
	}

	/**
	 * Get all {@link Constraint}s that are not masked.
	 * @return All {@link Constraint}s that are not masked.
	 */
	public Constraint[] getMaskedConstraints() {
		Vector<Constraint> ret = new Vector<Constraint>();
		for (Constraint con : this.getConstraints()) {
			if (con.isMasked()) ret.add(con);
		}
		return ret.toArray(new Constraint[ret.size()]);
	}

}

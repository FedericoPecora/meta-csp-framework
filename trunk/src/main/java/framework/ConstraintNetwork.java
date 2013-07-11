package framework;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

import javax.swing.JFrame;

import symbols.SymbolicVariable;
import throwables.NonInstantiatedDomain;
import utility.UI.Callback;
import utility.UI.ConstraintNetworkFrame;
import utility.logging.MetaCSPLogging;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.ObservableGraph;
import framework.meta.MetaConstraintSolver;
import framework.multi.MultiBinaryConstraint;


/**
 * This class implements the necessary functionality to maintain networks of {@link Constraint}s.  It is used by all
 * {@link ConstraintSolver}s and provides methods to add and remove {@link Variable}s, {@link Constraint}s, querying the
 * network for constraints, incident edges, etc.  It also provide basic graphical rendering functions.
 * Note that all implementing classes must call the one argument constructor.  
 * 
 * @author Federico Pecora
 */

public abstract class ConstraintNetwork implements Cloneable, Serializable {
	
	protected ConstraintSolver solver;
	protected ObservableGraph<Variable,Constraint> graph;
	protected DirectedSparseMultigraph<Variable,Constraint> g;
	protected HashMap<Integer, Variable> variables = new HashMap<Integer, Variable>();
	protected HashMap<Variable, Integer> variablesR = new HashMap<Variable, Integer>();
	protected HashMap<VariablePrototype,Variable> substitutions = new HashMap<VariablePrototype, Variable>();
	
	public Object annotation;
	
	private transient Logger logger = MetaCSPLogging.getLogger(this.getClass());
	private static final long serialVersionUID = 7526472295622776148L;
	
	private double weight=-1;

	
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
		logger.finest("Added susbstitution " + vp + " <-- " + v);
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
	 * Instantiates a new {@link ConstraintNetwork}.  Note that all implementing classes must call this constructor
	 * (i.e., specifying a {@link ConstraintSolver} is mandatory).
	 * @param sol The {@link ConstraintSolver} which maintains this {@link ConstraintNetwork}.
	 */
	public ConstraintNetwork(ConstraintSolver sol) {
		solver = sol;
		g = new DirectedSparseMultigraph<Variable,Constraint>();
		graph = new ObservableGraph<Variable,Constraint>(g);
		this.weight=-1;
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
	}
	
	/**
	 * Adds a {@link Constraint} to the network.
	 * @param c The {@link Constraint} to add to the network. NOTE: this only works for
	 * {@link BinaryConstraint}s and {@link MultiBinaryConstraint}s in the current implementation. 
	 */
	public void addConstraint(Constraint c) {
		if (c.getScope().length == 2) {
			for (Variable v : c.getScope()) if (!this.containsVariable(v)) this.addVariable(v);
			this.graph.addEdge(c, c.getScope()[0], c.getScope()[1]);
			logger.finest("Added constraint " + c);
		}
	}
	
	/**
	 * Removes a given {@link Constraint} from the network.
	 * @param c The {@link Constraint} to remove from the network.
	 */
	public void removeConstraint(Constraint c) {
		this.graph.removeEdge(c);
		logger.finest("Removed constraint " + c);
	}

	/**
	 * Gets the source {@link Variable} of a given {@link Constraint}.
	 * @param c The {@link Constraint} from which to get the source {@link Variable}.
	 * @return The source {@link Variable} of the given {@link Constraint}.
	 */
	public Variable getVariableFrom(Constraint c) {
		return graph.getSource(c);
	}

	/**
	 * Gets the destination {@link Variable} of a given {@link Constraint}.
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
	 * whould occur (in some CSPs, domains of {@link Variable}s cannot be instantiated by the {@link Variable} constructor,
	 * rather an explicit call to a dedicated method is necessary - see, e.g., {@link SymbolicVariable}s).
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
		ConstraintNetwork.draw(cn, cn.getClass().getSimpleName(), null);
	}
	
	/**
	 * Same as the static two-argument draw method, using the {@link ConstraintNetwork}'s simple class name as the
	 * title of the {@link JFrame}.
	 * @param cn The {@link ConstraintNetwork} to draw.
	 * @param cb A {@link Callback} object to use when the button is pressed. 
	 */
	public static void draw(ConstraintNetwork cn, Callback cb) {
		ConstraintNetwork.draw(cn, cn.getClass().getSimpleName(), cb);
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

		return this.graph.getVertices().toArray(new Variable[this.graph.getVertexCount()]);
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
		return this.graph.getEdges().toArray(new Constraint[this.graph.getEdgeCount()]);
	}
	
	/**
	 * Query the network for the existence of a given {@link Constraint}. 
	 * @param c The {@link Constraint} for the query. 
	 * @return <code>true</code> iff the network contains the given {@link Constraint} 
	 */
	public boolean containsConstraint(Constraint c) {
		return this.graph.containsEdge(c);
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
		return "[ConstraintNetwork]\n\tVertices: " + Arrays.toString(this.getVariables()) + "\n\tConstriants: " + Arrays.toString(this.getConstraints());
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
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		logger = MetaCSPLogging.getLogger(this.getClass());
	}

	/**
	 * Weight associated to the {@link ConstraintNetwork} for some metrics.
	 * @return Weight related to the {@link ConstraintNetwork} 
	 */
	public double getWeight() {
		return weight;
	}

	/**
	 * Weight to associate to the {@link ConstraintNetwork} for some metrics.
	 * @param weight Weight related to the {@link ConstraintNetwork} 
	 */
	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	public Object clone() {

		try {
			Constructor c = this.getClass().getConstructor(new Class[] {ConstraintSolver.class});
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
	
}

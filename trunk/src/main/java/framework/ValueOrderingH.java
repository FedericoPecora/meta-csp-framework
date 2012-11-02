package framework;

import java.util.Comparator;

import framework.meta.MetaConstraintSolver;

/**
 * Basic abstract class for implementing value ordering heuristics to be used in backtracking search (e.g., in the {@link MetaConstraintSolver} class).
 * @author Federico Pecora
 *
 */
public abstract class ValueOrderingH implements Comparator<ConstraintNetwork> {


}

package org.metacsp.framework;

import java.util.EventObject;

/** 
 * This class lets the listener know when the change occured and what 
 * object was changed.
 */
public class ConstraintNetworkChangeEvent extends EventObject {

	private static final long serialVersionUID = -7984239608365692506L;
	private final ConstraintNetwork dispatcher;
	private final ConstraintNetwork added;
	private final ConstraintNetwork removed;

	/**
	 * Creates a new {@link ConstraintNetworkChangeEvent}.
	 * @param dispatcher The {@link ConstraintNetwork} that acts as a dispatcher.
	 * @param added A {@link ConstraintNetwork} containing the {@link Variable}s and {@link Constraint}s that were added.
	 * @param removed A {@link ConstraintNetwork} containing the {@link Variable}s and {@link Constraint}s that were removed.
	 */
    public ConstraintNetworkChangeEvent(ConstraintNetwork dispatcher, ConstraintNetwork added, ConstraintNetwork removed) {
        super(dispatcher);
        this.dispatcher = dispatcher;
        this.added = added;
        this.removed = removed;
    }

    /**
     * Get the {@link ConstraintNetwork} that dispatched this change.
     * @return The {@link ConstraintNetwork} that dispatched this change.
     */
    public ConstraintNetwork getDispatcher() {
        return dispatcher;
    }

	/**
	 * Get the added {@link Variable}s and {@link Constraint}s.
	 * @return The added {@link Variable}s and {@link Constraint}s.
	 */
	public ConstraintNetwork getAdded() {
		return added;
	}

	/**
	 * Get the removed {@link Variable}s and {@link Constraint}s.
	 * @return The removed {@link Variable}s and {@link Constraint}s.
	 */
	public ConstraintNetwork getRemoved() {
		return removed;
	}
    
}
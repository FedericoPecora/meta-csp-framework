package org.metacsp.framework;

import java.util.EventListener;

/**
 * Listener interface for classes interested in knowing about a boolean
 * flag change.
 */
public interface ConstraintNetworkChangeListener extends EventListener {

    public void stateChanged(ConstraintNetworkChangeEvent event);

}

package org.metacsp.sensing;

import org.metacsp.multi.allenInterval.AllenIntervalConstraint;

import cern.colt.Arrays;

public class NetworkMaintenanceError extends Error {
	public NetworkMaintenanceError(AllenIntervalConstraint ... con) {
		super("Cannot add maintenance constraint(s) " + Arrays.toString(con));
	}

}

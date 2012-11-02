package throwables.time;

import time.SimpleDistanceConstraint;

public class MalformedSimpleDistanceConstraint extends Error {
	
	private static final long serialVersionUID = 1L;

	public MalformedSimpleDistanceConstraint(SimpleDistanceConstraint c, int bugID) {
		super("SimpleDistanceConstraint " + c.toString() + " is malformed (this is a BUG (ref #" + bugID + ") -- please notify maintainer(s))");
	}	
}

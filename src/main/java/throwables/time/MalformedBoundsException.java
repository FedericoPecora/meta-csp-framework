package throwables.time;

import time.Bounds;
import multi.allenInterval.AllenIntervalConstraint;

public class MalformedBoundsException extends Error {
	private static final long serialVersionUID = -3255990123215211931L;
	public MalformedBoundsException(AllenIntervalConstraint.Type t, Bounds b) {
		super("Cannot make " + t + " constraints with bounds " + b);
	}
}

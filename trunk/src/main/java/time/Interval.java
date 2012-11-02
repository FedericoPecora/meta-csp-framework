package time;

import framework.Domain;
import framework.ValueChoiceFunction;

/**
 * Represents intervals of time described by a start and an end time. 
 * Used as a domain for the APSPSolver variables (i.e., TimePoints), and also as a
 * utility class to represent intervals (e.g., for constraints, etc.).
 * Derived from original implementation by
 * the Planning and Scheduling Team (ISTC-CNR) under project APSI.
 *  
 * @author Federico Pecora and Planning and Scheduling Team
 */
public class Interval extends Domain {

	private Bounds bounds;

    /**
     * Builds a zero-length time interval (start = stop = 0).
     */
    public Interval(TimePoint timePoint) {
    	super(timePoint); 
    	bounds = new Bounds(0,0);
    }
    
    /**
     * Builds a time interval with the given start and end times.
     * @param start The start time of the interval.
     * @param stop The end time of the interval.
     */
    public Interval(TimePoint timePoint, long start, long stop) {
    	super(timePoint);
    	bounds = new Bounds(start, stop);
    }
    
    private final static ValueChoiceFunction startFunction = new ValueChoiceFunction() {
		@Override
		public Object getValue(Domain dom) {
			return ((Interval)dom).bounds.min;
		}
    };
    
    private final static ValueChoiceFunction endFunction = new ValueChoiceFunction() {
		@Override
		public Object getValue(Domain dom) {
			return ((Interval)dom).bounds.max;
		}
    };

	@Override
	protected void registerValueChoiceFunctions() {
		this.registerValueChoiceFunction(startFunction, "ET");
		this.registerValueChoiceFunction(endFunction, "LT");
	}
	
	/**
     * Compares two time intervals, returning true iff their start and end times coincide.
     * @return True iff the two intervals coincide and the argument is also an {@link Interval}.
     */
	@Override
    public boolean equals (Object obj) {
    	return (obj instanceof Interval) && this.compareTo(obj) == 0;
    }

    /**
     * Returns A String representation of the {@link Interval}.
     * @return A String describing the {@link Interval}.
     */
	@Override
	public String toString () {
		return bounds.toString();
	}

	public Bounds getBounds() { return bounds; }
	
	/**
	 * Compares this to another Interval
	 * @return {@code 1} if this Interval's lower bound is lower than the argument's, {@code 0} if they are the same, {@code 1} otherwise  
	 */
	@Override
	public int compareTo(Object arg0) {
		Interval that = (Interval)arg0;
		return this.bounds.compareTo(that.getBounds());
	}
	
	@Override
	public int hashCode() {
		return bounds.hashCode();
	}
	
	public long getLowerBound() { return bounds.min; }
	
	public long getUpperBound() { return bounds.max; }
	
//	public static Interval intersect(Interval a, Interval b) {
//		long _start = Math.max(a.start, b.start);
//		long _stop = Math.min(a.stop, b.stop);
//		
//		if(_start < _stop) {
//			return new Interval(null, _start, _stop);
//		} else {
//			return null;
//		}
//	}

}


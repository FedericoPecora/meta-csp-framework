package fuzzyAllenInterval;
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
public class SimpleInterval extends Domain {

	
	private String intervalName;
	
    /**
     * Builds a zero-length time interval (start = stop = 0).
     */
    public SimpleInterval(SimpleAllenInterval sai) {
    	super(sai);
    	intervalName = "Interval" + sai.getID();
    }
    
	@Override
	protected void registerValueChoiceFunctions() {
		this.registerValueChoiceFunction(new ValueChoiceFunction(){
			@Override
			public Object getValue(Domain dom) {
				return intervalName;
			}}, "ID");
	}
	
	public String getIntervalName() { return intervalName; }
	
	/**
     * Compares two time intervals, returning true iff their start and end times coincide.
     * @return True iff the two intervals coincide and the argument is also an {@link SimpleInterval}.
     */
    public boolean equals (Object obj) {
    	return (obj instanceof SimpleInterval) && (intervalName.equals(((SimpleInterval)obj).getIntervalName()));
    }

    /**
     * Returns A String representation of the {@link SimpleInterval}.
     * @return A String describing the {@link SimpleInterval}.
     */
	public String toString () {
		return intervalName;
	}

	/**
	 * Comapres this to another Interval
	 * @return {@code 1} if this Interval's lower bound is lower than the argument's, {@code 0} if they are the same, {@code 1} otherwise  
	 */
	@Override
	public int compareTo(Object arg0) {
		if (arg0 instanceof SimpleInterval) {
			SimpleInterval that = (SimpleInterval)arg0;
			return intervalName.compareTo(that.getIntervalName());
		}
		return 0;
	}
	
	@Override
	public int hashCode() {
		return intervalName.hashCode();
	}
	


}


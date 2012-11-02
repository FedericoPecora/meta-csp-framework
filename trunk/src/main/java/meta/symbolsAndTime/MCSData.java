package meta.symbolsAndTime;

import multi.activity.Activity;


/**
 * This class serves as a data structure for maintaining the information associated to minimal critical sets (MCSs).
 * An MCS is a set of {@link Activity} variables objects such that imposing only one precedence constraint among any two of
 * them results in resolving a (n-ary) conflict, e.g., a resource conflict.
 * The ESTA scheduling algorithm implemented in the works by inferring MCSs and attempting to impose constraints which sequences
 * one {@link Activity} in the MCS wrt the others.  The fundamental heuristic that makes this method efficient consists in choosing
 * to post constraints such that (1) sequencing the two decisions (i,j) involved in each constraint has "minimal" consequences
 * with respect to the temporal flexibility (FLEX(i,j)) of the underlying temporal network, and (2) the MCS within which the
 * decisions are chosen at each iteration has the highest value of k, which is a function of FLEX(i,j) for all pairs of
 * activities (i,j) in the MCS.  More details on this algorithm and heuristic are available in
 * [A. Cesta, A. Oddi and S. F. Smith, "A Constraint-based Method for Project Scheduling with Time Windows.
 * In Journal of Heuristics, volume 8 (1):109-136, 2002] and [P. Laborie, M. Ghallab, "Planning with Sharable Resource
 * Constraints", IJCAI 1995] respectively. 
 * @author Planning and Scheduling Team
 * @version 1.0
 */
public class MCSData implements Comparable<MCSData> {

	/**
	 * The value of k for the MCS associated to this {@link MCSData} object. 
	 */
	public float mcsK;
	
	/**
	 * The maximum FLEX(i,j) in the MCS associated to this {@link MCSData} object.
	 */
	public float mcsPcMin;
	
	/**
	 * The source {@link Activity} with which {@link #mcsPcMin} is obtained. 
	 */
	public Activity mcsActFrom;
	
	/**
	 * The destination {@link Activity} with which {@link #mcsPcMin} is obtained.
	 */
	public Activity mcsActTo;

	/**
	 * Create a new {@link MCSData} object.
	 * @param pcmin The lowest FLEX(i,j) in the MCS associated to this {@link MCSData} object.
	 * @param actFrom The i-th {@link Activity}.
	 * @param actTo The j-th {@link Activity}.
	 * @param k The value of k of the MCS associated to this {@link MCSData} object.
	 */
	public MCSData(float pcmin, Activity actFrom, Activity actTo, float k) {
		mcsK = k;
		mcsActFrom = actFrom;
		mcsActTo = actTo;
		mcsPcMin = pcmin;
	}

	/**
	 * Compare this {@link MCSData} object with a reference {@link MCSData} object.
	 * @param o The reference {@link MCSData} object
	 * @return {@code -1} if the value of k for the MCS associated to this {@link MCSData} object is less than that of
	 * the MCS associated to the reference {@link MCSData} object;  {@code 0} if the value of k for the MCS associated to this
	 * {@link MCSData} object is equal to that of the MCS associated to the reference {@link MCSData} object; {@code 1}
	 * otherwise.
	 */
	public int compareTo(MCSData o) {
		if (mcsK < o.mcsK)
			return 1;
		if (mcsK == o.mcsK)
			return 0;
		return -1;
	}

	/**
	 * Get a String representation of this {@link MCSData} object.
	 * @return A String describing this {@link MCSData} object.
	 */
	public String toString() {
		String ret = "[K = " + mcsK + ", pcMin = " + mcsPcMin + ", ActFrom = " + mcsActFrom + ", ActTo = " + mcsActTo + "]";
		return ret;
	}

}
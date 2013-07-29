/*******************************************************************************
 * Copyright (c) 2010-2013 Federico Pecora <federico.pecora@oru.se>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package org.metacsp.time;

import org.metacsp.framework.Domain;
import org.metacsp.framework.ValueChoiceFunction;

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
    
//	@Override
//	protected void registerValueChoiceFunctions() {
//		registerValueChoiceFunction(startFunction, "ET");
//		registerValueChoiceFunction(endFunction, "LT");
//	}
	
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


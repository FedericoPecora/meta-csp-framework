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

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author jug
 * A general purpose min/max value pair.
  */
public class Bounds implements Serializable, Comparable<Bounds> {

	private static final long serialVersionUID = 8712498014900634496L;
	
	public long min;
	public long max;
	
	public Bounds(long min, long max) {
		this.min = min;
		this.max = max;
		
		if(min > max) {
			throw new IllegalArgumentException(String.format("Invalid arguments, min > max, : (%d > %d)", min, max));
		}
	}
	
//	public boolean isEmpty() {
//		return ((this.min == 0) && (this.min == this.max));
//	}
	
	public boolean isSingleton() {
		return this.min == this.max;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Bounds) {
			Bounds bounds = (Bounds) obj;
			return min == bounds.min && max == bounds.max;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(new long[]{min, max});
	}
	
	@Override
	public String toString() {
		return "[" + APSPSolver.printLong(min) + ", " + APSPSolver.printLong(max) + "]";
	}
	
	/**
	 * Get intersection with another {@link Bounds}.
	 * This version treats intervals [n,n] as empty, which is necessary for
	 * intersections in scheduling where "a meets b" should not create a conflict.
	 * @param b The {@link Bounds} object to intersect this with.
	 * @return <code>true</code> is there is a non-empty intersection, <code>null</code> otherwise.
	 */
	public final Bounds intersectStrict(Bounds b) {
		final long _min = Math.max(this.min, b.min);
		final long _max = Math.min(this.max, b.max);
		if(_min < _max) return new Bounds(_min, _max);
		return null;
	}
	
	/**
	 * Get intersection with another {@link Bounds}.
	 * @param b The {@link Bounds} object to intersect this with.
	 * @return <code>true</code> is there is a non-empty intersection, <code>null</code> otherwise.
	 */
	public final Bounds intersect(Bounds b) {
		final long _min = Math.max(this.min, b.min);
		final long _max = Math.min(this.max, b.max);
		if(_min <= _max) return new Bounds(_min, _max);
		return null;
	}
	
	public static Bounds union(Bounds ...b) {
		long _min = Long.MAX_VALUE - 1;
		long _max = Long.MIN_VALUE + 1 ;
		for (int i = 0; i < b.length; i++) {
			_min = Math.min(_min, b[i].min);
			_max = Math.max(_max, b[i].max);
		}
		if(_min <= _max) return new Bounds(_min, _max);
		return null;
	}

	@Override
	public int compareTo(Bounds o) {
		return 2*Long.signum(o.min - this.min) + 1*Long.signum(o.max - this.max);
	}
}

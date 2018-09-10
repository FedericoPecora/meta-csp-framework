package org.metacsp.utility;
// Combinations.java
// Computes nCr -- all the ways you can combine r choices among n total objects.
// Unlike permutations, here order does not matter, so {0,1,2} is the same as {0,2,1}.

import java.util.Arrays;

// The algorithm is from Applied Combinatorics, by Alan Tucker.
// Based on code from koders.com

public class Combination {
	private int n, r;
	private int[] index;
	private boolean hasNext = true;

	public Combination(int n, int r) {
		this.n = n;
		this.r = r;
		index = new int[r];
		for (int i = 0; i<r; i++) index[i] = i;
	}

	public boolean hasNext() { return hasNext; }

	// Based on code from KodersCode:
	// The algorithm is from Applied Combinatorics, by Alan Tucker.
	// Move the index forward a notch. The algorithm finds the rightmost
	// index element that can be incremented, increments it, and then 
	// changes the elements to the right to each be 1 plus the element on their left. 
	//
	// For example, if an index of 5 things taken 3 at a time is at {0 3 4}, only the 0 can
	// be incremented without running out of room. The next index is {1, 1+1, 1+2) or
	// {1, 2, 3}. This will be followed by {1, 2, 4}, {1, 3, 4}, and {2, 3, 4}.

	private void moveIndex() {
		int i = rightmostIndexBelowMax();
		if (i >= 0) {
			index[i] = index[i]+1; 
			for (int j = i+1; j<r; j++)
				index[j] = index[j-1] + 1;
		}
		else hasNext = false;
	}

	public int[] next() {
		if (!hasNext) return null;
		int[] result = new int[r];
		for (int i=0; i<r; i++) result[i] = index[i];
		moveIndex();
		return result;
	}

	// return int,the index which can be bumped up.
	private int rightmostIndexBelowMax() {
		for (int i = r-1; i>=0; i--)
			if (index[i] < n - r + i) return i;
		return -1;
	}

	public static void main(String[] args) {
		Combination c = new Combination(4,2);

		while (c.hasNext()) {
			int[] a = c.next();
			System.out.println(Arrays.toString(a));
		}
	}

}
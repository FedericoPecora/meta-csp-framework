package org.metacsp.utility;

import java.util.Arrays;

/* Copyright (c) 2013 the authors listed at the following URL, and/or
the authors of referenced articles or incorporated external code:
http://en.literateprograms.org/Permutations_with_repetition_(Java)?action=history&offset=20080109002711

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

Retrieved from: http://en.literateprograms.org/Permutations_with_repetition_(Java)?oldid=11971
 */

/**
 * Generates r-permutations of n with repetition. 
 * @author Federico Pecora and authors listed at <a href="http://en.literateprograms.org/Permutations_with_repetition_(Java)?action=history&offset=20080109002711">en.literateprograms.org</a>
 */
public class PermutationsWithRepetition {
	private int n;
	private int r;
	
	/**
	 * Instantiates a new permutation object for r-permutations of n integers.
	 * @param n Number of integers available
	 * @param r Size of permutations
	 */
	public PermutationsWithRepetition(int n, int r) {
		this.n = n;
		this.r = r;
	}
	
	/**
	 * Get all r-permutations of n integers. 
	 * @return All r-permutations of n integers.
	 */
	public int[][] getVariations() {
		int permutations = (int) Math.pow(n, r);
		int[][] table = new int[permutations][r];

		for (int x = 0; x < r; x++) {
			int t2 = (int) Math.pow(n, x);
			for (int p1 = 0; p1 < permutations;) {
				for (int al = 0; al < n; al++) {
					for (int p2 = 0; p2 < t2; p2++) {
						table[p1][x] = al;
						p1++;
					}
				}
			}
		}

		return table;
	}
	
	public static void main(String[] args) {
		PermutationsWithRepetition gen = new PermutationsWithRepetition(5, 3);
		int[][] v = gen.getVariations();
		System.out.println(v.length);
		for (int i = 0; i < v.length; i++) {
			System.out.println(Arrays.toString(v[i]));
		}
	}
}

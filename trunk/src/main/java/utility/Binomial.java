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
package utility;

/*************************************************************************
 *  Compilation:  javac Binomial.java
 *  Execution:    java Binomial N K

 *  Compute binomial(N, K) using Pascal's identity
 *
 *        binomial(n, k) = binomial(n-1, k-1) + binomial(n-1, k)
 *
 *  and dynamic programming.
 *
 *  % java Binomial 10 2
 *  45
 * 
 *  % java Binomial 20 0
 *  1
 * 
 *  % java Binomial 50 20
 *  47129212243960
 * 
 *  % java Binomial 0 0      
 *  1                           // by definition
 * 
 *  % java Binomial 100 15
 *  253338471349988640
 *
 *************************************************************************/

public class Binomial {
    public static long binomial(int N, int K) {
        long[][] binomial = new long[N+1][K+1];
        // base cases
        for (int k = 1; k <= K; k++) binomial[0][k] = 0;
        for (int n = 0; n <= N; n++) binomial[n][0] = 1;
        // bottom-up dynamic programming
        for (int n = 1; n <= N; n++)
            for (int k = 1; k <= K; k++)
            	binomial[n][k] = binomial[n-1][k-1] + binomial[n-1][k];
        return binomial[N][K];
    }
}

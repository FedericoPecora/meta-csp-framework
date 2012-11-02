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
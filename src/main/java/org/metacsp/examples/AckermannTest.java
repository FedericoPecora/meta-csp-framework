package org.metacsp.examples;

public class AckermannTest {

	public static int ack(int m, int n) {
		if (m == 0) return n+1;
		if (n == 0) return ack(m-1,1);
		return ack(m-1,ack(m,n-1));
	}
	
	public static void main(String[] args) {
		System.out.println(ack(4,1));
	}

}

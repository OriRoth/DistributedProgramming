package client;

import compute.Task;
import java.io.Serializable;


public class Fib implements Task<BigInteger>, Serializable {

    private static final long serialVersionUID = 227L;

  
   
   
    private final int n;
    
    /**
     * Construct a task to calculate the n-th Fibonacci. 
     */
    public Fib(int n) {
        this.n = n;
    }

    /**
     * Calculate pi.
     */
    public BigInteger execute() {
        return computeFib(n);
    }

    /**
     * Compute the value of the n-th Fibonacci.
     */
    public static BigInteger computeFib(int n) {
    	if (n <= 1) return n;
        else return computeFib(n-1) + computeFib(n-2);
        
    }
  
}
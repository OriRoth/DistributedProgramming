package client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import compute.Compute;

public class ComputeFib {
    public static void main(String args[]) {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            String name = "Compute";
            Registry registry = LocateRegistry.getRegistry(args[0]);
            Compute comp = (Compute) registry.lookup(name);
            Fib task = new Fib(Integer.parseInt(args[1]));
            long fib = comp.executeTask(task);
            System.out.println(fib);
        } catch (Exception e) {
            System.err.println("ComputeFib exception:");
            e.printStackTrace();
        }
    }    
}
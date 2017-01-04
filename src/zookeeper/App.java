package zookeeper;

import java.util.Random;
import java.util.Scanner;

import org.apache.zookeeper.KeeperException;

import zookeeper.ZookeeperService;

/**
 * Hello world!
 *
 */
public class App {
	static Process process;
	static int n;
	static int processId;

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		String[] ws = in.nextLine().split(" ");
		String[] crash = ws[3].split("-");
		n = Integer.valueOf(ws[1]);
		processId = Integer.valueOf(ws[0]);
		process = new Process(processId, n, Integer.valueOf(ws[2]), Integer.valueOf(crash[0]), crash[1].charAt(0));
		in.close();

		process.print();

		ZookeeperService zkService = new ZookeeperService("/zk_barrier", n, processId);
		try {
			boolean flag = zkService.enter();
			System.out.println("Entered barrier: " + n);
			if (!flag)
				System.out.println("Error when entering the barrier");
		} catch (KeeperException e) {
		} catch (InterruptedException e) {
		}

		try {
			zkService.sendTransaction();
			zkService.runProtocol(process, n);
		} catch (KeeperException | InterruptedException e1) {
		}
		
		
		
		
		while (true) {
			// Generate random integer
			Random rand = new Random();
			int r = rand.nextInt(100);
			// Loop for rand iterations
			for (int i = 0; i < r; i++) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
		}
		// try {
		//
		// barrier.leave();
		// } catch (KeeperException e) {
		// } catch (InterruptedException e) {
		// }
		// //System.out.println("Left barrier");

	}
	
	
}


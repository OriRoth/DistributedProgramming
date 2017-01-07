package zookeeper;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.Scanner;

import org.apache.zookeeper.KeeperException;


/**
 * Hello world!
 *
 */
public class App {
	
	public static void main(String[] args) throws IOException {
		
		
		Scanner in = new Scanner(System.in);
		String[] ws = in.nextLine().split(" ");
		String[] crash = ws[3].split("-");
		
		int n = Integer.valueOf(ws[0]);
		int processId = Integer.valueOf(ws[1]);
		byte[] vote = new byte[1];
		if(ws[2].equals("yes"))
				vote[0]=1;
		
		FileWriter fw = new FileWriter("nbac_output_"+processId+".txt", false);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter out = new PrintWriter(bw);
		Process process = new Process(processId, n, vote, Integer.valueOf(crash[0]), crash[1].charAt(0));
		in.close();
	
		ZookeeperService zkService = new ZookeeperService("/root", process, out);
		try {
			boolean flag = zkService.enter();
			if (!flag)
				System.out.println("Error when entering the barrier");
		} catch (KeeperException e) {
		} catch (InterruptedException e) {
		}

		try {
			zkService.transaction();
			zkService.getVotes();
			zkService.runProtocol();
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

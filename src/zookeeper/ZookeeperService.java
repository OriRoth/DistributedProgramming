package zookeeper;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class ZookeeperService implements Watcher {

	int n;
	int processId;
	byte[] vote = new byte[1];
	byte[] v = new byte[1];
	Process process;
	static ZooKeeper zk = null;
	static Integer mutex;
	static Integer transactionMutex;
	boolean receivedTransaction;
	String root;

	int round = 0;
	Map<Integer, Map<Integer, byte[]>> messages = new HashMap<>();
	Set<Integer> rec = new HashSet<Integer>();
	private int yes;
	private boolean noVote;
	private PrintWriter out;

	/*
	 * TODO:
	 * 
	 * removing smallest id process which sends transactoin or maybe set data
	 * directly in other nodes event.getPath().toString().equals("/"+root)
	 * 
	 * when to set event watch = true and when to set watcher zookeeperservice
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.zookeeper.Watcher#process(org.apache.zookeeper.WatchedEvent)
	 */
	synchronized public void process(WatchedEvent event) {
		synchronized (mutex) {
			try {
				String path = event.getPath();
				System.out.println("Path:" + path + " type: " + event.getType());
				if (event.getType().toString().equals("NodeDataChanged") && path.equals("/root")) {
					System.out.println("received transaction");
					receivedTransaction = true;
					mutex.notify();
				} else if (event.getType().toString().equals("NodeDataChanged") && path.startsWith("/root/")) {
					Stat stat = new Stat();
					byte[] data = zk.getData(path, this, stat);
					int round = stat.getVersion() - 1;

					if (stat.getDataLength() > 1 && !process.getDecide()) {
						decide(data);
						return;
					}
					int id = Integer.valueOf(path.substring("/root/".length()));
					System.out.println("received data: " + data[0] + " from: " + id + " version: " + round);

					if (round == 0)
						if (data[0] == 0) {
							noVote = true;
							mutex.notify();
							return;
						} else
							yes++;

					if (messages.containsKey(round))
						messages.get(round).put(id, data);
					else {
						HashMap<Integer, byte[]> temp = new HashMap<>();
						temp.put(id, data);
						messages.put(round, temp);
					}
					mutex.notify();
				} else
					mutex.notify();

			} catch (KeeperException e) {
				e.getMessage();
			} catch (InterruptedException e) {
				e.getMessage();
			}
		}
	}

	/**
	 * Barrier constructor
	 *
	 * @param address
	 * @param root
	 * @param out
	 * @param out
	 * @param size
	 */
	public ZookeeperService(String root, Process process, PrintWriter out) {
		this.root = root;
		this.n = process.getN();
		this.process = process;
		this.processId = process.getProcessId();
		this.vote = process.getVote();
		this.out = out;

		if (zk == null) {
			try {
				System.out.println("Starting ZK:");
				zk = new ZooKeeper("localhost", 3000, this);
				mutex = new Integer(-1);
				System.out.println("Finished starting ZK: " + zk);
			} catch (IOException e) {
				System.out.println(e.toString());
				zk = null;
			}
		}


		// Create barrier node
		if (zk != null) {
			try {
				Stat s = zk.exists(root, true);
				if (s == null) {
					zk.create(root, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}
			} catch (KeeperException e) {
				System.out.println("Keeper exception when instantiating queue: " + e.toString());
			} catch (InterruptedException e) {
				System.out.println("Interrupted exception");
			}
		}

	}

	/**
	 * Join barrier
	 */
	public boolean enter() throws KeeperException, InterruptedException {
		zk.create(root + "/" + processId, null, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
		while (true) {
			synchronized (mutex) {
				List<String> list = zk.getChildren(root, true);
				if (list.size() < n) {
					mutex.wait();
				} else {
					for (String s : list)
						zk.exists(root + "/" + s, this);
					zk.register(this);
					System.out.println("Entered barrier: " + n);
					return true;

				}
			}
		}
	}

	/**
	 * Wait until all reach barrier
	 */
	public boolean leave() throws KeeperException, InterruptedException {
		zk.delete(root + "/" + processId, 0);
		while (true) {
			synchronized (mutex) {
				List<String> list = zk.getChildren(root, true);
				if (list.size() > 0) {
					mutex.wait();
				} else {
					return true;
				}
			}
		}
	}

	public boolean transaction() throws KeeperException, InterruptedException {
		synchronized (mutex) {
			List<String> list = zk.getChildren(root, this);
			int lowestId = Integer.valueOf(Collections.min(list));
			if (processId == lowestId) {
				zk.setData(root, new byte[0], -1);
				System.out.println(processId + " sends Transaction");
			}
			while (!receivedTransaction)
				mutex.wait();
			zk.setData(root + "/" + processId, vote, 0);
			return true;
		}
	}

	public boolean getVotes() throws KeeperException, InterruptedException {
		synchronized (mutex) {
			System.out.println("getting votes");
			while (yes < n) {
				if (noVote)
					break;
				mutex.wait();
			}
			if (yes == n) {
				v[0] = 1;
				return true;
			}
			v[0] = 0;
			return true;
		}

	}

	public boolean collectMessages() throws InterruptedException, KeeperException {
		synchronized (mutex) {
			while (messages.containsKey(round) && messages.get(round).size() <= Math.floor(n / 2)) {
				mutex.wait();
			}
			return true;
		}
	}

	public void runProtocol() throws InterruptedException, KeeperException {
		int c;
		byte[] est_from_c;
		byte[] est = v;
		byte[] bad = new byte[2];
		Stat stat;
		while (!process.getDecide()) {
			if (process.getCrashRound() == round && process.getCrashLocation() == 'A')
				crash();
			c = round % n + 1;
			est_from_c = bad;
			round++;
			System.out.println("round: " + round + " coordinator: " + c + " value: " + v[0]);
			if (c == processId)
				est_from_c = est;
			else {
				synchronized (mutex) {
					while (!messages.containsKey(round) || !messages.get(round).containsKey(c)) {
						stat = zk.exists(root + "/" + c, this);
						if (stat == null) {
							System.out.println("coordinator crash: " + c);
							break;
						}
						System.out.println("trying to get est_from_c");
						mutex.wait();
					}
					if (messages.containsKey(round) && messages.get(round).containsKey(c))
						est_from_c = messages.get(round).get(c);
				}
			}
			System.out.println("est_from_c: " + est_from_c[0]);
			System.out.println("send message:" + processId);
			zk.setData(root + "/" + processId, est_from_c, round);
			if (process.getCrashRound() == round && process.getCrashLocation() == 'B')
				crash();
			collectMessages();
			if (process.getCrashRound() == round && process.getCrashLocation() == 'C')
				crash();
			boolean done = true;
			for (Integer i : messages.get(round).keySet()) {
				System.out.println("from other:" + messages.get(round).get(i)[0]);
				if (messages.get(round).get(i) == bad) {
					done = false;
					break;
				}
			}
			if (done) {
				bad[0] = est_from_c[0];
				decide(bad);
				return;
			}
			est = v;
			out.println(v[0]);
		}
	}

	private void crash() {
		out.println("crashed");
		out.close();
		System.exit(0);
	}

	private void decide(byte[] vFinal) throws KeeperException, InterruptedException {
		synchronized (mutex) {
			process.setDecide(true);
			System.out.println("decided: " + vFinal[0]);
			out.println(vFinal[0]);
			out.close();
			zk.setData(root + "/" + processId, vFinal, -1);
			System.exit(0);
		}
	}
}

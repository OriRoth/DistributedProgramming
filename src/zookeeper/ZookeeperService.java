package zookeeper;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class ZookeeperService implements Watcher {
	int size;
	int processId;
	static ZooKeeper zk = null;
	static Integer mutex;
	String root;
	List<Message> messages = new ArrayList<>();

	// Set<Integer> suspected = new HashSet<>();
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
			System.out.println("Event path: " + event.getPath() + " Event name: " + event.getType().toString());

			if (event.getType().toString().equals("NodeChildrenChanged")){
			// int id =
			// Integer.valueOf(event.getPath().substring("/zk_barrier/".length()));
			// Stat state = null;
			// byte[] data = null;
			// try {
			// data = zk.getData(event.getPath(), true, state);
			// } catch (KeeperException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// @SuppressWarnings("null")//??
			// int version = state.getVersion() +1;
			// System.out.println(
			// "received message in Round: " + version + "from process: " + id +
			// "with data :" + data);
			// Message message = new Message(data, version);
			// messages.add(message);
			// mutex.notify();
			//
			// }else
			if (event.getType().toString().equals("NodeDataChanged")
					&& event.getPath().toString().equals("/zk_barrier")) {
				System.out.println("received transaction");
				try {
					zk.getChildren("/zk_barrier", this);
				} catch (KeeperException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mutex.notify();
			} else {

				System.out.println("Process: " + event.getType());
				mutex.notify();
			}

			// //add to suspected when node gets deleted
			// if(event.getType().toString().equals("NodeDeleted")){
			// int id
			// =Integer.valueOf(event.getPath().substring("/zk_barrier/".length()));
			// System.out.println("deleted "+ id);
			// suspected.add(id);
			// }

			}}
	}

	/**
	 * Barrier constructor
	 *
	 * @param address
	 * @param root
	 * @param size
	 */
	public ZookeeperService(String root, int size, Integer processId) {
		this.root = root;
		this.size = size;
		this.processId = processId;

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
		// else mutex = new Integer(-1);

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
	 *
	 * @return
	 * @throws KeeperException
	 * @throws InterruptedException
	 */
	public boolean enter() throws KeeperException, InterruptedException {
		zk.create(root + "/" + processId, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
		while (true) {
			synchronized (mutex) {
				List<String> list = zk.getChildren(root, true);
				if (list.size() < size) {
					mutex.wait();
				} else {
					zk.register(this);
					return true;
				}
			}
		}
	}

	/**
	 * Wait until all reach barrier
	 *
	 * @return
	 * @throws KeeperException
	 * @throws InterruptedException
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

	// public Integer findLowestId() throws KeeperException,
	// InterruptedException{
	// List<String> list = zk.getChildren(root, true);
	// int lowestId = Integer.valueOf(Collections.min(list));
	// System.out.println(lowestId);
	// return lowestId;
	// }

	public boolean sendTransaction() throws KeeperException, InterruptedException {
		while (true) {
			synchronized (mutex) { // not really neeeded
				List<String> list = zk.getChildren(root, true);
				zk.register(this);
				int lowestId = Integer.valueOf(Collections.min(list));
				if (processId == lowestId) {
					// zk.delete(root + "/" + processId, 0);
					zk.setData(root, "1".getBytes(), -1); // does version start
															// at 0?
					zk.register(this);
					System.out.println(processId + " sends Transaction");
					mutex.wait();
					return true;
				}
				mutex.wait();
				return false;
			}
		}
	}

	public boolean sendEstimate() { // version number == round number
		return false;
	}

	// public boolean isSuspected(int processId){
	// return suspected.contains(processId);
	// }

	public void sendMessage(int est_from_c, int version) throws KeeperException, InterruptedException {
		zk.setData(root + "/" + processId, String.valueOf(est_from_c).getBytes(), version);
	}

	public List<Message> collectMessages() throws InterruptedException {
		while (true) {
			synchronized (mutex) {
				if (messages.size() <= size / 2) {
					mutex.wait();
				} else {
					return messages;
				}
			}
		}

	}

	public boolean exists(String string) {
		// TODO Auto-generated method stub
		return false;
	}

	public List<Message> getMessages() {
		return messages;
	}

	public Message receiveFromC(int c) throws KeeperException, InterruptedException {
		Stat state = zk.exists(root + "/" + processId, this); // ??
		byte[] data = zk.getData(root + "/" + processId, this, state);
		Message message = new Message(data, state.getVersion() + 1);
		return message;
	}

	public int runProtocol(Process process,int n) throws InterruptedException, KeeperException {
		int round = 0;
		int c;
		int est = 0;
		int est_from_c;
		int value;
		int id = process.getProcessId();
		int v = process.getVote(); // value
		
		Map<Integer, Message> est_from_other = new HashMap<>();
		while (!process.getDecide()) {
			c = round % n + 1;
			System.out.println("round: " + round + " coordinator: " + c);
			est_from_c = 1;
			round++;
			if (c == id)
				est_from_c = est;
			else {
				zk.exists(root + "/" + c, this);
				while (est_from_other.containsKey(c)
						|| (est_from_other.containsKey(c) && est_from_other.get(c).getRound() != round)) {

					if (est_from_other.containsKey(c) && (est_from_other.get(c).getRound() == round))
						est_from_c = est_from_other.get(c).getValue();
					System.out.println(est_from_other.get(c));
				}

				sendMessage(est_from_c, round - 1); // A
													// KeeperException with
													// error code
													// KeeperException.NoNode
													// will be thrown if no
													// node with the given
													// path exists.
				System.out.println("send message:" + id);
				collectMessages(); // A KeeperException with error code
									// KeeperException.BadVersion will
									// be thrown if the given version
									// does not match the node's
									// version.
				// using different mutex wait()

				// send <EST, ri, est_from_ci> to all
				// wait until <EST, ri, est_from_c> collected from a majority of
				// processes
				while (est_from_other.size() < n / 2) {
					// wait mutex

				}
				// always when waiting for other process also check for decide v
				// sending

			}
			
		}
		return 0;
	}
}
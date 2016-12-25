package zookeeper;
import java.io.IOException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

public class ZkConnectTest {
	static ZooKeeper zk;
	static ZkConnector zkc = new ZkConnector();


	public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
	
	
		zkc.connect("localhost");
		zk = zkc.getZooKeeper();
		zk.create("/newznode", "new znode".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);


	
	}

}

package il.ac.afeka.springzookeeperdemo.services;

import jakarta.annotation.PostConstruct;
import org.apache.zookeeper.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ZooKeeperService implements ZKService {
    private final ZooKeeper zk;
    @Value("${spring.application.name}")
    private String NODE_NAME;


    public ZooKeeperService(ZooKeeper zk) {
        this.zk = zk;
    }

    @PostConstruct
    private void init() throws InterruptedException, KeeperException {
        this.createAllParentNodes();
        this.addToAllNodes(NODE_NAME, "cluster node");
        this.addToLiveNodes(NODE_NAME, "cluster node");
        this.electForMaster();
    }

    @Override
    public String getLeaderNodeData() throws InterruptedException, KeeperException {
        byte[] nodeData = zk.getData(ZKNode.LEADER_NODE, false, null);
        return new String(nodeData);
    }

    @Override
    public void electForMaster() throws InterruptedException, KeeperException {
        try {
            if (this.masterExists()) {
                // add watcher for /elections/leader deletion (leader disconnect)
                this.zk.addWatch(ZKNode.LEADER_NODE, event -> {
                    try {
                        if (event.getType().equals(Watcher.Event.EventType.NodeDeleted)) {
                            electForMaster();
                        }
                    } catch (InterruptedException | KeeperException e) { System.err.println(e.getMessage()); }
                }, AddWatchMode.PERSISTENT);
            } else {
                // register as leader node
                this.createNode(ZKNode.LEADER_NODE, CreateMode.EPHEMERAL, NODE_NAME.getBytes());
            }
        } catch (KeeperException e) { System.err.println(e.getMessage()); }
    }

    @Override
    public boolean masterExists() throws InterruptedException, KeeperException {
        return this.zk.exists(ZKNode.LEADER_NODE, false) != null;
    }

    @Override
    public boolean amILeader() throws InterruptedException, KeeperException {
        return this.getLeaderNodeData().equals(NODE_NAME);
    }

    @Override
    public void addToLiveNodes(String nodeName, String data) throws InterruptedException, KeeperException {
        String path = String.format("%s/%s", ZKNode.LIVE_NODES, nodeName);
        if (data == null) {
            data = NODE_NAME;
        }

        this.createNode(path, CreateMode.EPHEMERAL, data.getBytes());
    }

    @Override
    public List<String> getLiveNodes() throws InterruptedException, KeeperException {
        return this.zk.getChildren(ZKNode.LIVE_NODES, false);
    }

    @Override
    public void addToAllNodes(String nodeName, String data) throws InterruptedException, KeeperException {
        String path = String.format("%s/%s", ZKNode.ALL_NODES, nodeName);
        if (data == null) {
            data = NODE_NAME;
        }

        this.createNode(path, CreateMode.EPHEMERAL, data.getBytes());
    }

    @Override
    public List<String> getAllNodes(boolean includeSelf) throws InterruptedException, KeeperException {
        return this.zk.getChildren(ZKNode.ALL_NODES, false)
                .stream()
                .filter(nodeId -> includeSelf || !nodeId.equals(NODE_NAME))
                .toList();
    }

    @Override
    public void deleteNodeFromCluster(String nodeId) throws InterruptedException, KeeperException {
        this.zk.delete(String.format("%s/%s", ZKNode.ALL_NODES, nodeId), -1);
        this.zk.delete(String.format("%s/%s", ZKNode.LIVE_NODES, nodeId), -1);
        if (this.getLeaderNodeData().equals(nodeId)) {
          this.zk.delete(ZKNode.LEADER_NODE, -1);
        }
    }

    private void createAllParentNodes() throws InterruptedException, KeeperException {
        createNode(ZKNode.ELECTION_NODE, CreateMode.PERSISTENT, new byte[0]);
        createNode(ZKNode.ALL_NODES, CreateMode.PERSISTENT, new byte[0]);
        createNode(ZKNode.LIVE_NODES, CreateMode.PERSISTENT, new byte[0]);
    }

    private void createNode(String path, CreateMode createMode, byte[] data)
            throws InterruptedException, KeeperException {
        if (zk.exists(path, null) != null)
            return;
        zk.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
    }
}

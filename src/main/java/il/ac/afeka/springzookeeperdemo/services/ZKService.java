package il.ac.afeka.springzookeeperdemo.services;

import org.apache.zookeeper.KeeperException;

import java.util.List;

public interface ZKService {

    String getLeaderNodeData() throws InterruptedException, KeeperException;

    void electForMaster() throws InterruptedException, KeeperException;

    boolean masterExists() throws InterruptedException, KeeperException;

    boolean amILeader() throws InterruptedException, KeeperException;

    void addToLiveNodes(String nodeName, String data) throws InterruptedException, KeeperException;

    List<String> getLiveNodes() throws InterruptedException, KeeperException;

    void addToAllNodes(String nodeName, String data) throws InterruptedException, KeeperException;

    List<String> getAllNodes(boolean includeSelf) throws InterruptedException, KeeperException;

    void deleteNodeFromCluster(String nodeId) throws InterruptedException, KeeperException;
}

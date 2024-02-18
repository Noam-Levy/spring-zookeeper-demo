package il.ac.afeka.springzookeeperdemo.services;

public final class ZKNode {
    public static final String ALL_NODES = "/all";
    public static final String LIVE_NODES = "/live";
    public static final String ELECTION_NODE = "/elections";
    public static final String LEADER_NODE = String.format("%s/leader", ELECTION_NODE);

    private ZKNode() {}
}
